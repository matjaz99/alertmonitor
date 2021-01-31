package si.matjazcerkvenik.alertmonitor.model.alertmanager;

public class AlertmanagerResyncAlert {

    private String alertname;
    private String severity;

    public String getAlertname() {
        return alertname;
    }

    public void setAlertname(String alertname) {
        this.alertname = alertname;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return "AlertmanagerResyncAlert{" +
                "alertname='" + alertname + '\'' +
                ", severity='" + severity + '\'' +
                '}';
    }

}

