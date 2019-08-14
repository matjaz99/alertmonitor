package si.matjazcerkvenik.alertmonitor.model;

import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DAO {

    public static long startUpTime = 0;

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

    private DAO() {
    }

    public static DAO getInstance() {
        if (instance == null) instance = new DAO();
        return instance;
    }

    public void addWebhookMessage(WebhookMessage message) {
        while (webhookMessages.size() > WEBHOOK_TABLE_SIZE) {
            WebhookMessage m = webhookMessages.remove(0);
        }
        webhookMessages.add(message);
    }

    public List<WebhookMessage> getWebhookMessages() {
        return webhookMessages;
    }

    public List<DNotification> getJournal() {
        return journal;
    }

    public void addToJournal(List<DNotification> notifList) {
        while (journal.size() > JOURNAL_TABLE_SIZE) {
            DNotification m = journal.remove(0);
        }
        this.journal.addAll(notifList);
    }

    public Map<String, DNotification> getActiveAlerts() {
        return activeAlerts;
    }

    public void addActiveAlert(DNotification n) {
        activeAlerts.put(n.getCorrelationId(), n);
    }

    public void updateActiveAlert(DNotification newNotif) {
        activeAlerts.get(newNotif.getCorrelationId()).setLastTimestamp(newNotif.getTimestamp());
        int c = activeAlerts.get(newNotif.getCorrelationId()).getCounter();
        activeAlerts.get(newNotif.getCorrelationId()).setCounter(c + 1);
    }

    public void removeActiveAlert(DNotification n) {
        activeAlerts.remove(n.getCorrelationId());
    }

    public void parseTags(String tags) {

        String[] array = tags.split(",");

        // TODO color tags

        for (int i = 0; i < array.length; i++) {
            String tagName = array[i].trim();
            if (tagName.length() > 0 && !tagMap.containsKey(tagName)) {
                DTag t = new DTag(tagName, "#7ab1d3");
                tagMap.put(tagName, t);
            }
        }

    }

    public String getFormatedTimestamp(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        String format = "yyyy/MM/dd H:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(cal.getTime());
    }

    public int getActiveAlarmsCount(String severity) {
        List<DNotification> list = activeAlerts.values().stream()
                .filter(notif -> notif.getSeverity().equals(severity))
                .collect(Collectors.toList());
        return list.size();

    }

    public double calculateAlertsBalanceFactor() {
        if (activeAlerts.size() == 0) return 0;
        double d = (5 * getActiveAlarmsCount("critical")
                + 4 * getActiveAlarmsCount("major")
                + 3 * getActiveAlarmsCount("minor")
                + 2 * getActiveAlarmsCount("warning")) * 1.0 / activeAlerts.size();
        return d;
    }
}
