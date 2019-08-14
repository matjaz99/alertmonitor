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

import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DNotification;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


@ManagedBean
@SessionScoped
public class WebhookBean {

	private String columnTemplate = "id brand year";

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

	public int getAlarmsCount() {
		return DAO.raisingEventCount;
	}

	public int getClearsCount() {
		return DAO.clearingEventCount;
	}

	public List<WebhookMessage> getWebhookMessages() {
		return DAO.getInstance().getWebhookMessages();
	}

	public List<DNotification> getJournal() {
		return DAO.getInstance().getJournal();
	}

	public List<DNotification> getActiveAlarms() {
		List<DNotification> list = new ArrayList<DNotification>(DAO.getInstance().getActiveAlerts().values());
		Collections.sort(list, new Comparator<DNotification>() {
			@Override
			public int compare(DNotification lhs, DNotification rhs) {
				// -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
				return lhs.getTimestamp() > rhs.getTimestamp() ? -1 : (lhs.getTimestamp() < rhs.getTimestamp()) ? 1 : 0;
			}
		});
		System.out.println("Active alarm list size: " + list.size());
		return list;
	}

	public int getActiveAlarmsCount(String severity) {
		return DAO.getInstance().getActiveAlarmsCount(severity);
	}

	public String getBalanceFactor() {
		DecimalFormat df2 = new DecimalFormat("#.##");
		return df2.format(DAO.getInstance().calculateAlertsBalanceFactor());
	}

	public String getStartTime() {
		return DAO.getInstance().getFormatedTimestamp(DAO.startUpTime);
	}

	public String getUpTime() {
		int secUp = (int) ((System.currentTimeMillis() - DAO.startUpTime) / 1000);
		return secUp + " seconds";
	}

	public String getUpTimeTest() {
		int secUp = (int) ((System.currentTimeMillis() - DAO.startUpTime) / 1000);
		int minUp = secUp / 60;
		int hourUp = minUp / 60;
		int dayUp = hourUp / 24;
		// TODO finish this
		System.out.println(secUp + "s = " + minUp + "m + " + minUp % 60 + "s");
		System.out.println(minUp + "m = " + hourUp + "h + " + minUp % 60 + "m + " + minUp % 60 + "s");
		String resp = hourUp + "h + " + minUp % 60 + "m + " + minUp % 60 + "s";
		return resp;
	}

	public String getLastEventTime() {
		return DAO.getInstance().getFormatedTimestamp(DAO.lastEventTimestamp);
	}

	public String getTimeSinceLastEvent() {
		int secUp = (int) ((System.currentTimeMillis() - DAO.lastEventTimestamp) / 1000);
		return secUp + " seconds";
	}

}
