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
	
	public List<RawHttpMessage> getMessages() {
		return WebhookServlet.messages;
	}
	
	public List<AmAlertMessage> getAmMessages() {
		return WebhookServlet.amMessages;
	}
	
	public List<DNotification> getDNotifs() {
		return WebhookServlet.dNotifs;
	}
	
	public List<DNotification> getActiveAlarms() {
		List<DNotification> list = new ArrayList<DNotification>(WebhookServlet.activeAlerts.values());
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

//		List<String> lines = Arrays.asList("spring", "node", "mkyong");
//
//		List<String> result = lines.stream()                // convert list to stream
//				.filter(line -> !"mkyong".equals(line))     // we dont like mkyong
//				.collect(Collectors.toList());              // collect the output and convert streams to a List
//
//		result.forEach(System.out::println);                //output : spring, node

		List<DNotification> list = WebhookServlet.activeAlerts.values().stream()
				.filter(notif -> notif.getSeverity().equals(severity))
				.collect(Collectors.toList());

		return list.size();

	}
	
}
