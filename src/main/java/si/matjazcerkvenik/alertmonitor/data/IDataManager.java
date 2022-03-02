package si.matjazcerkvenik.alertmonitor.data;

import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

import java.util.List;

public interface IDataManager {

    public void addWebhookMessage(WebhookMessage message);

    public List<WebhookMessage> getWebhookMessages();

    public void addToJournal(List<DEvent> event);

    public List<DEvent> getJournal();

    public long getJournalSize();

    public DEvent getEvent(String id);

    public int getNumberOfAlertsInLastHour();

    public String getAlertsPerSecondInLastHour();

    public void cleanDB();

    public void handleAlarmClearing(DEvent event);

}
