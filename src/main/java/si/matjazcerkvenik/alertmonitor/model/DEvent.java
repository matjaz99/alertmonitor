/*
   Copyright 2021 Matjaž Cerkvenik

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package si.matjazcerkvenik.alertmonitor.model;

import si.matjazcerkvenik.alertmonitor.util.MD5;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DEvent implements Cloneable {
	
	/** Unique ID of notification */
	private String uid;
	
	/** Correlation ID identifies the same type of events */
	private String correlationId;
	
	/** Timestamp of first occurrence */
	private long timestamp;
	
	/** Counter of identical events (according to NID) */
	private int counter = 1;

	/** Timestamp of first occurrence */
	private long firstTimestamp;

	/** Timestamp of last occurrence */
	private long lastTimestamp;

	/** Timestamp when alert was cleared */
	private long clearTimestamp;
	
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

	/** Stripped hostname. Instance without protocol and port, resolved to IP address */
	private String hostname;
	
	/** Severity of notification */
	private String severity;
	
	/** Urgency of notification */
	private String priority;

	/** UID of event that cleared the alert */
	private String clearUid = "-";

	/** Comma-separated list of custom tags (labels) */
	private String tags;

	/** Additional description of notification */
	private String description;

	private String group;
	private String eventType = "5";
	private String probableCause = "1024";
	private String currentValue;
	private String url;

	/** Status of alert: firing or resolved */
	private String status;

	private String generatorUrl;

	/** A helping flag to mark alert to be deleted after resync */
	private boolean toBeDeleted = false;

	/** Other labels, such as external_labels or custom labels in alerts. */
	private Map<String, String> otherLabels;

	/** A rule that caused this notification */
	private String ruleExpression = "";
	private String ruleTimeLimit;

	/** Supported labels */
	public static final String KEY_ALERTNAME = "alertname";
	public static final String KEY_INFO = "info";
	public static final String KEY_INSTANCE = "instance";
	public static final String KEY_NODENAME = "nodename";
	public static final String KEY_JOB = "job";
	public static final String KEY_TAGS = "tags";
	public static final String KEY_SEVERITY = "severity";
	public static final String KEY_PRIORITY = "priority";
	public static final String KEY_GROUP = "group";
	public static final String KEY_EVENTTYPE = "eventType";
	public static final String KEY_PROBABLECAUSE = "probableCause";
	public static final String KEY_URL = "url";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_CURRENTVALUE = "currentValue";


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
	public String getFormatedFirstTimestamp() {
		return DAO.getInstance().getFormatedTimestamp(firstTimestamp);
	}
	public String getFormatedLastTimestamp() {
		return DAO.getInstance().getFormatedTimestamp(lastTimestamp);
	}
	public String getFormatedClearTimestamp() {
		return DAO.getInstance().getFormatedTimestamp(clearTimestamp);
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public long getFirstTimestamp() {
		return firstTimestamp;
	}

	public void setFirstTimestamp(long firstTimestamp) {
		this.firstTimestamp = firstTimestamp;
	}

	public long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public long getClearTimestamp() {
		return clearTimestamp;
	}

	public void setClearTimestamp(long clearTimestamp) {
		this.clearTimestamp = clearTimestamp;
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

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
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

	public String getClearUid() {
		return clearUid;
	}

	public void setClearUid(String clearUid) {
		this.clearUid = clearUid;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
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

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
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

	public String getGeneratorUrl() {
		return generatorUrl;
	}

	public void setGeneratorUrl(String generatorUrl) {
		this.generatorUrl = generatorUrl;
	}

	public boolean isToBeDeleted() {
		return toBeDeleted;
	}

	public void setToBeDeleted(boolean toBeDeleted) {
		this.toBeDeleted = toBeDeleted;
	}

	public String getRuleExpression() {
		return ruleExpression;
	}

	public void setRuleExpression(String ruleExpression) {
		this.ruleExpression = ruleExpression;
	}

	public String getRuleTimeLimit() {
		return ruleTimeLimit;
	}

	public void setRuleTimeLimit(String ruleTimeLimit) {
		this.ruleTimeLimit = ruleTimeLimit;
	}

	public Map<String, String> getOtherLabels() {
		return otherLabels;
	}

	public void setOtherLabels(Map<String, String> labels) {
		if (otherLabels == null) otherLabels = new HashMap<>();
		otherLabels = labels;
		otherLabels.remove(KEY_ALERTNAME);
		otherLabels.remove(KEY_INFO);
		otherLabels.remove(KEY_INSTANCE);
		otherLabels.remove(KEY_NODENAME);
		otherLabels.remove(KEY_JOB);
		otherLabels.remove(KEY_TAGS);
		otherLabels.remove(KEY_SEVERITY);
		otherLabels.remove(KEY_PRIORITY);
		otherLabels.remove(KEY_GROUP);
		otherLabels.remove(KEY_EVENTTYPE);
		otherLabels.remove(KEY_PROBABLECAUSE);
		otherLabels.remove(KEY_URL);
		otherLabels.remove(KEY_DESCRIPTION);
	}

	public void generateUID() {
		uid = MD5.getChecksum(timestamp
				+ this.hashCode()
				+ new Random().nextInt(Integer.MAX_VALUE)
				+ priority
				+ new Random().nextInt(Integer.MAX_VALUE)
				+ alertname
				+ new Random().nextInt(Integer.MAX_VALUE)
				+ info
				+ new Random().nextInt(Integer.MAX_VALUE)
				+ group
				+ new Random().nextInt(Integer.MAX_VALUE)
				+ instance
				+ new Random().nextInt(Integer.MAX_VALUE)
				+ description
				+ new Random().nextInt(Integer.MAX_VALUE)
				+ source
				+ new Random().nextInt(Integer.MAX_VALUE)
				+ userAgent
				+ new Random().nextInt(Integer.MAX_VALUE)
				+ severity
				+ new Random().nextInt(Integer.MAX_VALUE)
				+ tags);
	}

	public void generateCID() {
		correlationId = MD5.getChecksum(alertname + info + instance + job);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public String toString() {
		return "DEvent{" +
				"uid='" + uid + '\'' +
				", correlationId='" + correlationId + '\'' +
				", timestamp=" + timestamp +
				", counter=" + counter +
				", firstTimestamp=" + firstTimestamp +
				", lastTimestamp=" + lastTimestamp +
				", clearTimestamp=" + clearTimestamp +
				", source='" + source + '\'' +
				", userAgent='" + userAgent + '\'' +
				", alertname='" + alertname + '\'' +
				", info='" + info + '\'' +
				", job='" + job + '\'' +
				", instance='" + instance + '\'' +
				", nodename='" + nodename + '\'' +
				", hostname='" + hostname + '\'' +
				", severity='" + severity + '\'' +
				", priority='" + priority + '\'' +
				", clearUid='" + clearUid + '\'' +
				", tags='" + tags + '\'' +
				", description='" + description + '\'' +
				", group='" + group + '\'' +
				", eventType='" + eventType + '\'' +
				", probableCause='" + probableCause + '\'' +
				", currentValue='" + currentValue + '\'' +
				", url='" + url + '\'' +
				", status='" + status + '\'' +
				", generatorUrl='" + generatorUrl + '\'' +
				", otherLabels=" + otherLabels +
				", ruleExpression='" + ruleExpression + '\'' +
				", ruleTimeLimit='" + ruleTimeLimit + '\'' +
				'}';
	}

}
