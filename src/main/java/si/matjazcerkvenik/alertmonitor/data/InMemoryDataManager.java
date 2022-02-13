package si.matjazcerkvenik.alertmonitor.data;

import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PRule;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiException;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryDataManager implements IDataManager {

    /** List of webhook messages in its raw form. */
    private List<WebhookMessage> webhookMessages = new LinkedList<>();

    /** Journal of events, limited by JOURNAL_TABLE_SIZE */
    private List<DEvent> journal = new LinkedList<>();

    @Override
    public void addWebhookMessage(WebhookMessage message) {
        // webhook messages can be 1% of journal size
        while (webhookMessages.size() > AmProps.JOURNAL_TABLE_SIZE / 100) {
            webhookMessages.remove(0);
        }
        webhookMessages.add(message);
    }

    @Override
    public List<WebhookMessage> getWebhookMessages() {
        return webhookMessages;
    }

    @Override
    public void addToJournal(List<DEvent> events) {
        while (journal.size() > AmProps.JOURNAL_TABLE_SIZE) {
            DEvent m = journal.remove(0);
            LogFactory.getLogger().info("Purging journal: " + m.getUid());
        }
        journal.addAll(events);
        for (DEvent e : events) {
            LogFactory.getLogger().info("Adding to journal: " + e.getUid());
        }

    }

    @Override
    public List<DEvent> getJournal() {
        return journal;
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
        // not applicable
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
