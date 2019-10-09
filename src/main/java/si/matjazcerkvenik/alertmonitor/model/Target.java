package si.matjazcerkvenik.alertmonitor.model;

import java.util.ArrayList;
import java.util.List;

public class Target {

    private String id;
    private String instance;
    private List<DNotification> alerts = new ArrayList<DNotification>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
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
                ", instance='" + instance + '\'' +
                ", alerts=" + alerts +
                '}';
    }
}
