package si.matjazcerkvenik.alertmonitor.providers;

import si.matjazcerkvenik.alertmonitor.model.config.ProviderConfig;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

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

//    public boolean synchronizeAlerts(List<DEvent> alertList, boolean sync);

    public abstract void restartSyncTimer();

}
