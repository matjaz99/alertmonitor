package si.matjazcerkvenik.alertmonitor.web.uibeans;

import java.util.Map;

import jakarta.faces.context.FacesContext;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;

/**
 * This is parent class of all Ui beans. It contains providerId, as it is common parameter for almost all requests.
 */
public class CommonBean {
	
	protected Map<String, String> urlParams;
	protected String providerId;
	protected AbstractDataProvider dataProvider;
	

	public CommonBean() {
		urlParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        providerId = urlParams.getOrDefault("providerId", null);
        if (providerId != null) {
        	dataProvider = DAO.getInstance().getDataProviderById(providerId);
		}
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public AbstractDataProvider getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(AbstractDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	public String getProviderName() {
		return DAO.getInstance().getDataProviderById(providerId).getProviderConfig().getName();
	}
	

}
