package si.matjazcerkvenik.alertmonitor.providers;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.Target;
import si.matjazcerkvenik.alertmonitor.model.config.ProviderConfig;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PRule;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiClient;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiClientPool;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiException;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

import java.util.List;
import java.util.Timer;

public abstract class AbstractDataProvider {

    protected ProviderConfig providerConfig;

    protected Timer syncTimer = null;

    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public void setProviderConfig(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    public abstract void processIncomingEvent(WebhookMessage m);

    public List<WebhookMessage> getWebhookMessages() {
        return DAO.getInstance().getDataManager().getWebhookMessages();
    }

    public DEvent getEvent(String id) {
        DEvent event = DAO.getInstance().getDataManager().getEvent(id);
        return event;
    }

    public abstract List<Target> getTargets();

    public abstract List<Target> getSmartTargets();

    public abstract Target getSingleTarget(String id);

//    public boolean synchronizeAlerts(List<DEvent> alertList, boolean sync);

    public abstract void restartSyncTimer();

}
