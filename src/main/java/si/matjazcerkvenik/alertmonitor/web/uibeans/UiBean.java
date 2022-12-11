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
package si.matjazcerkvenik.alertmonitor.web.uibeans;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.*;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.web.Growl;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

import java.util.*;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;


@ManagedBean
@SessionScoped
@SuppressWarnings("unused")
public class UiBean {

	@ManagedProperty(value="#{uiConfigBean}")
	private UiConfigBean uiConfigBean;

	private List<DTag> tagList = new ArrayList<>();
	private String searchString;
	private boolean smartTargetsEnabled = true;

	// result of Prometheus API call
	private String result;

	public void addMessage() {
		Growl.showInfoGrowl("Configuration updated", "");
	}

	public UiConfigBean getUiConfigBean() {
		return uiConfigBean;
	}

	public void setUiConfigBean(UiConfigBean uiConfigBean) {
		this.uiConfigBean = uiConfigBean;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
		LogFactory.getLogger().info("SEARCH: " + searchString);
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public List<WebhookMessage> getWebhookMessages() {
		AbstractDataProvider adp = DAO.getInstance().getDataProvider(uiConfigBean.getSelectedDataProvider());
		System.out.println("found adp: " + adp.getProviderConfig().getName());
		return adp.getWebhookMessages();
	}


	public List<DEvent> getJournal() {
		AbstractDataProvider adp = DAO.getInstance().getDataProvider(uiConfigBean.getSelectedDataProvider());
		return adp.getJournal();
	}


	public List<DEvent> getActiveAlarms() {
		AbstractDataProvider adp = DAO.getInstance().getDataProvider(uiConfigBean.getSelectedDataProvider());
		List<DEvent> list = new ArrayList<>(adp.getActiveAlerts().values());
		List<DEvent> result = list.stream()
				.filter(notif -> filterNotification(notif))
				.collect(Collectors.toList());

		// if you sort alerts here, then sorting in columns is not working
		// TODO how to sort?
//		Collections.sort(result, new Comparator<DNotification>() {
//			@Override
//			public int compare(DNotification lhs, DNotification rhs) {
//				// -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
//				return lhs.getTimestamp() > rhs.getTimestamp() ? -1 : (lhs.getTimestamp() < rhs.getTimestamp()) ? 1 : 0;
//			}
//		});
		return result;
	}

	/**
	 * Return true if notification satisfies conditions to be displayed in GUI.
	 * Search field is checked and selected tags are checked.
	 * @param notif alert
	 * @return true to display alert
	 */
	private boolean filterNotification(DEvent notif) {
		// check if matches search field
		if (searchString != null && searchString.length() > 0) {
			if (!notif.getInstance().toLowerCase().contains(searchString.toLowerCase())
					&& !notif.getAlertname().toLowerCase().contains(searchString.toLowerCase())
					&& !notif.getInfo().toLowerCase().contains(searchString.toLowerCase())
					&& !notif.getJob().toLowerCase().contains(searchString.toLowerCase())
					&& !notif.getDescription().toLowerCase().contains(searchString.toLowerCase())) return false;
		}

		// read tags
		String[] array = notif.getTags().split(",");
		for (int i = 0; i < array.length; i++) {
			String tagName = array[i].trim();

			for (DTag t : tagList) {
				if (t.getName().equals(tagName) && t.isSelected()) {
					return true;
				}
				if (t.getName().equals(notif.getSeverity()) && t.isSelected()) {
					return true;
				}
				if (t.getName().equals(notif.getPriority()) && t.isSelected()) {
					return true;
				}
			}

		}
		return false;
	}




	public List<DTag> getTags() {
		AbstractDataProvider adp = DAO.getInstance().getDataProvider(uiConfigBean.getSelectedDataProvider());
		List<DTag> daoTagList = adp.getTags();
		// add all from daoTagList to tagList which are not present yet
		for (DTag dt : daoTagList) {
			boolean found = false;
			for (DTag t: tagList) {
				if (t.getName().equals(dt.getName())) found = true;
			}
			if (!found) {
				if (getNumberOfSelectedTags() == tagList.size()) {
					dt.setSelected(true);
				} else {
					dt.setSelected(false);
				}
				tagList.add(dt);
			}
		}
		// remove all from tagList that are no more in daoTagList
		for (Iterator<DTag> it = tagList.iterator(); it.hasNext();) {
			DTag t = it.next();
			boolean found = false;
			for (DTag dt: daoTagList) {
				if (dt.getName().equals(t.getName())) found = true;
			}
			if (!found) {
				it.remove();
			}
		}
		return tagList;
	}

	public void tagAction(DTag tag) {
		LogFactory.getLogger().debug("tag action called: " + tag.getName());

		int numberOfSelectedTags = getNumberOfSelectedTags();

		LogFactory.getLogger().debug("numberOfSelectedTags: " + numberOfSelectedTags + "/" + tagList.size());

		if (numberOfSelectedTags == tagList.size()) {
			// all tags are enabled
			// first tag to be selected
			// deselect all except this one
			for (DTag t : tagList) {
				if (t.getName().equals(tag.getName())) {
					t.setSelected(true);
				} else {
					t.setSelected(false);
				}
			}
			return;
		}

		if (numberOfSelectedTags == 1) {
			// exactly one tag is selected, others are disabled
			// find which tag is selected
			DTag theOnlySelectedTag = null;
			for (DTag t : tagList) {
				if (t.isSelected()) {
					theOnlySelectedTag = t;
					break;
				}
			}
			// if this is the same tag, then select all
			if (theOnlySelectedTag.getName().equals(tag.getName())) {
				for (DTag t : tagList) {
					t.setSelected(true);
				}
			} else {
				for (DTag t : tagList) {
					if (t.getName().equals(tag.getName())) {
						t.setSelected(true);
					}
				}
			}
			return;
		}

		if (numberOfSelectedTags > 0) {
            DTag selectedTag = null;
            for (DTag t : tagList) {
                if (t.getName().equals(tag.getName())) {
                    t.setSelected(!t.isSelected());
                    break;
                }
            }
        }
	}

	private int getNumberOfSelectedTags() {
		int numberOfSelectedTags = 0;
		for (DTag t : tagList) {
			if (t.isSelected()) {
				numberOfSelectedTags++;
			}
		}
		return numberOfSelectedTags;
	}



	public boolean isSmartTargetsEnabled() {
		return smartTargetsEnabled;
	}

	public void setSmartTargetsEnabled(boolean smartTargetsEnabled) {
		this.smartTargetsEnabled = smartTargetsEnabled;
	}

	public List<Target> getTargets() {

		result = null;

		AbstractDataProvider adp = DAO.getInstance().getDataProvider(uiConfigBean.getSelectedDataProvider());

		List<Target> tList;
		if (smartTargetsEnabled) {
			tList = adp.getSmartTargets();
		} else {
			tList = adp.getTargets();
		}

		if (tList == null) {
			result = "failed to retrieve targets";
			return new ArrayList<>();
		}

		return tList.stream()
				.filter(target -> filterTarget(target))
				.collect(Collectors.toList());
	}

	/**
	 * Filter targets according to search criteria
	 * @param target
	 * @return true if matches
	 */
	private boolean filterTarget(Target target) {
		// check if matches search field
		if (searchString != null && searchString.length() > 0) {
			if (!target.getHostname().toLowerCase().contains(searchString.toLowerCase())) return false;
		}
		return true;
	}

	public String getTargetHighestPriorityBullet(Target target) {
		int critical = 0;
		int major = 0;
		int minor = 0;
		int warning = 0;
		int informational = 0;
		int indeterminate = 0;
		for (DEvent n : target.getAlerts()) {
			if (n.getSeverity().equalsIgnoreCase(DSeverity.CRITICAL)) {
				critical++;
			}
			if (n.getSeverity().equalsIgnoreCase(DSeverity.MAJOR)) {
				major++;
			}
			if (n.getSeverity().equalsIgnoreCase(DSeverity.MINOR)) {
				minor++;
			}
			if (n.getSeverity().equalsIgnoreCase(DSeverity.WARNING)) {
				warning++;
			}
			if (n.getSeverity().equalsIgnoreCase(DSeverity.INFORMATIONAL)) {
				informational++;
			}
			if (n.getSeverity().equalsIgnoreCase(DSeverity.INDETERMINATE)) {
				indeterminate++;
			}
		}

		if (critical > 0) return "bullet_red";
		if (major >  0) return "bullet_orange";
		if (minor > 0) return "bullet_orange";
		if (warning > 0) return "bullet_yellow";
		if (informational > 0) return "bullet_blue";
		if (indeterminate > 0) return "bullet_purple";
		return "bullet_green";
	}

	/**
	 * Call targets API and sort by jobs.
	 * @return list of jobs
	 */
	public List<Job> getJobs() {

		result = null;

		AbstractDataProvider adp = DAO.getInstance().getDataProvider(uiConfigBean.getSelectedDataProvider());

		List<Target> tList = adp.getTargets();

		if (tList == null) {
			result = "failed to retrieve jobs";
			return new ArrayList<>();
		}

		Map<String, Job> jMap = new HashMap<>();

		for (Target t : tList) {
			Job job = jMap.getOrDefault(t.getJob(), new Job());
			job.setJobName(t.getJob());
			job.getTargetList().add(t);
			jMap.put(job.getJobName(), job);
		}

		return new ArrayList<>(jMap.values());
	}


}
