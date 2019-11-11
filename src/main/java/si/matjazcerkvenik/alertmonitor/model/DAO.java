package si.matjazcerkvenik.alertmonitor.model;

import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.MD5Checksum;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DAO {

    private static SimpleLogger logger = null;

    public static long startUpTime = 0;
    public static String version = "n/a";
    public static boolean isContainerized = false;
    public static String DATE_FORMAT = "yyyy/MM/dd H:mm:ss";

    private static DAO instance;
    private static int WEBHOOK_TABLE_SIZE = 5000;
    private static int JOURNAL_TABLE_SIZE = 5000;

    private List<WebhookMessage> webhookMessages = new LinkedList<WebhookMessage>();
    private List<DNotification> journal = new LinkedList<DNotification>();
    private Map<String, DNotification> activeAlerts = new HashMap<String, DNotification>();

    public static int webhookMessagesReceivedCount = 0;
    public static int amMessagesReceivedCount = 0;
    public static int journalReceivedCount = 0;
    // TODO remove these two, it can be calculated from number of alerts by severity
    public static int raisingEventCount = 0;
    public static int clearingEventCount = 0;
    public static long lastEventTimestamp = 0;

    private Map<String, DTag> tagMap = new HashMap<String, DTag>();

    private String localIpAddress;

    private DAO() {
    }

    public static DAO getInstance() {
        if (instance == null) instance = new DAO();
        return instance;
    }

    public static SimpleLogger getLogger() {
        if (logger == null) {
            logger = new SimpleLogger();
            if (logger.getFilename().contains("simple-logger.log")) {
                logger.setFilename("./alertmonitor.log");
            }
        }
        return logger;
    }

    /**
     * Add new webhook message to the list. Also delete oldest messages.
     * @param message
     */
    public void addWebhookMessage(WebhookMessage message) {
        while (webhookMessages.size() > WEBHOOK_TABLE_SIZE) {
            WebhookMessage m = webhookMessages.remove(0);
        }
        webhookMessages.add(message);
        webhookMessagesReceivedCount++;
        AmMetrics.alertmonitor_webhook_messages_received_total.labels(message.getRemoteHost(), message.getMethod().toUpperCase()).inc();
    }

    public List<WebhookMessage> getWebhookMessages() {
        return webhookMessages;
    }

    /**
     * Add new notification to journal. Also delete oldest notifications.
     * @param notif
     */
    public void addToJournal(DNotification notif) {
        while (journal.size() > JOURNAL_TABLE_SIZE) {
            DNotification m = journal.remove(0);
            getLogger().info("Removing from journal: " + m.getUid());
        }
        journal.add(notif);
        getLogger().info("Adding to journal: " + notif.getUid());
        journalReceivedCount++;
        AmMetrics.alertmonitor_journal_messages_total.labels(notif.getSeverity()).inc();
    }

    /**
     * Return whole journal
     * @return list
     */
    public List<DNotification> getJournal() {
        return journal;
    }

    /**
     * Get single notification from journal
     * @param id
     * @return notification
     */
    public DNotification getNotification(String id) {
        for (DNotification n : journal) {
            if (n.getUid().equals(id)) return n;
        }
        return null;
    }

    /**
     * Add new alert to active alerts. This method is called when first alert
     * of this type occurs (according to correlationId). First and last timestamps
     * are set to time of reception (timestamp). Also new tags are added to tagMap.
     * @param n
     */
    public void addActiveAlert(DNotification n) {

        n.setFirstTimestamp(n.getTimestamp());
        n.setLastTimestamp(n.getTimestamp());

        activeAlerts.put(n.getCorrelationId(), n);
        raisingEventCount++;

        // parse tags from tags label
        String[] array = n.getTags().split(",");
        for (int i = 0; i < array.length; i++) {
            String tagName = array[i].trim();
            if (tagName.length() > 1) {
                DTag t = new DTag(tagName, TagColors.getColor(tagName));
                addTag(t);
            }
        }

    }

    /**
     * Return a map of active alerts
     * @return map
     */
    public Map<String, DNotification> getActiveAlerts() {
        return activeAlerts;
    }

    /**
     * New alert has appeared and must replace existing active alert. Since this
     * is new occurrence of existing alert, the firstTimestamp is overwritten
     * by existing firstTimestamp (time when alert occurred for the first time).
     * The lastTimestamp is set to time of reception and counter is increased by 1
     * (according to existing alert).
     * Alert then finally replaces reference in activeAlert map so it points to new
     * alert.
     * @param newNotif
     */
    public void updateActiveAlert(DNotification newNotif) {
        DNotification existingNotif = activeAlerts.get(newNotif.getCorrelationId());
        // get some variables from existing alert
        newNotif.setFirstTimestamp(existingNotif.getFirstTimestamp());
        newNotif.setLastTimestamp(newNotif.getTimestamp());
        newNotif.setCounter(existingNotif.getCounter() + 1);
        // override existing alert with new alert
        activeAlerts.put(newNotif.getCorrelationId(), newNotif);
    }

    /**
     * Clear arrived and active alert must be removed. Before removing,
     * all alerts in journal have clearTimestamp corrected to point to clear event.
     * @param n
     */
    public void removeActiveAlert(DNotification n) {
        for (DNotification jNotif : journal) {
            if (jNotif.getCorrelationId().equals(n.getCorrelationId())
                    && jNotif.getClearTimestamp() == 0) {
                jNotif.setClearTimestamp(n.getTimestamp());
                jNotif.setClearUid(n.getUid());
            }
        }
        n.setFirstTimestamp(n.getTimestamp());
        n.setLastTimestamp(n.getTimestamp());
        activeAlerts.remove(n.getCorrelationId());
        removeObsoleteTags();
        clearingEventCount++;
    }

    /**
     * Remove tags which have no active alerts left.
     */
    private void removeObsoleteTags() {
        Map<String, Object> allTags = new HashMap<String, Object>();
        for (DNotification n : activeAlerts.values()) {
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
        for (String n : tagMap.keySet()) {
            if (!allTags.containsKey(n)) {
                tagMap.remove(n);
            }
        }

    }

    /**
     * Add new tag if it does not exist yet.
     * @param tag
     */
    public void addTag(DTag tag) {
        tagMap.putIfAbsent(tag.getName(), tag);
    }

    /**
     * Return list of tags.
     * @return list
     */
    public List<DTag> getTags() {
        return new ArrayList<DTag>(tagMap.values());
    }

    /**
     * Format timestamp from millis into readable form.
     * @param timestamp
     * @return readable date
     */
    public String getFormatedTimestamp(long timestamp) {
        if (timestamp == 0) return "n/a";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(cal.getTime());
    }

    /**
     * Return a list of active alerts filtered by severity.
     * @param severity
     * @return list
     */
    public List<DNotification> getActiveAlarmsList(String severity) {
        List<DNotification> list = activeAlerts.values().stream()
                .filter(notif -> notif.getSeverity().equals(severity))
                .collect(Collectors.toList());
        return list;

    }

    /**
     * Return a list of targets with active alerts.
     * @return list
     */
    public List<Target> getTargets() {
        List<DNotification> list = new ArrayList<DNotification>(getActiveAlerts().values());
        Map<String, Target> targetsMap = new HashMap<String, Target>();

        for (DNotification n : list) {
            String host = n.getHostname();
            Target t = targetsMap.getOrDefault(host, new Target());
            t.setHostname(host);
            t.addAlert(n);
            t.setId(MD5Checksum.getMd5Checksum(host));
            targetsMap.put(host, t);
        }

        return new ArrayList<>(targetsMap.values());
    }


    public double calculateAlertsBalanceFactor() {
        if (activeAlerts.size() == 0) return 0;
        double d = (5 * getActiveAlarmsList("critical").size()
                + 4 * getActiveAlarmsList("major").size()
                + 3 * getActiveAlarmsList("minor").size()
                + 2 * getActiveAlarmsList("warning").size()) * 1.0 / activeAlerts.size();
        return d;
    }

    public String getLocalIpAddress() {
        if (localIpAddress != null) return localIpAddress;
        try {
            localIpAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            localIpAddress = "UnknownHost";
        }
        return localIpAddress;
    }

}
