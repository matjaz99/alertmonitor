package si.matjazcerkvenik.alertmonitor.data;

import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class InMemoryDataManager implements IDataManager {

    private static SimpleLogger logger = LogFactory.getLogger();

    public InMemoryDataManager() {
        logger.info("InMemoryDataManager initialized");
    }

    /** List of webhook messages in its raw form. */
    private List<WebhookMessage> webhookMessages = new LinkedList<>();

    /** Journal of events, limited by JOURNAL_TABLE_SIZE */
    private List<DEvent> journal = new LinkedList<>();

    @Override
    public void addWebhookMessage(WebhookMessage message) {
        webhookMessages.add(message);
    }

    @Override
    public List<WebhookMessage> getWebhookMessages() {
        return webhookMessages;
    }

    @Override
    public void addToJournal(List<DEvent> events) {
        journal.addAll(events);
        for (DEvent e : events) {
            LogFactory.getLogger().info("InMemoryDataManager: adding to journal: uid=" + e.getUid());
        }

    }

    @Override
    public List<DEvent> getJournal() {
        return journal;
    }

    @Override
    public long getJournalSize() {
        return journal.size();
    }

    @Override
    public DEvent getEvent(String id) {
        for (DEvent n : journal) {
            if (n.getUid().equals(id)) {
                return n;
            }
        }
        return null;
    }

    @Override
    public void cleanDB() {
        for (Iterator<WebhookMessage> it = webhookMessages.iterator(); it.hasNext();) {
            if (it.next().getTimestamp() < (System.currentTimeMillis() - 24 * 3600 * 1000)) {
                it.remove();
            }
        }
        for (Iterator<DEvent> it = journal.iterator(); it.hasNext();) {
            if (it.next().getTimestamp() < (System.currentTimeMillis() - AmProps.ALERTMONITOR_DATA_RETENTION_DAYS * 24 * 3600 * 1000)) {
                it.remove();
            }
        }
    }

    @Override
    public void handleAlarmClearing(DEvent event) {
        for (DEvent jEvent : journal) {
            if (jEvent.getCorrelationId().equals(event.getCorrelationId())
                    && jEvent.getClearTimestamp() == 0) {
                jEvent.setClearTimestamp(event.getTimestamp());
                jEvent.setClearUid(event.getUid());
            }
        }
    }
}
