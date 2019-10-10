package si.matjazcerkvenik.alertmonitor.model;

import java.util.ArrayList;
import java.util.List;

public class Target {

    private String id;
    private String hostname;
    private List<DNotification> alerts = new ArrayList<DNotification>();

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
        return alerts;
    }

    public void setAlerts(List<DNotification> alerts) {
        this.alerts = alerts;
    }

    public void addAlert(DNotification notification) {
        alerts.add(notification);
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
