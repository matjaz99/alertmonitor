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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


@ManagedBean
@SessionScoped
public class WebhookBean {
	
	public List<HttpMessage> getMessages() {
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
		return list;
	}
	
	public String getSeverityStatusIcon(String severity) {
		
		if (severity.equalsIgnoreCase("critical")) {
			return "bullet_red.png";
		} else if (severity.equalsIgnoreCase("major")) {
			return "bullet_orange.png";
		} else if (severity.equalsIgnoreCase("minor")) {
			return "bullet_orange.png";
		} else if (severity.equalsIgnoreCase("warning")) {
			return "bullet_yellow.png";
		} else if (severity.equalsIgnoreCase("clear")) {
			return "bullet_green.png";
		} else if (severity.equalsIgnoreCase("informational")) {
			return "bullet_blue.png";
		} else {
			return "bullet_black.png";
		}
		
	}
	
}
