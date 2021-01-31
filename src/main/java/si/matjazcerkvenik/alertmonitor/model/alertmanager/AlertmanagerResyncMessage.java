package si.matjazcerkvenik.alertmonitor.model.alertmanager;

public class AlertmanagerResyncMessage {

    private String status;
    private AlertmanagerResyncDataObject data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AlertmanagerResyncDataObject getData() {
        return data;
    }

    public void setData(AlertmanagerResyncDataObject data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "AlertmanagerResyncMessage{" +
                "status='" + status + '\'' +
                ", data=" + data +
                '}';
    }
}
