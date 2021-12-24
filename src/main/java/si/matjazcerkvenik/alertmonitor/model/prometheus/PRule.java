package si.matjazcerkvenik.alertmonitor.model.prometheus;

import java.util.Map;

public class PRule {

    private String state;
    private String name;
    private String query;
    private String duration;
    private Map<String, String> labels;
    private Map<String, String> annotations;
    private String health;
    private Double evaluationTime;
    private String lastEvaluation;
    private String type;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public Double getEvaluationTime() {
        return evaluationTime;
    }

    public void setEvaluationTime(Double evaluationTime) {
        this.evaluationTime = evaluationTime;
    }

    public String getLastEvaluation() {
        return lastEvaluation;
    }

    public void setLastEvaluation(String lastEvaluation) {
        this.lastEvaluation = lastEvaluation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
