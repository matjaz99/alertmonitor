package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import java.util.Map;

public class AlertmanagerResyncAlert {

    private String alertname;
    private String alertstate;
    private String info;
    private String instance;
    private String job;
    private String priority;
    private String severity;
    private String tags;
    private String nodename;
    private String hostname;
    private String description;
    private String team;
    private String eventType;
    private String probableCause;
    private String currentValue;
    private String url;


    public String getAlertname() {
        return alertname;
    }

    public void setAlertname(String alertname) {
        this.alertname = alertname;
    }

    public String getAlertstate() {
        return alertstate;
    }

    public void setAlertstate(String alertstate) {
        this.alertstate = alertstate;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getNodename() {
        return nodename;
    }

    public void setNodename(String nodename) {
        this.nodename = nodename;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getProbableCause() {
        return probableCause;
    }

    public void setProbableCause(String probableCause) {
        this.probableCause = probableCause;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "AlertmanagerResyncAlert{" +
                "alertname='" + alertname + '\'' +
                ", alertstate='" + alertstate + '\'' +
                ", info='" + info + '\'' +
                ", instance='" + instance + '\'' +
                ", job='" + job + '\'' +
                ", priority='" + priority + '\'' +
                ", severity='" + severity + '\'' +
                ", tags='" + tags + '\'' +
                ", nodename='" + nodename + '\'' +
                ", hostname='" + hostname + '\'' +
                ", description='" + description + '\'' +
                ", team='" + team + '\'' +
                ", eventType='" + eventType + '\'' +
                ", probableCause='" + probableCause + '\'' +
                ", currentValue='" + currentValue + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}

