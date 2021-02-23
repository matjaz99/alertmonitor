package si.matjazcerkvenik.alertmonitor.model.alertmanager;

public class AmPSyncMessage {

    private String status;
    private AmPSyncData data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AmPSyncData getData() {
        return data;
    }

    public void setData(AmPSyncData data) {
        this.data = data;
    }
}
