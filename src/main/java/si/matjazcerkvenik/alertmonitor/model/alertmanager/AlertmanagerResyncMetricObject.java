package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import java.util.List;

public class AlertmanagerResyncMetricObject {

    private AlertmanagerResyncAlert metric;
    private List<Object> value;

    public AlertmanagerResyncAlert getMetric() {
        return metric;
    }

    public void setMetric(AlertmanagerResyncAlert metric) {
        this.metric = metric;
    }

    public List<Object> getValue() {
        return value;
    }

    public void setValue(List<Object> value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "AlertmanagerResyncMetricObject{" +
                "metric=" + metric +
                ", value=" + value +
                '}';
    }
}

