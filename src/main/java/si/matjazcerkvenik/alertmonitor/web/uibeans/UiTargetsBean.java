package si.matjazcerkvenik.alertmonitor.web.uibeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DJob;
import si.matjazcerkvenik.alertmonitor.model.DSeverity;
import si.matjazcerkvenik.alertmonitor.model.DTarget;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

@Named("uiTargetsBean")
@ViewScoped
@SuppressWarnings("unused")
public class UiTargetsBean implements Serializable {

	private static final long serialVersionUID = 6626636171597L;
	
	private String providerId;
	
	private boolean smartTargetsEnabled = true;
	
	// result of Prometheus API call
	private String result;
	
	@PostConstruct
	public void init() {
		Map<String, String> params = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
        providerId = params.getOrDefault("providerId", null);
        if (providerId == null) {
        	// error
		}
		LogFactory.getLogger().info("UiTargetsBean.init(): " + providerId);
	}
	
	public boolean isSmartTargetsEnabled() {
		return smartTargetsEnabled;
	}

	public void setSmartTargetsEnabled(boolean smartTargetsEnabled) {
		this.smartTargetsEnabled = smartTargetsEnabled;
	}
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public List<DTarget> getTargets() {

		result = null;

		List<DTarget> tList = new ArrayList<DTarget>();
		
		if (providerId == null) {
			for (AbstractDataProvider adp : DAO.getInstance().getAllDataProviders()) {
				if (smartTargetsEnabled) {
					tList.addAll(adp.getSmartTargets());
				} else {
					tList.addAll(adp.getTargets());
				}
			}
		} else {
			AbstractDataProvider adp = DAO.getInstance().getDataProviderById(providerId);
			if (smartTargetsEnabled) {
				tList = adp.getSmartTargets();
			} else {
				tList = adp.getTargets();
			}
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
	private boolean filterTarget(DTarget target) {
		// check if matches search field
		// TODO
//		if (searchString != null && searchString.length() > 0) {
//			if (!target.getHostname().toLowerCase().contains(searchString.toLowerCase())) return false;
//		}
		return true;
	}

	public String getTargetHighestPriorityBullet(DTarget target) {
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
	public List<DJob> getJobs() {

		result = null;

		AbstractDataProvider adp = DAO.getInstance().getDataProviderById(providerId);

		List<DTarget> tList = adp.getTargets();

		if (tList == null) {
			result = "failed to retrieve jobs";
			return new ArrayList<>();
		}

		Map<String, DJob> jMap = new HashMap<>();

		for (DTarget t : tList) {
			DJob job = jMap.getOrDefault(t.getJob(), new DJob());
			job.setName(t.getJob());
			job.getTargetList().add(t);
			jMap.put(job.getName(), job);
		}

		return new ArrayList<>(jMap.values());
	}
	
}
