package si.matjazcerkvenik.alertmonitor.data;

import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.util.List;

public interface IDataManager {

    public void addWebhookMessage(WebhookMessage message);

    public abstract List<WebhookMessage> getWebhookMessages();

    public abstract void addToJournal(DEvent event);

    public abstract List<DEvent> getJournal();

    public abstract DEvent getEvent(String id);

}
