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
package si.matjazcerkvenik.alertmonitor.model;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.*;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PAlert;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
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

    public static void main(String... args) {
        AmProps.ALERTMONITOR_PROMETHEUS_SERVER = "http://pgcentos:9090";
        PrometheusSyncTask rt = new PrometheusSyncTask();
        rt.run();
    }

    @Override
    public void run() {

        logger.info("PSYNC: === starting periodic synchronization ===");
        AmMetrics.lastPsyncTimestamp = System.currentTimeMillis();

        try {

            PrometheusApi api = new PrometheusApi();
            List<PAlert> activeAlerts = api.alerts();

            if (activeAlerts == null) {
                logger.error("PSYNC: null response returned");
                logger.info("PSYNC: === Periodic synchronization complete ===");
                AmMetrics.psyncFailedCount++;
                DAO.getInstance().addWarning("psync_failed", "Synchronization is failing");
                return;
            }

            // all alerts retrieved by psync
            List<DEvent> pSyncAlerts = new ArrayList<>();

            for (PAlert alert : activeAlerts) {
                logger.debug(alert.toString());

                DEvent e = new DEvent();
                e.setTimestamp(System.currentTimeMillis());
                e.setAlertname(alert.getLabels().getOrDefault(DEvent.LBL_ALERTNAME, "-unknown-"));
                e.setSource("PSYNC");
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

                logger.debug("PSYNC: " + e.toString());
                pSyncAlerts.add(e);

            } // for each alert

            DAO.getInstance().synchronizeAlerts(pSyncAlerts, true);

            AmMetrics.psyncSuccessCount++;
            DAO.getInstance().removeWarning("psync_failed");

        } catch (Exception e) {
            logger.error("PSYNC: failed to synchronize alarms; root cause: " + e.getMessage());
            AmMetrics.psyncFailedCount++;
            DAO.getInstance().addWarning("psync_failed", "Synchronization is failing");
        }

        logger.info("PSYNC: === Periodic synchronization complete ===");

    }


}
