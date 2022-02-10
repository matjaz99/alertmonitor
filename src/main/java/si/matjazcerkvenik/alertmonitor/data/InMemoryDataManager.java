package si.matjazcerkvenik.alertmonitor.data;

import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.util.LinkedList;
import java.util.List;

public class InMemoryDataManager implements IDataManager {

    /** List of webhook messages in its raw form. */
    private List<WebhookMessage> webhookMessages = new LinkedList<>();

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
    public void addToJournal(DEvent event) {

    }

    @Override
    public List<DEvent> getJournal() {
        return null;
    }

    @Override
    public DEvent getEvent(String id) {
        return null;
    }
}
