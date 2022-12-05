package si.matjazcerkvenik.alertmonitor.model;

import si.matjazcerkvenik.alertmonitor.model.config.ProviderConfig;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

public class DataProvider {

    private ProviderConfig providerConfig;

    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public void setProviderConfig(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    public void processIncomingEvent(WebhookMessage m) {

    }

}
