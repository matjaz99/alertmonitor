package si.matjazcerkvenik.alertmonitor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Target {

    private String id;
    private String hostname;
    private boolean smartTarget;
    private String job;
    private boolean up;
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

    public boolean isSmartTarget() {
        return smartTarget;
    }

    public void setSmartTarget(boolean smartTarget) {
        this.smartTarget = smartTarget;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public void setHealth(String health) {
        if (health.equalsIgnoreCase("up")) {
            this.up = true;
            return;
        }
        this.up = false;
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
