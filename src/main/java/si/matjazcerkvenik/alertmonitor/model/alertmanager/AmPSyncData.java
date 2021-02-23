package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import java.util.List;

public class AmPSyncData {

    private List<AmPSyncAlert> alerts;

    public List<AmPSyncAlert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AmPSyncAlert> alerts) {
        this.alerts = alerts;
    }


}
