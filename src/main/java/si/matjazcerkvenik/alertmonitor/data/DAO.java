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
package si.matjazcerkvenik.alertmonitor.data;

import si.matjazcerkvenik.alertmonitor.model.*;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PRule;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PTarget;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiException;
import si.matjazcerkvenik.alertmonitor.util.*;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.*;
import java.util.stream.Collectors;

public class DAO {

    private SimpleLogger logger = LogFactory.getLogger();

    /** Singleton instance */
    private static DAO instance;

    private IDataManager dataManager;

    /** Map of active alerts. Key is correlation id */
    private Map<String, DEvent> activeAlerts = new HashMap<>();

    /** Map of tags of active alerts. Key is the tag name */
    private Map<String, DTag> tagMap = new HashMap<>();

    /** Map of warnings in the alertmonitor. It's a map, because it is easier to search and remove */
    private Map<String, String> warnings = new HashMap<>();



    private DAO() {
        if (AmProps.ALERTMONITOR_MONGODB_ENABLED) {
            dataManager = new MongoDbDataManager();
        } else {
            dataManager = new InMemoryDataManager();
        }
        TaskManager.getInstance().startDbMaintenanceTimer();
    }

    public static DAO getInstance() {
        if (instance == null) {
            instance = new DAO();
        }
        return instance;
    }

    public IDataManager getDataManager() {
        return dataManager;
    }

    /**
     * Add new webhook message to the list. Also delete oldest messages.
     * @param message incoming message
     */
    public void addWebhookMessage(WebhookMessage message) {
        dataManager.addWebhookMessage(message);
        AmMetrics.webhookMessagesReceivedCount++;
        AmMetrics.alertmonitor_webhook_messages_received_total.labels(message.getRemoteHost(), message.getMethod().toUpperCase()).inc();
    }

    public List<WebhookMessage> getWebhookMessages() {
        return dataManager.getWebhookMessages();
    }

    /**
     * Add new notification to journal. Also delete oldest notifications.
     * @param events notifications
     */
    public void addToJournal(List<DEvent> events) {
        dataManager.addToJournal(events);
        AmMetrics.journalReceivedCount++;
        for (DEvent e : events) {
            AmMetrics.alertmonitor_journal_messages_total.labels(e.getSeverity()).inc();
        }
    }

    /**
     * Return whole journal
     * @return list
     */
    public List<DEvent> getJournal() {
        return dataManager.getJournal();
    }

    public long getJournalSize() {
        return dataManager.getJournalSize();
    }

    /**
     * Get single event from journal
     * @param id unique ID of event
     * @return event
     */
    public DEvent getEvent(String id) {

        DEvent event = dataManager.getEvent(id);

        if (event == null) return null;

        try {
            List<PRule> ruleList = new ArrayList<>();
            PrometheusApi api = new PrometheusApi();
            ruleList = api.rules();
            for (PRule r : ruleList) {
                if (event.getAlertname().equals(r.getName())) {
                    event.setRuleExpression(r.getQuery());
                    event.setRuleTimeLimit(r.getDuration());
                }
            }
        } catch (PrometheusApiException e) {
            LogFactory.getLogger().error("DAO: failed to load rules for alert: " + id + "; root cause: " + e.getMessage());
        }

        return event;
    }

