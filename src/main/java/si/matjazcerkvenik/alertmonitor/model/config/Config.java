package si.matjazcerkvenik.alertmonitor.model.config;

import java.util.List;

public class Config {

    private String version;

    private List<ProviderConfig> providers;


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public List<ProviderConfig> getProviders() {
        return providers;
    }

    public void setProviders(List<ProviderConfig> providers) {
        this.providers = providers;
    }

    @Override
    public String toString() {
        return "Config{" +
                "providers=" + providers +
                '}';
    }
}
