package si.matjazcerkvenik.alertmonitor.model.prometheus;

public class PTargetMessage {

    private String status;
    private PTargetData data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PTargetData getData() {
        return data;
    }

    public void setData(PTargetData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PTargetMessage{" +
                "status='" + status + '\'' +
                ", data=" + data +
                '}';
    }

}
