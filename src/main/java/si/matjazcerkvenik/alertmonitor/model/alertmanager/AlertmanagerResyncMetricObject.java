package si.matjazcerkvenik.alertmonitor.model.alertmanager;

public class AlertmanagerResyncMetricObject {

    private AlertmanagerResyncAlert metric;

    public AlertmanagerResyncAlert getMetric() {
        return metric;
    }

    public void setMetric(AlertmanagerResyncAlert metric) {
        this.metric = metric;
    }

    @Override
    public String toString() {
        return "AlertmanagerResyncMetricObject{" +
                "metric=" + metric +
                '}';
    }
}

