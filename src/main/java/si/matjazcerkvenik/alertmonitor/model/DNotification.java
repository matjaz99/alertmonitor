package si.matjazcerkvenik.alertmonitor.model;

public class DNotification {
	
	/** Unique ID of notification */
	private String uid;
	
	/** Correlation ID identifies the same type of events */
	private String correlationId;
	
	/** timestamp of first occurence */
	private long timestamp;
	
	/** Counter of identical events (according to NID) */
	private int counter = 1;
	
	/** Timestamp of last occurrence */
	private long lastTimestamp;
	
	/** Source IP who sent the notification */
	private String source;
	
	/** User-agent from http header */
	private String userAgent;

	/** Name of alert */
	private String alertname;

	/** Information about alert */
	private String info;

	/** Prometheus job scraper name */
	private String job;
	
	/** IP address of node */
	private String instance;
	
	/** Name or hostname of node */
	private String nodename;
	
	/** Severity of notification */
	private String severity;
	
	/** Urgency of notification */
	private String priority;

	/** Comma-separated list of custom tags (labels) */
	private String tags;

	/** Summary description of notification */
	private String summary;

	/** Additional description of notification */
	private String description;

	private String team;
	private String eventType;
	private String probableCause;
	private String currentValue;
	private String url;

	/** Status of alert: firing or resolved */
	private String status;
	
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	// TODO put this method somewhere else
	public String getFormatedTimestamp() {
		return DAO.getInstance().getFormatedTimestamp(timestamp);
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getAlertname() {
		return alertname;
	}

	public void setAlertname(String alertname) {
		this.alertname = alertname;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getNodename() {
		return nodename;
	}

	public void setNodename(String nodename) {
		this.nodename = nodename;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
}
