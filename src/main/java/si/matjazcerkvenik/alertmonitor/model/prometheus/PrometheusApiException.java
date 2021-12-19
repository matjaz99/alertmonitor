package si.matjazcerkvenik.alertmonitor.model.prometheus;

public class PrometheusApiException extends Exception {

    public PrometheusApiException(String message) {
        super(message);
    }

    public PrometheusApiException(String message, Throwable cause) {
        super(message, cause);
    }

}
