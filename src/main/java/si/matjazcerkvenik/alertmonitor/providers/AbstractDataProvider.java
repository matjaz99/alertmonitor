package si.matjazcerkvenik.alertmonitor.providers;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.*;
import si.matjazcerkvenik.alertmonitor.model.config.ProviderConfig;
import si.matjazcerkvenik.alertmonitor.util.*;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractDataProvider {

    protected SimpleLogger logger = LogFactory.getLogger();

    protected ProviderConfig providerConfig;

    /** Map of active alerts. Key is correlation id */
    protected Map<String, DEvent> activeAlerts = new HashMap<>();

    /** Map of tags of active alerts. Key is the tag name */
    protected Map<String, DTag> tagMap = new HashMap<>();

    protected Timer syncTimer = null;

    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public void setProviderConfig(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    public abstract void processIncomingEvent(WebhookMessage m);

    public List<WebhookMessage> getWebhookMessages() {
        return DAO.getInstance().getDataManager().getWebhookMessages();
    }

    /**
     * Add new notification to journal. Also delete oldest notifications.
     * @param events notifications
     */
    public void addToJournal(List<DEvent> events) {
        DAO.getInstance().getDataManager().addToJournal(events);
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
        return DAO.getInstance().getDataManager().getJournal();
    }

    public long getJournalSize() {
        return DAO.getInstance().getDataManager().getJournalSize();
    }

    public DEvent getEvent(String id) {
        DEvent event = DAO.getInstance().getDataManager().getEvent(id);
        return event;
    }

    public boolean synchronizeAlerts(List<DEvent> alertList, boolean sync) {

        if (sync) {

            // set flag toBeDeleted=true for all active alerts before executing sync
            for (DEvent e : activeAlerts.values()) {
                e.setToBeDeleted(true);
            }

            List<DEvent> newAlerts = new ArrayList<>();
            int newAlertsCount = 0;

            for (DEvent e : alertList) {
                if (activeAlerts.containsKey(e.getCorrelationId())) {
                    logger.info("SYNC: Alert exists: {uid=" + e.getUid() + ", cid=" + e.getCorrelationId() + ", alertname=" + e.getAlertname() + ", instance=" + e.getInstance() + "}");
                    activeAlerts.get(e.getCorrelationId()).setToBeDeleted(false);
                } else {
                    logger.info("SYNC: New alert: {uid=" + e.getUid() + ", cid=" + e.getCorrelationId() + ", alertname=" + e.getAlertname() + ", instance=" + e.getInstance() + "}");
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

            // remove active alerts which were not received (toBeDeleted=true)
            for (String cid : cidToDelete) {
                logger.info("SYNC: Removing active alert: {cid=" + cid + "}");
                removeActiveAlert(activeAlerts.get(cid));
            }
            addToJournal(newAlerts);

            logger.info("SYNC: total sync alerts count: " + alertList.size());
            logger.info("SYNC: new alerts count: " + newAlertsCount);
            logger.info("SYNC: alerts to be deleted: " + cidToDelete.size());

            return true;
        }


        // regular alarms

        for (DEvent e : alertList) {

            if (AmProps.ALERTMONITOR_KAFKA_ENABLED) KafkaClient.getInstance().publish(AmProps.ALERTMONITOR_KAFKA_TOPIC, Formatter.toJson(e));

            // correlation
            if (activeAlerts.containsKey(e.getCorrelationId())) {
                if (e.getSeverity().equalsIgnoreCase(DSeverity.CLEAR)) {
                    removeActiveAlert(activeAlerts.get(e.getCorrelationId()));
                    logger.info("SYNC: clear alert: uid=" + e.getUid() + ", cid=" + e.getCorrelationId() + ", alertName: " + e.getAlertname());
                } else {
                    updateActiveAlert(e);
                    logger.info("SYNC: updating alert: uid=" + e.getUid() + ", counter=" + e.getCounter() + ", cid=" + e.getCorrelationId() + ", alertName: " + e.getAlertname());
                }
            } else {
                if (!e.getSeverity().equalsIgnoreCase(DSeverity.CLEAR)) {
                    e.setFirstTimestamp(e.getTimestamp());
                    e.setLastTimestamp(e.getTimestamp());
                    addActiveAlert(e);
                    logger.info("SYNC: new alert: uid=" + e.getUid() + ", cid=" + e.getCorrelationId() + ", alertName: " + e.getAlertname());
                }
            }

        }

        addToJournal(alertList);

        return true;

    }

    /**
     * Add new alert to active alerts. This method is called when first alert
     * of this type occurs (according to correlationId). First and last timestamps
     * are set to time of reception (timestamp). Also, new tags are added to tagMap.
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
        newEvent.setCounter(existingEvent.getCounter() + 1);
        // replace existing with new one
        activeAlerts.put(newEvent.getCorrelationId(), newEvent);
    }

    /**
     * Clear arrived and active alert must be removed. Before removing,
     * all alerts in journal must have clearTimestamp corrected to a point of clear event.
     * @param activeAlert
     */
    public void removeActiveAlert(DEvent activeAlert) {

        // create artificial clear event
        DEvent clearEvent = activeAlert.generateClearEvent();
        clearEvent.setClearUid(clearEvent.getUid());

        activeAlert.setFirstTimestamp(activeAlert.getTimestamp());
        activeAlert.setLastTimestamp(clearEvent.getTimestamp());
        DAO.getInstance().getDataManager().handleAlarmClearing(clearEvent);
        activeAlerts.remove(activeAlert.getCorrelationId());
        removeObsoleteTags();

        // add clear event to journal, just as any other alert
        List<DEvent> list = new ArrayList<>();
        list.add(clearEvent);
        addToJournal(list);

        AmMetrics.clearingEventCount++;
        LogFactory.getAlertLog().write(activeAlert.toString());
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

    public double calculateAlertsBalanceFactor() {
        if (activeAlerts.size() == 0) return 0;
        double d = (5 * getActiveAlarmsList("critical").size()
                + 4 * getActiveAlarmsList("major").size()
                + 3 * getActiveAlarmsList("minor").size()
                + 2 * getActiveAlarmsList("warning").size()) * 1.0 / activeAlerts.size();
        return d;
    }

    /**
     * Return a map of active alerts
     * @return map of all active alerts
     */
    public Map<String, DEvent> getActiveAlerts() {
        return activeAlerts;
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
     * Return list of tags.
     * @return list
     */
    public List<DTag> getTags() {
        return new ArrayList<>(tagMap.values());
    }

    public abstract List<Target> getTargets();

    public abstract List<Target> getSmartTargets();

    public abstract Target getSingleTarget(String id);

    /**
     * Get instances with active alerts.
     * @return list of instances
     */
    public List<String> getActiveTargets() {
        Map<String, Target> map = new HashMap<>();
        for (DEvent e : activeAlerts.values()) {
            map.put(e.getInstance(), null);
        }
        return new ArrayList<>(map.keySet());
    }

    public abstract void restartSyncTimer();

}
