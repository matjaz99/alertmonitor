package si.matjazcerkvenik.alertmonitor.model.prometheus;

import java.util.List;

public class PAlertsData {

    private List<PAlert> alerts;

    public List<PAlert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<PAlert> alerts) {
        this.alerts = alerts;
    }


}
