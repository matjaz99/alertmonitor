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

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import si.matjazcerkvenik.alertmonitor.util.AmDateFormat;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.MD5;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@SuppressWarnings("unused")
public class DEvent implements Cloneable {
	
	/** Unique ID of notification */
	@BsonProperty(value = "uid")
	private String uid;
	
	/** Correlation ID identifies the same type of events */
	@BsonProperty(value = "correlationId")
	private String correlationId;

	/** Labels that identify the prometheus */
	@BsonProperty(value = "prometheusId")
	private String prometheusId;

	/** Provider name who sent alert */
	@BsonProperty(value = "provider")
	private String provider;
	
	/** Provider ID who sent alert */
	@BsonProperty(value = "providerId")
	private String providerId;
	
	/** Timestamp of this occurrence */
	@BsonProperty(value = "timestamp")
	private long timestamp;
	
	/** Counter of identical events (according to NID) */
	@BsonProperty(value = "counter")
	private int counter = 0;

	/** Timestamp of first occurrence */
	@BsonProperty(value = "firstTimestamp")
	private long firstTimestamp;

	/** Timestamp when alert was cleared */
	@BsonProperty(value = "clearTimestamp")
	private long clearTimestamp;
	
	/** Source IP who sent the notification */
	@BsonProperty(value = "source")
	private String source;
	
	/** User-agent from http header */
	@BsonProperty(value = "userAgent")
	private String userAgent;

	/** Name of alert */
	@BsonProperty(value = "alertname")
	private String alertname;

	/** Information about alert */
	@BsonProperty(value = "info")
	private String info;

	/** Prometheus job scraper name */
	@BsonProperty(value = "job")
	private String job;
	
	/** IP address of node */
	@BsonProperty(value = "instance")
	private String instance;
	
	/** Name or hostname of node */
	@BsonProperty(value = "nodename")
	private String nodename;

	/** Stripped hostname. Instance without protocol and port, resolved to IP address */
	@BsonProperty(value = "hostname")
	private String hostname;
	
	/** Severity of notification */
	@BsonProperty(value = "severity")
	private String severity;
	
	/** Urgency of notification */
	@BsonProperty(value = "priority")
	private String priority;

	/** UID of event that cleared the alert */
	@BsonProperty(value = "clearUid")
	private String clearUid = "n/a";

	/** Comma-separated list of custom tags (labels) */
	@BsonProperty(value = "tags")
	private String tags;

	/** Additional description of notification */
	@BsonProperty(value = "description")
	private String description;

	@BsonProperty(value = "group")
	private String group;

	/** Event type according to ITU X.733 */
	@BsonProperty(value = "eventType")
	private String eventType = "5";

	/** Probable cause according to ITU X.733 */
	@BsonProperty(value = "probableCause")
	private String probableCause = "1024";

	/** Current metric value */
	@BsonProperty(value = "currentValue")
	private String currentValue;

	/** URL of somewhere, eg. grafana */
	@BsonProperty(value = "url")
	private String url;

	/** Status of alert: firing or resolved */
	@BsonIgnore
	private String status;

	@BsonIgnore
	private String generatorUrl;

	/** A helping flag to mark alerts to be deleted after resync */
	@BsonIgnore
	private boolean toBeDeleted = false;

	/** Other labels, such as external_labels or custom labels in alerts. */
	private Map<String, String> otherLabels;

	@BsonProperty(value = "otherLabelsString")
	private String otherLabelsString;

	/** A rule that caused this notification */
	@BsonIgnore
	private String ruleExpression = "";
	@BsonIgnore
	private String ruleTimeLimit;

	/** Supported labels */
	public static final String LBL_ALERTNAME = "alertname";
	public static final String LBL_INFO = "info";
	public static final String LBL_INSTANCE = "instance";
	public static final String LBL_NODENAME = "nodename";
	public static final String LBL_JOB = "job";
	public static final String LBL_TAGS = "tags";
	public static final String LBL_SEVERITY = "severity";
	public static final String LBL_PRIORITY = "priority";
	public static final String LBL_GROUP = "group";
	public static final String LBL_EVENTTYPE = "eventType";
	public static final String LBL_PROBABLECAUSE = "probableCause";
	public static final String LBL_URL = "url";
	public static final String LBL_DESCRIPTION = "description";
	public static final String LBL_CURRENTVALUE = "currentValue";


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

	public String getPrometheusId() {
		return prometheusId;
	}

	public void setPrometheusId(String prometheusId) {
		this.prometheusId = prometheusId;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	// TODO put this method somewhere else
	public String getFormatedTimestamp() {
		return Formatter.getFormatedTimestamp(timestamp, AmDateFormat.DATE_TIME);
	}

	public String getFormatedFirstTimestamp() {
		return Formatter.getFormatedTimestamp(firstTimestamp, AmDateFormat.DATE_TIME);
	}

	public String getFormatedClearTimestamp() {
		return Formatter.getFormatedTimestamp(clearTimestamp, AmDateFormat.DATE_TIME);
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
		otherLabels.remove(LBL_ALERTNAME);
		otherLabels.remove(LBL_INFO);
		otherLabels.remove(LBL_INSTANCE);
		otherLabels.remove(LBL_NODENAME);
		otherLabels.remove(LBL_JOB);
		otherLabels.remove(LBL_TAGS);
		otherLabels.remove(LBL_SEVERITY);
		otherLabels.remove(LBL_PRIORITY);
		otherLabels.remove(LBL_GROUP);
		otherLabels.remove(LBL_EVENTTYPE);
		otherLabels.remove(LBL_PROBABLECAUSE);
		otherLabels.remove(LBL_URL);
		otherLabels.remove(LBL_DESCRIPTION);

		String[] lblArray = AmProps.ALERTMONITOR_PROMETHEUS_ID_LABELS.split(",");
		for (int i = 0; i < lblArray.length; i++) {
			otherLabels.remove(lblArray[i].trim());
		}
		otherLabelsString = otherLabels.toString();
	}

	public String getOtherLabelsString() {
		return otherLabelsString;
	}

	public void setOtherLabelsString(String otherLabelsString) {
		this.otherLabelsString = otherLabelsString;
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

//	@Override
//	protected Object clone() throws CloneNotSupportedException {
//		return super.clone();
//	}

	/**
	 * This will generate a new object, which represents a clear event of this alert.
	 * @return event
	 */
	public DEvent generateClearEvent() {
		try {
			long now = System.currentTimeMillis();
			// create artificial clear event
			DEvent eClone = (DEvent) this.clone();
			eClone.setTimestamp(now);
			eClone.setClearTimestamp(now);
			eClone.setSeverity(DSeverity.CLEAR);
			eClone.setSource("SYNC");
			eClone.generateUID();
			return eClone;
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "DEvent{" +
				"uid='" + uid + '\'' +
				", correlationId='" + correlationId + '\'' +
				", prometheusId='" + prometheusId + '\'' +
				", timestamp=" + timestamp +
				", counter=" + counter +
				", firstTimestamp=" + firstTimestamp +
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
				", toBeDeleted=" + toBeDeleted +
				", otherLabels=" + otherLabels +
				", ruleExpression='" + ruleExpression + '\'' +
				", ruleTimeLimit='" + ruleTimeLimit + '\'' +
				'}';
	}
}
