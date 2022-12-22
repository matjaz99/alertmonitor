/*
   Copyright 2021 Matjaž Cerkvenik

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package si.matjazcerkvenik.alertmonitor.providers;

import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DSeverity;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.*;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PAlert;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusHttpClient;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.simplelogger.SimpleLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class PrometheusSyncTask extends TimerTask {

    private SimpleLogger logger = LogFactory.getLogger();

    private AbstractDataProvider dataProvider;

    public PrometheusSyncTask(AbstractDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void run() {

        logger.info("SYNCTASK[" + dataProvider.getProviderConfig().getName() + "]: === starting synchronization ===");
        dataProvider.setLastSyncTimestamp(System.currentTimeMillis());

        PrometheusHttpClient api = dataProvider.getPrometheusApiClientPool().getClient();

        try {

            List<PAlert> activeAlerts = api.alerts();

            if (activeAlerts == null) {
                logger.error("SYNCTASK[" + dataProvider.getProviderConfig().getName() + "]: null response returned");
                logger.info("SYNCTASK[" + dataProvider.getProviderConfig().getName() + "]: === Periodic synchronization complete ===");
                dataProvider.syncFailedCount++;
                dataProvider.addWarning("sync", "Synchronization is failing");
                return;
            }

            // all alerts retrieved by sync
            List<DEvent> syncAlerts = new ArrayList<>();

            for (PAlert alert : activeAlerts) {
                logger.debug(alert.toString());

                DEvent e = new DEvent();
                e.setTimestamp(System.currentTimeMillis());
                e.setAlertname(alert.getLabels().getOrDefault(DEvent.LBL_ALERTNAME, "-unknown-"));
                e.setSource("SYNC");
                e.setUserAgent("");
                e.setInstance(alert.getLabels().getOrDefault(DEvent.LBL_INSTANCE, "-"));
                e.setHostname(Formatter.stripInstance(e.getInstance()));
                e.setNodename(alert.getLabels().getOrDefault(DEvent.LBL_NODENAME, e.getInstance()));
                e.setInfo(alert.getLabels().getOrDefault(DEvent.LBL_INFO, "-"));
                e.setJob(alert.getLabels().getOrDefault(DEvent.LBL_JOB, "-"));
                e.setTags(alert.getLabels().getOrDefault(DEvent.LBL_TAGS, ""));
                e.setSeverity(alert.getLabels().getOrDefault(DEvent.LBL_SEVERITY, "indeterminate"));
                e.setPriority(alert.getLabels().getOrDefault(DEvent.LBL_PRIORITY, "low"));
                e.setGroup(alert.getLabels().getOrDefault(DEvent.LBL_GROUP, "unknown"));
                e.setEventType(alert.getLabels().getOrDefault(DEvent.LBL_EVENTTYPE, "5"));
                e.setProbableCause(alert.getLabels().getOrDefault(DEvent.LBL_PROBABLECAUSE, "1024"));
                e.setCurrentValue(alert.getAnnotations().getOrDefault(DEvent.LBL_CURRENTVALUE, "-"));
                e.setUrl(alert.getLabels().getOrDefault(DEvent.LBL_URL, ""));
                if (alert.getLabels().containsKey(DEvent.LBL_DESCRIPTION)) {
                    e.setDescription(alert.getLabels().getOrDefault(DEvent.LBL_DESCRIPTION, "-"));
                } else {
                    e.setDescription(alert.getAnnotations().getOrDefault(DEvent.LBL_DESCRIPTION, "-"));
                }

                // set prometheusId
                String[] lblArray = AmProps.ALERTMONITOR_PROMETHEUS_ID_LABELS.split(",");
                String s = "{";
                for (int i = 0; i < lblArray.length; i++) {
                    s += lblArray[i].trim() + "=\"" + alert.getLabels().getOrDefault(lblArray[i].trim(), "-") + "\", ";
                }
                s = s.substring(0, s.length()-2) + "}";
                e.setPrometheusId(s);

                // set all other labels
                e.setOtherLabels(alert.getLabels());

                if (!alert.getState().equals("firing")) {
                    // ignore alerts in pending state
                    continue;
                }
                e.setStatus("firing");

                // add tags
                // eg: severity (but not clear), priority
                if (!e.getSeverity().equals(DSeverity.CLEAR)) {
                    e.setTags(e.getTags() + "," + e.getSeverity());
                }
                e.setTags(e.getTags() + "," + e.getPriority());

                // environment variable substitution
                e.setNodename(AlertmanagerProcessor.substitute(e.getNodename()));
                e.setInfo(AlertmanagerProcessor.substitute(e.getInfo()));
                e.setDescription(AlertmanagerProcessor.substitute(e.getDescription()));
                e.setTags(AlertmanagerProcessor.substitute(e.getTags()));
                e.setUrl(AlertmanagerProcessor.substitute(e.getUrl()));

                // set unique ID of event
                e.generateUID();

                // set correlation ID
                e.generateCID();

                logger.debug("SYNCTASK[" + dataProvider.getProviderConfig().getName() + "]: " + e.toString());
                syncAlerts.add(e);

            } // for each alert

            dataProvider.synchronizeAlerts(syncAlerts, true);

            dataProvider.syncSuccessCount++;
            AmMetrics.alertmonitor_sync_success.labels(dataProvider.providerConfig.getName()).set(1);
            dataProvider.removeWarning("sync");

        } catch (Exception e) {
            logger.error("SYNCTASK[" + dataProvider.getProviderConfig().getName() + "]: failed to synchronize alarms; root cause: " + e.getMessage());
            dataProvider.syncFailedCount++;
            AmMetrics.alertmonitor_sync_success.labels(dataProvider.providerConfig.getName()).set(0);
            dataProvider.addWarning("sync", "Synchronization is failing");
        } finally {
            dataProvider.getPrometheusApiClientPool().returnClient(api);
        }

        logger.info("SYNCTASK[" + dataProvider.getProviderConfig().getName() + "]: === synchronization complete ===");

    }


}
