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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DJob;
import si.matjazcerkvenik.alertmonitor.model.DSeverity;
import si.matjazcerkvenik.alertmonitor.model.DTag;
import si.matjazcerkvenik.alertmonitor.model.DTarget;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.web.Growl;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;


@Named("uiBean")
@RequestScoped
@SuppressWarnings("unused")
public class UiBean implements Serializable {

	private static final long serialVersionUID = 479421012028252L;

	@Inject
	private UiConfigBean uiConfigBean;
	
	@Inject
	private UiSessionBean uiSessionBean;
	
	private String providerId;

	private List<DTag> tagList = new ArrayList<>();
	private String searchString;

	
	@PostConstruct
	public void init() {
		Map<String, String> params = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
        providerId = params.getOrDefault("providerId", null);
        if (providerId == null) {
        	providerId = DAO.getInstance().getDataProvider(".default").getProviderConfig().getId();
		}
		LogFactory.getLogger().info("UiBean.init(): " + providerId);
	}
	

	public void addMessage() {
		Growl.showInfoGrowl("Configuration updated", "");
	}

	public UiConfigBean getUiConfigBean() {
		return uiConfigBean;
	}

	public void setUiConfigBean(UiConfigBean uiConfigBean) {
		this.uiConfigBean = uiConfigBean;
	}
	

	public UiSessionBean getUiSessionBean() {
		return uiSessionBean;
	}

	public void setUiSessionBean(UiSessionBean uiSessionBean) {
		this.uiSessionBean = uiSessionBean;
	}
	
	

	public String getProviderId() {
		return providerId;
	}


	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}


	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
		LogFactory.getLogger().info("SEARCH: " + searchString);
	}

	

	public List<WebhookMessage> getWebhookMessages() {
		AbstractDataProvider adp = DAO.getInstance().getDataProvider(uiConfigBean.getSelectedDataProvider());
		return adp.getWebhookMessages();
	}


	public List<DEvent> getJournal() {
		AbstractDataProvider adp = DAO.getInstance().getDataProvider(uiConfigBean.getSelectedDataProvider());
		return adp.getJournal();
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




	


}
