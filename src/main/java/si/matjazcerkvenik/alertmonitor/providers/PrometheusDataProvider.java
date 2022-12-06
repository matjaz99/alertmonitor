package si.matjazcerkvenik.alertmonitor.providers;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerProcessor;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AmAlertMessage;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

import java.util.List;
import java.util.Timer;

public class PrometheusDataProvider extends AbstractDataProvider {

    private PrometheusSyncTask prometheusSyncTask = null;

    public PrometheusDataProvider() {
        restartSyncTimer();
    }

    @Override
    public void processIncomingEvent(WebhookMessage m) {

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

    }

    @Override
    public void restartSyncTimer() {

        stopSyncTimer();

        if (AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC == 0) {
            LogFactory.getLogger().info("Sync is disabled");
        }
        // TODO fix configuration according to provider config

        // start resync timer
        if (prometheusSyncTask == null) {
            LogFactory.getLogger().info("Start periodic sync task with period=" + AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC);
            AmMetrics.alertmonitor_psync_interval_seconds.set(AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC);
            if (AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC > 0) {
                syncTimer = new Timer("SyncTimer");
                prometheusSyncTask = new PrometheusSyncTask();
                syncTimer.schedule(prometheusSyncTask, 5 * 1000, AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC * 1000);
            }
        }

    }

    public void stopSyncTimer() {
        if (syncTimer != null) {
            syncTimer.cancel();
            syncTimer = null;
        }
        if (prometheusSyncTask != null) {
            prometheusSyncTask.cancel();
            prometheusSyncTask = null;
        }
    }

}
