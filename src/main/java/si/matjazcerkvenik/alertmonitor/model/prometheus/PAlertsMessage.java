package si.matjazcerkvenik.alertmonitor.model.prometheus;

public class PAlertsMessage {

    private String status;
    private PAlertsData data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PAlertsData getData() {
        return data;
    }

    public void setData(PAlertsData data) {
        this.data = data;
    }
}