    public boolean synchronizeAlerts(List<DEvent> alertList, boolean psync) {

        if (psync) {

            // set flag toBeDeleted=true for all active alerts before executing sync
            for (DEvent e : activeAlerts.values()) {
                e.setToBeDeleted(true);
            }

            List<DEvent> newAlerts = new ArrayList<>();
            int newAlertsCount = 0;

            for (DEvent e : alertList) {
                if (activeAlerts.containsKey(e.getCorrelationId())) {
                    logger.info("PSYNC: Alert exists: {uid=" + e.getUid() + ", cid=" + e.getCorrelationId() + ", alertname=" + e.getAlertname() + ", instance=" + e.getInstance() + "}");
                    activeAlerts.get(e.getCorrelationId()).setToBeDeleted(false);
                } else {
                    logger.info("PSYNC: New alert: {uid=" + e.getUid() + ", cid=" + e.getCorrelationId() + ", alertname=" + e.getAlertname() + ", instance=" + e.getInstance() + "}");
                    e.setFirstTimestamp(e.getTimestamp());
                    e.setLastTimestamp(e.getTimestamp());
                    addActiveAlert(e);
                    newAlerts.add(e);
                    newAlertsCount++;
                }
            }

            // collect all cids that need to be deleted from active alerts
            List<String> cidToDelete = new ArrayList<>();
            for (DEvent e : activeAlerts.values()) {
                if (e.isToBeDeleted()) cidToDelete.add(e.getCorrelationId());
            }

            // generate artificial clear event and
            // remove active alerts which were not received (toBeDeleted=true)
            for (String cid : cidToDelete) {
                logger.info("PSYNC: Removing active alert: {cid=" + cid + "}");
                DEvent e = activeAlerts.get(cid);
                LogFactory.getLogger().info("original: " + e.toString());
                // create artificial clear event
                DEvent eClone = e.generateClearEvent();
                LogFactory.getLogger().info("clone:::: " + eClone.toString());
                newAlerts.add(eClone);
                removeActiveAlert(e);
            }
            addToJournal(newAlerts);

            logger.info("PSYNC: total psync alerts count: " + alertList.size());
            logger.info("PSYNC: new alerts count: " + newAlertsCount);
            logger.info("PSYNC: alerts to be deleted: " + cidToDelete.size());

            return true;
        }


        // regular alarms

        DAO.getInstance().addToJournal(alertList);

        for (DEvent e : alertList) {

            if (AmProps.ALERTMONITOR_KAFKA_ENABLED) KafkaClient.getInstance().publish(AmProps.ALERTMONITOR_KAFKA_TOPIC, Formatter.toJson(e));

            // correlation
            if (DAO.getInstance().getActiveAlerts().containsKey(e.getCorrelationId())) {
                if (e.getSeverity().equalsIgnoreCase(DSeverity.CLEAR)) {
                    removeActiveAlert(e);
                    logger.info("AlertmanagerProcessor: clear alert: cid=" + e.getCorrelationId());
                } else {
                    updateActiveAlert(e);
                    logger.info("AlertmanagerProcessor: updating alert: cid=" + e.getCorrelationId());
                }
            } else {
                if (!e.getSeverity().equalsIgnoreCase(DSeverity.CLEAR)) {
                    e.setFirstTimestamp(e.getTimestamp());
                    e.setLastTimestamp(e.getTimestamp());
                    addActiveAlert(e);
                    logger.info("AlertmanagerProcessor: new alert: cid=" + e.getCorrelationId());
                }
            }

        }

        return true;

    }


    /**
     * Return a map of active alerts
     * @return map of all active alerts
     */
    public Map<String, DEvent> getActiveAlerts() {
        return activeAlerts;
    }

    /**
     * Add new alert to active alerts. This method is called when first alert
     * of this type occurs (according to correlationId). First and last timestamps
     * are set to time of reception (timestamp). Also new tags are added to tagMap.
     * @param event
     */
    public void addActiveAlert(DEvent event) {

        activeAlerts.put(event.getCorrelationId(), event);
        AmMetrics.raisingEventCount++;
        LogFactory.getAlertLog().write(event.toString());

        // parse tags from tags label
        String[] array = event.getTags().split(",");
        for (int i = 0; i < array.length; i++) {
            String tagName = array[i].trim();
            if (tagName.length() > 0) {
                DTag t = new DTag(tagName, TagColors.getColor(tagName));
                tagMap.putIfAbsent(t.getName(), t);
            }
        }

    }

    /**
     * New alert has appeared and must replace existing active alert. Since this
     * is new occurrence of existing alert, the firstTimestamp is overwritten
     * by existing firstTimestamp (time when alert occurred for the first time).
     * The lastTimestamp is set to time of reception and counter is increased by 1
     * (according to existing alert).
     * Alert then finally replaces reference in activeAlert map so it points to new
     * alert.
     * @param newEvent last received notificatioin
     */
    public void updateActiveAlert(DEvent newEvent) {
        DEvent existingEvent = activeAlerts.get(newEvent.getCorrelationId());
        // take data from existing alert and fill it into new alert
        newEvent.setFirstTimestamp(existingEvent.getFirstTimestamp());
        newEvent.setLastTimestamp(newEvent.getTimestamp());
        if (newEvent.getSource().equalsIgnoreCase("RESYC")) {
            // resync alert, don't increase counter
        } else {
            // regular alert
            newEvent.setCounter(existingEvent.getCounter() + 1);
        }
        // replace existing with new one
        activeAlerts.put(existingEvent.getCorrelationId(), newEvent);
    }

    /**
     * Clear arrived and active alert must be removed. Before removing,
     * all alerts in journal have clearTimestamp corrected to point to clear event.
     * @param event
     */
    public void removeActiveAlert(DEvent event) {
        dataManager.handleAlarmClearing(event);
        event.setFirstTimestamp(event.getTimestamp());
        event.setLastTimestamp(event.getTimestamp());
        activeAlerts.remove(event.getCorrelationId());
        removeObsoleteTags();
        AmMetrics.clearingEventCount++;
        LogFactory.getAlertLog().write(event.toString());
    }

    /**
     * Remove tags which have no active alerts left.
     */
    private void removeObsoleteTags() {
        Map<String, Object> allTags = new HashMap<String, Object>();
        for (DEvent n : activeAlerts.values()) {
            String[] array = n.getTags().split(",");
            for (int i = 0; i < array.length; i++) {
                String tagName = array[i].trim();
                if (tagName.length() > 1) {
                    allTags.putIfAbsent(tagName, null);
                }
            }
        }
        if (allTags.isEmpty()) {
            tagMap.clear();
            return;
        }
//        for (String n : tagMap.keySet()) {
//            if (!allTags.containsKey(n)) {
//                tagMap.remove(n);
//            }
//        }
//        Iterator<Map.Entry<String, DTag>> it = tagMap.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry<String, DTag> entry = it.next();
//            if(!allTags.containsKey(entry.getKey())){
//                it.remove();
//            }
//        }
        tagMap.entrySet().removeIf(entry -> !allTags.containsKey(entry.getKey()));

    }


