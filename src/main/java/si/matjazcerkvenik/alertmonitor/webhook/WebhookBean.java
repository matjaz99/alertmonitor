/* 
 * Copyright (C) 2015 Matjaz Cerkvenik
 * 
 * DTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DTools. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package si.matjazcerkvenik.alertmonitor.webhook;

import si.matjazcerkvenik.alertmonitor.model.*;
import si.matjazcerkvenik.alertmonitor.util.KafkaClient;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


@ManagedBean
@SessionScoped
public class WebhookBean {

	private String columnTemplate = "id brand year";

	private List<DTag> tagList = new ArrayList<>();

	private String searchString;

	public void addMessage() {
		Growl.showInfoGrowl("Configuration updated", "");
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
		DAO.getLogger().info("SEARCH: " + searchString);
	}

	public String getPsyncEndpoint() {
		return DAO.ALERTMONITOR_PSYNC_ENDPOINT;
	}

	public void setPsyncEndpoint(String endpoint) {
		DAO.ALERTMONITOR_PSYNC_ENDPOINT = endpoint;
		DAO.getLogger().info("WebhookBean: psync endpoint changed:" + endpoint);
//		Growl.showInfoGrowl("Configuration updated", "");
	}

	public String getVersion() {
		return DAO.version;
	}

	public boolean isContainerized() { return DAO.isContainerized; }

	public String getLocalIpAddress() {
		return DAO.getInstance().getLocalIpAddress();
	}

	public int getWhMsgCount() {
		return DAO.webhookMessagesReceivedCount;
	}

	public int getAmMsgCount() {
		return DAO.amMessagesReceivedCount;
	}

	public int getJournalCount() {
		return DAO.journalReceivedCount;
	}

	public int getJournalSize() {
		return DAO.getInstance().getJournal().size();
	}

	public void setJournalMaxSize(String size) {

		DAO.JOURNAL_TABLE_SIZE = Integer.parseInt(size);
		DAO.getLogger().info("WebhookBean: journal max size changed:" + DAO.JOURNAL_TABLE_SIZE);
//		Growl.showInfoGrowl("Configuration updated", "");
	}

	public String getJournalMaxSize() { return Integer.toString(DAO.JOURNAL_TABLE_SIZE); }

	public int getAlarmsCount() {
		return DAO.raisingEventCount;
	}

	public int getClearsCount() {
		return DAO.clearingEventCount;
	}

	public int getNumberOfAlertsInLastHour() {
		List<DNotification> list = new ArrayList<DNotification>(DAO.getInstance().getJournal());
		List<DNotification> result = list.stream()
				.filter(notif -> checkIfYoungerThan(notif, 60))
				.collect(Collectors.toList());
		return result.size();
	}

	public String getAlertsPerSecondInLastHour() {
		List<DNotification> list = new ArrayList<DNotification>(DAO.getInstance().getJournal());
		List<DNotification> result = list.stream()
				.filter(notif -> checkIfYoungerThan(notif, 60))
				.collect(Collectors.toList());
		int count = result.size();
		double perSecond = count / 3600.0;
		DecimalFormat df2 = new DecimalFormat("#.###");
		return df2.format(perSecond);
	}

	private boolean checkIfYoungerThan(DNotification notif, int minutes) {
		if (System.currentTimeMillis() - notif.getTimestamp() < minutes * 60 * 1000) return true;
		return false;
	}

	public void setPsyncInterval(String interval) {

		DAO.ALERTMONITOR_PSYNC_INTERVAL_SEC = Integer.parseInt(interval);
		DAO.getLogger().info("WebhookBean: psync interval changed:" + DAO.ALERTMONITOR_PSYNC_INTERVAL_SEC);
//		Growl.showInfoGrowl("Configuration updated", "");
		TaskManager.getInstance().restartPsyncTimer();
	}

	public String getPsyncInterval() { return Integer.toString(DAO.ALERTMONITOR_PSYNC_INTERVAL_SEC); }

	public String getLastPsyncTime() { return DAO.getInstance().getFormatedTimestamp(DAO.lastPsyncTimestamp); }

	public String getPsyncSuccessCount() { return Integer.toString(DAO.psyncSuccessCount); }

	public String getPsyncFailedCount() { return Integer.toString(DAO.psyncFailedCount); }

	public void setKafkaEnabled(boolean kafkaEnabled) {
		DAO.ALERTMONITOR_KAFKA_ENABLED = kafkaEnabled;
		DAO.getLogger().info("WebhookBean: kafka enabled changed:" + DAO.ALERTMONITOR_KAFKA_ENABLED);
	}

	public boolean isKafkaEnabled() {
		return DAO.ALERTMONITOR_KAFKA_ENABLED;
	}

	public void setKafkaServer(String kafkaServer) {
		DAO.ALERTMONITOR_KAFKA_SERVER = kafkaServer;
		DAO.getLogger().info("WebhookBean: kafka server changed:" + DAO.ALERTMONITOR_KAFKA_SERVER);
		KafkaClient.getInstance().resetClient();
	}

	public String getKafkaServer() {
		return DAO.ALERTMONITOR_KAFKA_SERVER;
	}

	public void setKafkaTopic(String kafkaTopic) {
		DAO.ALERTMONITOR_KAFKA_TOPIC = kafkaTopic;
		DAO.getLogger().info("WebhookBean: kafka topic changed:" + DAO.ALERTMONITOR_KAFKA_TOPIC);
	}

	public String getKafkaTopic() {
		return DAO.ALERTMONITOR_KAFKA_TOPIC;
	}

	public List<WebhookMessage> getWebhookMessages() {
		return DAO.getInstance().getWebhookMessages();
	}

	public List<DNotification> getJournal() {
		return DAO.getInstance().getJournal();
	}

	public List<DTag> getTags() {
		List<DTag> daoTagList = DAO.getInstance().getTags();
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

	public int getActiveAlarmsCount() {
		return DAO.getInstance().getActiveAlerts().size();
	}

	public List<DNotification> getActiveAlarms() {
		List<DNotification> list = new ArrayList<>(DAO.getInstance().getActiveAlerts().values());
		List<DNotification> result = list.stream()
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
	private boolean filterNotification(DNotification notif) {
		// check if matches search field
		if (searchString != null && searchString.length() > 0) {
			if (!notif.getInstance().contains(searchString)) return false;
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

	public int getActiveAlarmsCount(String severity) {
		return DAO.getInstance().getActiveAlarmsList(severity).size();
	}

	public String getBalanceFactor() {
		DecimalFormat df2 = new DecimalFormat("#.##");
		return df2.format(DAO.getInstance().calculateAlertsBalanceFactor());
	}

	public String getStartTime() {
		return DAO.getInstance().getFormatedTimestamp(DAO.startUpTime);
	}

	public String getUpTime() {
		int secUpTotal = (int) ((System.currentTimeMillis() - DAO.startUpTime) / 1000);
		return convertToDHMSFormat(secUpTotal);
	}

	public String getLastEventTime() {
		return DAO.getInstance().getFormatedTimestamp(DAO.lastEventTimestamp);
	}

	public String getTimeSinceLastEvent() {
		int secUp = (int) ((System.currentTimeMillis() - DAO.lastEventTimestamp) / 1000);
		return convertToDHMSFormat(secUp);
	}

	private String convertToDHMSFormat(int secUpTotal) {
		int secUpRemain = secUpTotal % 60;
		int minUpTotal = secUpTotal / 60;
		int minUpRemain = minUpTotal % 60;
		int hourUpTotal = minUpTotal / 60;
		int hourUpRemain = hourUpTotal % 60;
		int dayUpTotal = hourUpTotal / 24;
		int dayUpRemain = hourUpTotal % 24;

		String resp = minUpRemain + "m " + secUpRemain + "s";

		if (dayUpTotal == 0) {
			if (hourUpRemain > 0) {
				resp = hourUpTotal + "h " + resp;
			}
		}

		if (dayUpTotal > 0) {
			resp = dayUpTotal + "d " + dayUpRemain + "h " + resp;
		}

		return resp;
	}

	public void tagAction(DTag tag) {
		DAO.getLogger().debug("tag action called: " + tag.getName());

		int numberOfSelectedTags = getNumberOfSelectedTags();

		DAO.getLogger().debug("numberOfSelectedTags: " + numberOfSelectedTags + "/" + tagList.size());

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


	public List<Target> getTargets() {
		return DAO.getInstance().getTargets();
	}

	public String getTargetHighestPriorityBullet(Target target) {
		int critical = 0;
		int major = 0;
		int minor = 0;
		int warning = 0;
		for (DNotification n : target.getAlerts()) {
			if (n.getSeverity().equalsIgnoreCase(Severity.CRITICAL)) {
				critical++;
			}
			if (n.getSeverity().equalsIgnoreCase(Severity.MAJOR)) {
				major++;
			}
			if (n.getSeverity().equalsIgnoreCase(Severity.MINOR)) {
				minor++;
			}
			if (n.getSeverity().equalsIgnoreCase(Severity.WARNING)) {
				warning++;
			}
		}

		if (critical > 0) return "bullet_red_mini.png";
		if (major >  0) return "bullet_orange_mini.png";
		if (minor > 0) return "bullet_orange_mini.png";
		if (warning > 0) return "bullet_yellow_mini.png";
		return "bullet_green_mini.png";
	}



}
