package si.matjazcerkvenik.alertmonitor.model;

import si.matjazcerkvenik.alertmonitor.model.alertmanager.*;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PAlert;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.simplelogger.SimpleLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class PSyncTask extends TimerTask {

    private SimpleLogger logger = DAO.getLogger();

    public static void main(String... args) {
        DAO.ALERTMONITOR_PSYNC_ENDPOINT = "http://pgcentos:9090";
        PSyncTask rt = new PSyncTask();
        rt.run();
    }

    @Override
    public void run() {

        logger.info("PSYNC: === starting periodic synchronization ===");
        DAO.lastPsyncTimestamp = System.currentTimeMillis();

        try {

            PrometheusApi api = new PrometheusApi();
            List<PAlert> activeAlerts = api.alerts();

            if (activeAlerts != null) {

                // set flag toBeDeleted=true for all active alerts before executing resync
                for (DNotification n : DAO.getInstance().getActiveAlerts().values()) {
                    n.setToBeDeleted(true);
                }

                List<DNotification> resyncAlerts = new ArrayList<>();
                int newAlertsCount = 0;

                for (PAlert alert : activeAlerts) {
                    logger.debug(alert.toString());

                    DNotification n = new DNotification();
                    n.setTimestamp(System.currentTimeMillis());
                    n.setAlertname(alert.getLabels().getOrDefault("alertname", "-unknown-"));
                    n.setSource("PSYNC");
                    n.setUserAgent("Alertmonitor/v1");
                    n.setInstance(alert.getLabels().getOrDefault("instance", "-"));
                    n.setHostname(AlertmanagerProcessor.stripInstance(n.getInstance()));
                    n.setNodename(alert.getLabels().getOrDefault("nodename", n.getInstance()));
                    n.setInfo(alert.getLabels().getOrDefault("info", "-"));
                    n.setJob(alert.getLabels().getOrDefault("job", "-"));
                    n.setTags(alert.getLabels().getOrDefault("tags", ""));
                    n.setSeverity(alert.getLabels().getOrDefault("severity", "indeterminate"));
                    n.setPriority(alert.getLabels().getOrDefault("priority", "low"));
                    n.setTeam(alert.getLabels().getOrDefault("team", "unassigned"));
                    n.setEventType(alert.getLabels().getOrDefault("eventType", "5"));
                    n.setProbableCause(alert.getLabels().getOrDefault("probableCause", "1024"));
                    n.setCurrentValue(alert.getAnnotations().getOrDefault("currentValue", "-"));
                    n.setUrl(alert.getLabels().getOrDefault("url", ""));
                    if (alert.getLabels().containsKey("description")) {
                        n.setDescription(alert.getLabels().getOrDefault("description", "-"));
                    } else {
                        n.setDescription(alert.getAnnotations().getOrDefault("description", "-"));
                    }

                    if (!alert.getState().equals("firing")) {
                        // ignore alerts in pending state
                        continue;
                    }
                    n.setStatus("firing");

                    // add other labels directly into tags
                    // eg: severity (but not clear), priority
                    if (!n.getSeverity().equals(Severity.CLEAR)) {
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
                    DAO.getInstance().addToJournal(n);

                    if (DAO.getInstance().getActiveAlerts().containsKey(n.getCorrelationId())) {
                        logger.info("PSYNC: Alert exists: {uid=" + n.getUid() + ", cid=" + n.getCorrelationId() + ", alertname=" + n.getAlertname() + ", instance=" + n.getInstance() + "}");
                        DAO.getInstance().getActiveAlerts().get(n.getCorrelationId()).setToBeDeleted(false);
                    } else {
                        logger.info("PSYNC: New alert: {uid=" + n.getUid() + ", cid=" + n.getCorrelationId() + ", alertname=" + n.getAlertname() + ", instance=" + n.getInstance() + "}");
                        DAO.getInstance().addActiveAlert(n);
                        newAlertsCount++;
                    }

                } // for each alert

                // clear those in activeAlerts which were not received toBeDeleted=true
                List<String> cidToDelete = new ArrayList<>();
                for (DNotification n : DAO.getInstance().getActiveAlerts().values()) {
                    if (n.isToBeDeleted()) cidToDelete.add(n.getCorrelationId());
                }
                for (String cid : cidToDelete) {
                    logger.info("PSYNC: Removing alert: {cid=" + cid + "}");
                    DNotification x = DAO.getInstance().getActiveAlerts().get(cid);
                    // create artificial clear event
                    DNotification xClone = (DNotification) x.clone();
                    xClone.setClearTimestamp(System.currentTimeMillis());
                    xClone.setSeverity(Severity.CLEAR);
                    xClone.setSource("PSYNC");
                    xClone.generateUID();
                    DAO.getInstance().addToJournal(xClone);
                    DAO.getInstance().removeActiveAlert(x);
                }

                logger.info("PSYNC: total psync alerts count: " + resyncAlerts.size());
                logger.info("PSYNC: new alerts count: " + newAlertsCount);
                logger.info("PSYNC: alerts to be deleted: " + cidToDelete.size());

            }

        } catch (Exception e) {
            logger.error("PSYNC: Failed to synchronize alarms: ", e);
//            e.printStackTrace();
            AmMetrics.alertmonitor_psync_task_total.labels("Failed").inc();
            DAO.psyncFailedCount++;
        }

        logger.info("PSYNC: === Periodic synchronization complete ===");

    }


}
