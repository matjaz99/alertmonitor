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
package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import java.util.List;
import java.util.Map;

/**
 * This class represents whole message sent from Alertmanager.
 */
public class AmMessage {
	
	private String receiver;
	private String status;
	private List<AmAlert> alerts;
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

	public List<AmAlert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<AmAlert> alerts) {
		this.alerts = alerts;
	}

	@Override
	public String toString() {
		return "AmMessage [receiver=" + receiver + ", status=" + status + ", alerts=" + alerts + ", groupLabels="
				+ groupLabels + ", commonLabels=" + commonLabels + ", commonAnnotations=" + commonAnnotations
				+ ", externalURL=" + externalURL + ", version=" + version + ", groupKey=" + groupKey + "]";
	}
	
	
}
