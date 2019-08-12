package si.matjazcerkvenik.alertmonitor.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DNotification {
	
	/** Unique ID of notification */
	private String uid;
	
	/** Alert ID identifies the same events */
	private String alertId;
	
	/** timestamp of first occurence */
	private long timestamp;
	
	/** Counter of identical events (according to NID) */
	private int counter = 1;
	
	/** timestamp of last occurrence */
	private long lastTimestamp;
	
	/** Source IP who sent the notification */
	private String source;
	
	/** user-agent from http header */
	private String userAgent;
	private String sourceinfo;
	private String alertname;
	private String job;
	
	/** IP address of node */
	private String instance;
	
	/** Name or hostname of node */
	private String nodename;
	
	/** Severity of notification */
	private String severity;
	
	/** Urgency of notification */
	private String priority;
	private String tags;
	private String summary;
	private String description;
	private String status;
	
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getAlertId() {
		return alertId;
	}

	public void setAlertId(String alertId) {
		this.alertId = alertId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getFormatedTimestamp() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		String format = "yyyy/MM/dd H:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(cal.getTime());
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

	public String getSourceinfo() {
		return sourceinfo;
	}

	public void setSourceinfo(String sourceinfo) {
		this.sourceinfo = sourceinfo;
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
	
}