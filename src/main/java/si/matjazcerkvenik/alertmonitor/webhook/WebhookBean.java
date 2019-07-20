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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


@ManagedBean
@SessionScoped
public class WebhookBean {

	private String columnTemplate = "id brand year";

	public int getRawMsgCount() {
		return DAO.rawMessagesReceivedCount;
	}

	public int getAmMsgCount() {
		return DAO.amMessagesReceivedCount;
	}

	public int getJournalCount() {
		return DAO.journalReceivedCount;
	}

	public int getAlarmsCount() {
		return DAO.alertEventCount;
	}

	public int getClearsCount() {
		return DAO.clearEventCount;
	}

	public double getBalanceFactor() {
		double d = (5 * getActiveAlarmsCount("critical")
			+ 4 * getActiveAlarmsCount("major")
			+ 3 * getActiveAlarmsCount("minor")
			+ 2 * getActiveAlarmsCount("warning")) * 1.0 / getActiveAlarms().size();
		return d;
	}

	public int getJournalSize() {
		return DAO.getInstance().getJournal().size();
	}

	public String getStartUpTime() {
		long now = System.currentTimeMillis();
		return (now - DAO.startUpTime) / 1000 + " seconds";
	}
	
	public List<RawHttpMessage> getMessages() {
		return DAO.getInstance().getRawMessages();
	}

	
	public List<DNotification> getJournal() {
		List<DNotification> list = DAO.getInstance().getJournal();
//		Collections.sort(list, new Comparator<DNotification>() {
//			@Override
//			public int compare(DNotification lhs, DNotification rhs) {
//				// -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
//				return lhs.getTimestamp() > rhs.getTimestamp() ? -1 : (lhs.getTimestamp() < rhs.getTimestamp()) ? 1 : 0;
//			}
//		});
		return list;
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

		List<DNotification> list = DAO.getInstance().getActiveAlerts().values().stream()
				.filter(notif -> notif.getSeverity().equals(severity))
				.collect(Collectors.toList());

		return list.size();

	}
	
}
