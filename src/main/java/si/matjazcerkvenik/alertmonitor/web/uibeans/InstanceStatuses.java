package si.matjazcerkvenik.alertmonitor.web.uibeans;

public class InstanceStatuses {

    private String instance;
    private String job;
    private String scrapeInterval;
    private String status;
    private String downTime;
    private String availability;
    private String MTBF;

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

    public String getScrapeInterval() {
        return scrapeInterval;
    }

    public void setScrapeInterval(String scrapeInterval) {
        this.scrapeInterval = scrapeInterval;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status.equalsIgnoreCase("0")) this.status = "DOWN";
        if (status.equalsIgnoreCase("1")) this.status = "UP";
    }

    /**
     * Return red if DOWN and green if UP.
     * @return color
     */
    public String getStatusColor() {
        if (status == null) return "black";
        if (status.equals("UP")) return "green";
        return "red";
    }

    public String getDownTime() {
        return downTime;
    }

    public void setDownTime(String downTime) {
        this.downTime = downTime;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getMTBF() {
        return MTBF;
    }

    public void setMTBF(String MTBF) {
        this.MTBF = MTBF;
    }
}
