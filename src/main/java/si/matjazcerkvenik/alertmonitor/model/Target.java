package si.matjazcerkvenik.alertmonitor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Target {

    private String id;
    private String hostname;
    private Map<String, DNotification> alerts = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public List<DNotification> getAlerts() {
        return new ArrayList<>(alerts.values());
    }

    public void addAlert(DNotification notification) {
        if (!alerts.containsKey(notification.getUid())) alerts.put(notification.getUid(), notification);
    }

    @Override
    public String toString() {
        return "Target{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", alerts=" + alerts +
                '}';
    }
}
