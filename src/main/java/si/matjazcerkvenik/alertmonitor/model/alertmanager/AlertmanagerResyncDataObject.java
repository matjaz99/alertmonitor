package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import java.util.List;

public class AlertmanagerResyncDataObject {

    private String resultType;
    private List<AlertmanagerResyncMetricObject> result;

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public List<AlertmanagerResyncMetricObject> getResult() {
        return result;
    }

    public void setResult(List<AlertmanagerResyncMetricObject> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "AlertmanagerResyncDataObject{" +
                "resultType='" + resultType + '\'' +
                ", result=" + result +
                '}';
    }
}

