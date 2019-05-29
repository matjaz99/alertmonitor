package si.matjazcerkvenik.alertmonitor.webhook;

import java.util.List;
import java.util.Map;

public class AmAlertMessage {
	
	private String receiver;
	private String status;
	private List<Alert> alerts;
	private Map<String, String> groupLabels;
	private Map<String, String> commonLabels;
	private Map<String, String> commonAnnotations;
	private String externalURL;
	private String version;
	private String groupKey;
	
	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Map<String, String> getGroupLabels() {
		return groupLabels;
	}

	public void setGroupLabels(Map<String, String> groupLabels) {
		this.groupLabels = groupLabels;
	}

	public Map<String, String> getCommonLabels() {
		return commonLabels;
	}

	public void setCommonLabels(Map<String, String> commonLabels) {
		this.commonLabels = commonLabels;
	}

	public Map<String, String> getCommonAnnotations() {
		return commonAnnotations;
	}

	public void setCommonAnnotations(Map<String, String> commonAnnotations) {
		this.commonAnnotations = commonAnnotations;
	}

	public String getExternalURL() {
		return externalURL;
	}

	public void setExternalURL(String externalURL) {
		this.externalURL = externalURL;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getGroupKey() {
		return groupKey;
	}

	public void setGroupKey(String groupKey) {
		this.groupKey = groupKey;
	}
	public List<Alert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<Alert> alerts) {
		this.alerts = alerts;
	}

	@Override
	public String toString() {
		return "AmAlertMessage [receiver=" + receiver + ", status=" + status + ", alerts=" + alerts + ", groupLabels="
				+ groupLabels + ", commonLabels=" + commonLabels + ", commonAnnotations=" + commonAnnotations
				+ ", externalURL=" + externalURL + ", version=" + version + ", groupKey=" + groupKey + "]";
	}
	
	
}
