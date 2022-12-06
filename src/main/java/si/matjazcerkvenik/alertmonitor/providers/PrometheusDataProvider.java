package si.matjazcerkvenik.alertmonitor.providers;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerProcessor;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AmAlertMessage;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

import java.util.List;

public class PrometheusDataProvider extends AbstractDataProvider {



    @Override
    public void processIncomingEvent(WebhookMessage m) {

        if (providerConfig.getSource().equalsIgnoreCase("prometheus")) {
            try {
                AmAlertMessage amAlertMessage = AlertmanagerProcessor.processWebhookMessage(m);
                List<DEvent> eventList = AlertmanagerProcessor.convertToDevent(m, amAlertMessage);
                DAO.getInstance().synchronizeAlerts(eventList, false);
                AmMetrics.amMessagesReceivedCount++;
                AmMetrics.lastEventTimestamp = System.currentTimeMillis();
            } catch (Exception e) {
                LogFactory.getLogger().error("DataProvider: processIncomingEvent(): unable to process incoming message: \n" + m.toString());
                LogFactory.getLogger().error("DataProvider: processIncomingEvent(): error: " + e.getMessage());
            }
        } else {
            LogFactory.getLogger().warn("DataProvider: processIncomingEvent(): unknown type: " + providerConfig.getSource());
        }

    }

}
