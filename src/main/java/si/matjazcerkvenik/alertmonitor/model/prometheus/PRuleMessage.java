package si.matjazcerkvenik.alertmonitor.model.prometheus;

public class PRuleMessage {

    private String status;
    private PRuleData data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PRuleData getData() {
        return data;
    }

    public void setData(PRuleData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PRuleMessage{" +
                "status='" + status + '\'' +
                ", data=" + data +
                '}';
    }
}
