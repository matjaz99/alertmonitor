package si.matjazcerkvenik.alertmonitor.model;

public class DWarning {

    private String severity = DWARNING_SEVERITY_WARNING;
    private String message;

    public static final String DWARNING_SEVERITY_CRITICAL = "danger";
    public static final String DWARNING_SEVERITY_WARNING = "warning";
    public static final String DWARNING_SEVERITY_CLEAR = "success";

    public DWarning(String severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    public String getIcon() {
        if (severity.equals(DWARNING_SEVERITY_CLEAR)) {
            return "pi pi-check";
        } else if (severity.equals(DWARNING_SEVERITY_CRITICAL)) {
            return "pi pi-exclamation-circle";
        } else {
            return "pi pi-exclamation-triangle";
        }
    }

    public String getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }
}
