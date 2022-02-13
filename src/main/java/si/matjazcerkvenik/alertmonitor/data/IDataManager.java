package si.matjazcerkvenik.alertmonitor.data;

import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.util.List;

public interface IDataManager {

    public void addWebhookMessage(WebhookMessage message);

    public List<WebhookMessage> getWebhookMessages();

    public void addToJournal(List<DEvent> event);

    public List<DEvent> getJournal();

    public DEvent getEvent(String id);

    public void cleanDB();

    public void handleAlarmClearing(DEvent event);

}
