package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import java.util.Map;

public class AmAlert {
	
	private String status;
	private Map<String, String> labels;
	private Map<String, String> annotations;
	private String startsAt;
	private String endsAt;
	private String generatorURL;
	
	
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getStartsAt() {
		return startsAt;
	}

	public void setStartsAt(String startsAt) {
		this.startsAt = startsAt;
	}

	public String getEndsAt() {
		return endsAt;
	}

	public void setEndsAt(String endsAt) {
		this.endsAt = endsAt;
	}

	public String getGeneratorURL() {
		return generatorURL;
	}

	public void setGeneratorURL(String generatorURL) {
		this.generatorURL = generatorURL;
	}

	@Override
	public String toString() {
		return "AmAlert [status=" + status + ", labels=" + labels + ", annotations="
				+ annotations + ", startsAt=" + startsAt + ", endsAt=" + endsAt + ", generatorURL="
				+ generatorURL + "]";
	}
	
	
	
	/* Helper methods */
	
	public String getLabel(String key) {
		if (labels.containsKey(key)) {
			return labels.get(key);
		} else {
			if (key.equalsIgnoreCase("severity")) {
				return status;
			}
			return "";
		}
	}
	
}
