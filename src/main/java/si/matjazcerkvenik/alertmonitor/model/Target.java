package si.matjazcerkvenik.alertmonitor.model;

import java.util.ArrayList;
import java.util.List;

public class Target {

    private String instance;
    private List<DNotification> alerts = new ArrayList<DNotification>();

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
}
