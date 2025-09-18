package si.matjazcerkvenik.alertmonitor.web.uibeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DTag;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

@Named("uiAlertsBean")
@RequestScoped
@SuppressWarnings("unused")
public class UiAlertsBean extends CommonBean implements Serializable {

	private static final long serialVersionUID = 2676128254354495702L;
	
	private String searchString;
	
	@PostConstruct
	public void init() {
        if (providerId == null) {
        	// show alerts of all providers
		} else {
			// show only alerts for selected provider
		}
		LogFactory.getLogger().info("UiAlertsBean.init(): " + providerId);
	}
	
	public List<DEvent> getActiveAlarms() {
		List<DEvent> list = new ArrayList<>();
		
		if (providerId == null) {
        	// get alerts of all providers
			for (AbstractDataProvider adp : DAO.getInstance().getAllDataProviders()) {
				list.addAll(adp.getActiveAlerts().values());
			}
		} else {
			// get only alerts for selected provider
			list.addAll(dataProvider.getActiveAlerts().values());
		}
		
		List<DEvent> result = list.stream()
				.filter(notif -> filterEvent(notif))
				.sorted(Comparator.comparing(DEvent::getSeverity))
				.collect(Collectors.toList());
		
		return result;
	}

	/**
	 * Return true if notification satisfies conditions to be displayed in GUI.
	 * Search field is checked and selected tags are checked.
	 * @param event alert
	 * @return true to display alert
	 */
	private boolean filterEvent(DEvent event) {
		// check if matches search field
		if (searchString != null && searchString.length() > 0) {
			if (!event.getInstance().toLowerCase().contains(searchString.toLowerCase())
					&& !event.getAlertname().toLowerCase().contains(searchString.toLowerCase())
					&& !event.getInfo().toLowerCase().contains(searchString.toLowerCase())
					&& !event.getJob().toLowerCase().contains(searchString.toLowerCase())
					&& !event.getDescription().toLowerCase().contains(searchString.toLowerCase()))
				return false;
		}
		
		// TODO finish this

		// read tags
//		String[] array = event.getTags().split(",");
//		for (int i = 0; i < array.length; i++) {
//			String tagName = array[i].trim();
//
//			for (DTag t : tagList) {
//				if (t.getName().equals(tagName) && t.isSelected()) {
//					return true;
//				}
//				if (t.getName().equals(event.getSeverity()) && t.isSelected()) {
//					return true;
//				}
//				if (t.getName().equals(event.getPriority()) && t.isSelected()) {
//					return true;
//				}
//			}
//
//		}
//		return false;
		return true;
	}

}