    /**
     * Return list of tags.
     * @return list
     */
    public List<DTag> getTags() {
        return new ArrayList<>(tagMap.values());
    }



    /**
     * Return a list of active alerts filtered by severity.
     * @param severity severity
     * @return list
     */
    public List<DEvent> getActiveAlarmsList(String severity) {
        List<DEvent> list = activeAlerts.values().stream()
                .filter(notif -> notif.getSeverity().equals(severity))
                .collect(Collectors.toList());
        return list;

    }

    /**
     * Return a list of targets (instances) from Prometheus.
     * @return list
     */
    public List<Target> getTargets() {

        try {
            PrometheusApi api = new PrometheusApi();
            List<PTarget> pTargets = api.targets();
            Map<String, Target> targetsMap = new HashMap<String, Target>();

            // convert from PTarget to Target
            for (PTarget pTarget : pTargets) {
                String host = pTarget.getLabels().get("instance");
                Target t = targetsMap.getOrDefault(host, new Target());
                t.setSmartTarget(false);
                t.setHealth(pTarget.getHealth());
                t.setHostname(host);
                t.setJob(pTarget.getLabels().get("job"));
                t.setId(MD5.getChecksum("host" + t.getHostname() + t.getJob()));
                // load active alerts
                for (DEvent n : getActiveAlerts().values()) {
                    if (n.getInstance().equals(host)) t.addAlert(n);
                }
                targetsMap.put(t.getId(), t);
            }

            return new ArrayList<>(targetsMap.values());

        } catch (Exception e) {
            LogFactory.getLogger().error("DAO: failed getting targets; root cause: " + e.getMessage());
        }

        return null;
    }

    // the only difference is stripped hostname
    public List<Target> getSmartTargets() {

        try {
            PrometheusApi api = new PrometheusApi();
            List<PTarget> pTargets = api.targets();
            Map<String, Target> targetsMap = new HashMap<String, Target>();

            // convert from PTarget to Target
            for (PTarget pTarget : pTargets) {
                String host = Formatter.stripInstance(pTarget.getLabels().get("instance"));
                Target t = targetsMap.getOrDefault(host, new Target());
                t.setSmartTarget(true);
                boolean up = false;
                if (pTarget.getHealth().equalsIgnoreCase("up")) up = true;
                t.setUp(up || t.isUp());
                t.setHostname(host);
                t.setId(MD5.getChecksum("smarthost" + t.getHostname()));
                // load active alerts
                for (DEvent n : getActiveAlerts().values()) {
                    if (n.getHostname().equals(host)) t.addAlert(n);
                }
                targetsMap.put(host, t);
            }

            return new ArrayList<>(targetsMap.values());

        } catch (PrometheusApiException e) {
            LogFactory.getLogger().error("DAO: failed getting targets; root cause: " + e.getMessage());
        }

        return null;

    }

    public Target getSingleTarget(String id) {
        List<Target> t1 = getTargets();
        for (Target t : t1) {
            if (t.getId().equals(id)) return t;
        }
        List<Target> t2 = getSmartTargets();
        for (Target t : t2) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }



    private List<Target> getTargetsFromProm() {

        try {
            PrometheusApi api = new PrometheusApi();
            List<PTarget> targets = api.targets();

            for (PTarget pTarget : targets) {
                Target t = new Target();
                t.setHostname(Formatter.stripInstance(pTarget.getDiscoveredLabels().get("__address__")));
                t.setId(MD5.getChecksum("host" + t.getHostname()));
            }

            // TODO

        } catch (Exception e) {
            LogFactory.getLogger().error("Exception getting targets", e);
        }

        return new ArrayList<>();
    }


    public double calculateAlertsBalanceFactor() {
        if (activeAlerts.size() == 0) return 0;
        double d = (5 * getActiveAlarmsList("critical").size()
                + 4 * getActiveAlarmsList("major").size()
                + 3 * getActiveAlarmsList("minor").size()
                + 2 * getActiveAlarmsList("warning").size()) * 1.0 / activeAlerts.size();
        return d;
    }

//    public String getLocalIpAddress() {
//        if (localIpAddress != null) return localIpAddress;
//        try {
//            localIpAddress = InetAddress.getLocalHost().getHostAddress();
//        } catch (UnknownHostException e) {
//            localIpAddress = "UnknownHost";
//        }
//        return localIpAddress;
//    }


    public void addWarning(String msgId, String msg) {
        warnings.put(msgId, msg);
    }

    public void removeWarning(String msgId) {
        warnings.remove(msgId);
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings.values());
    }

}
