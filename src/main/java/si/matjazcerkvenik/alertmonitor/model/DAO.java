package si.matjazcerkvenik.alertmonitor.model;

import si.matjazcerkvenik.alertmonitor.webhook.RawHttpMessage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DAO {

    public static long startUpTime = System.currentTimeMillis();

    private static DAO instance;
    private static int RAW_MSG_TABLE_SIZE = 5000;
    private static int JOURNAL_TABLE_SIZE = 5000;

    private List<RawHttpMessage> rawMessages = new LinkedList<RawHttpMessage>();
    private List<DNotification> journal = new LinkedList<DNotification>();
    private Map<String, DNotification> activeAlerts = new HashMap<String, DNotification>();

    public static int rawMessagesReceivedCount = 0;
    public static int amMessagesReceivedCount = 0;
    public static int journalReceivedCount = 0;
    public static int alertEventCount = 0;
    public static int clearEventCount = 0;

    private Map<String, DTag> tagMap = new HashMap<String, DTag>();

    private DAO() {
    }

    public static DAO getInstance() {
        if (instance == null) instance = new DAO();
        return instance;
    }

    public void addRawMessage(RawHttpMessage message) {
        while (rawMessages.size() > RAW_MSG_TABLE_SIZE) {
            RawHttpMessage m = rawMessages.remove(0);
            if (m != null) System.out.println("Removing rawMsg");
            if (m == null) System.out.println("Couldnt remove rawMsg");
        }
        rawMessages.add(message);
    }

    public List<RawHttpMessage> getRawMessages() {
        return rawMessages;
    }

    public List<DNotification> getJournal() {
        return journal;
    }

    public void addToJournal(List<DNotification> notifList) {
        while (journal.size() > JOURNAL_TABLE_SIZE) {
            DNotification m = journal.remove(0);
            if (m != null) System.out.println("Removing notif");
            if (m == null) System.out.println("Couldnt remove notif");
        }
        this.journal.addAll(notifList);
    }

    public Map<String, DNotification> getActiveAlerts() {
        return activeAlerts;
    }

    public void addActiveAlert(DNotification n) {
        activeAlerts.put(n.getAlertId(), n);
    }

    public void updateActiveAlert(DNotification newNotif) {

        activeAlerts.get(newNotif.getAlertId()).setLastTimestamp(newNotif.getTimestamp());
        int c = activeAlerts.get(newNotif.getAlertId()).getCounter();
        activeAlerts.get(newNotif.getAlertId()).setCounter(c + 1);
    }

    public void removeActiveAlert(DNotification n) {
        activeAlerts.remove(n.getAlertId());
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
}
