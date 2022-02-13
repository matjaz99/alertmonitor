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

            if (activeAlerts != null) {

                // set flag toBeDeleted=true for all active alerts before executing resync
                for (DEvent n : DAO.getInstance().getActiveAlerts().values()) {
                    n.setToBeDeleted(true);
                }

                List<DEvent> resyncAlerts = new ArrayList<>();
                int newAlertsCount = 0;

                for (PAlert alert : activeAlerts) {
                    logger.debug(alert.toString());

                    DEvent n = new DEvent();
                    n.setTimestamp(System.currentTimeMillis());
                    n.setAlertname(alert.getLabels().getOrDefault(DEvent.KEY_ALERTNAME, "-unknown-"));
                    n.setSource("PSYNC");
                    n.setUserAgent("Alertmonitor/v1");
                    n.setInstance(alert.getLabels().getOrDefault(DEvent.KEY_INSTANCE, "-"));
                    n.setHostname(Formatter.stripInstance(n.getInstance()));
                    n.setNodename(alert.getLabels().getOrDefault(DEvent.KEY_NODENAME, n.getInstance()));
                    n.setInfo(alert.getLabels().getOrDefault(DEvent.KEY_INFO, "-"));
                    n.setJob(alert.getLabels().getOrDefault(DEvent.KEY_JOB, "-"));
                    n.setTags(alert.getLabels().getOrDefault(DEvent.KEY_TAGS, ""));
                    n.setSeverity(alert.getLabels().getOrDefault(DEvent.KEY_SEVERITY, "indeterminate"));
                    n.setPriority(alert.getLabels().getOrDefault(DEvent.KEY_PRIORITY, "low"));
                    n.setGroup(alert.getLabels().getOrDefault(DEvent.KEY_GROUP, "unknown"));
                    n.setEventType(alert.getLabels().getOrDefault(DEvent.KEY_EVENTTYPE, "5"));
                    n.setProbableCause(alert.getLabels().getOrDefault(DEvent.KEY_PROBABLECAUSE, "1024"));
                    n.setCurrentValue(alert.getAnnotations().getOrDefault(DEvent.KEY_CURRENTVALUE, "-"));
                    n.setUrl(alert.getLabels().getOrDefault(DEvent.KEY_URL, ""));
                    if (alert.getLabels().containsKey(DEvent.KEY_DESCRIPTION)) {
                        n.setDescription(alert.getLabels().getOrDefault(DEvent.KEY_DESCRIPTION, "-"));
                    } else {
                        n.setDescription(alert.getAnnotations().getOrDefault(DEvent.KEY_DESCRIPTION, "-"));
                    }

                    // set prometheusId
                    String[] lblArray = AmProps.ALERTMONITOR_PROMETHEUS_ID_LABELS.split(",");
                    String s = "{";
                    for (int i = 0; i < lblArray.length; i++) {
                        s += lblArray[i].trim() + "=\"" + alert.getLabels().getOrDefault(lblArray[i].trim(), "-") + "\", ";
                    }
                    s = s.substring(0, s.length()-2) + "}";
                    n.setPrometheusId(s);

                    // set all other labels
                    n.setOtherLabels(alert.getLabels());

                    if (!alert.getState().equals("firing")) {
                        // ignore alerts in pending state
                        continue;
                    }
                    n.setStatus("firing");

                    // add other labels directly into tags
                    // eg: severity (but not clear), priority
                    if (!n.getSeverity().equals(DSeverity.CLEAR)) {
                        n.setTags(n.getTags() + "," + n.getSeverity());
                    }
                    n.setTags(n.getTags() + "," + n.getPriority());

                    // environment variable substitution
                    n.setNodename(AlertmanagerProcessor.substitute(n.getNodename()));
                    n.setInfo(AlertmanagerProcessor.substitute(n.getInfo()));
                    n.setDescription(AlertmanagerProcessor.substitute(n.getDescription()));
                    n.setTags(AlertmanagerProcessor.substitute(n.getTags()));
                    n.setUrl(AlertmanagerProcessor.substitute(n.getUrl()));

                    // set unique ID of event
                    n.generateUID();

                    // set correlation ID
                    n.generateCID();

                    logger.debug(n.toString());
                    resyncAlerts.add(n);

                    if (DAO.getInstance().getActiveAlerts().containsKey(n.getCorrelationId())) {
                        logger.info("PSYNC: Alert exists: {uid=" + n.getUid() + ", cid=" + n.getCorrelationId() + ", alertname=" + n.getAlertname() + ", instance=" + n.getInstance() + "}");
                        DAO.getInstance().getActiveAlerts().get(n.getCorrelationId()).setToBeDeleted(false);
                    } else {
                        logger.info("PSYNC: New alert: {uid=" + n.getUid() + ", cid=" + n.getCorrelationId() + ", alertname=" + n.getAlertname() + ", instance=" + n.getInstance() + "}");
                        DAO.getInstance().addActiveAlert(n);
                        newAlertsCount++;
                    }

                } // for each alert

                DAO.getInstance().addToJournal(resyncAlerts);

                // collect all cids that to be deleted
                List<String> cidToDelete = new ArrayList<>();
                for (DEvent n : DAO.getInstance().getActiveAlerts().values()) {
                    if (n.isToBeDeleted()) cidToDelete.add(n.getCorrelationId());
                }

                // clear those in activeAlerts which were not received (toBeDeleted=true)
                List<DEvent> temp = new ArrayList<>();
                for (String cid : cidToDelete) {
                    logger.info("PSYNC: Removing alert: {cid=" + cid + "}");
                    DEvent x = DAO.getInstance().getActiveAlerts().get(cid);
                    // create artificial clear event
                    DEvent xClone = (DEvent) x.clone();
                    xClone.setClearTimestamp(System.currentTimeMillis());
                    xClone.setSeverity(DSeverity.CLEAR);
                    xClone.setSource("PSYNC");
                    xClone.generateUID();
                    temp.add(xClone);
                    DAO.getInstance().removeActiveAlert(x);
                }
                DAO.getInstance().addToJournal(temp);

                logger.info("PSYNC: total psync alerts count: " + resyncAlerts.size());
                logger.info("PSYNC: new alerts count: " + newAlertsCount);
                logger.info("PSYNC: alerts to be deleted: " + cidToDelete.size());

                AmMetrics.psyncSuccessCount++;
                DAO.getInstance().removeWarning("psync_failed");

            } else { // null response
                logger.error("PSYNC: null response returned");
                AmMetrics.psyncFailedCount++;
                DAO.getInstance().addWarning("psync_failed", "Synchronization is failing");
            }

        } catch (Exception e) {
            logger.error("PSYNC: failed to synchronize alarms; root cause: " + e.getMessage());
            AmMetrics.psyncFailedCount++;
            DAO.getInstance().addWarning("psync_failed", "Synchronization is failing");
        }

        logger.info("PSYNC: === Periodic synchronization complete ===");

    }


}
