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
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DTag;
import si.matjazcerkvenik.alertmonitor.model.DTagColors;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.web.Growl;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.*;

@Named("uiAlertBean")
@RequestScoped
@SuppressWarnings("unused")
public class UiAlertBean extends CommonBean implements Serializable {

    private static final long serialVersionUID = 2791411831853745037L;

    private DEvent event;

    @PostConstruct
    public void init() {
        String uid = urlParams.getOrDefault("uid", "null");
        if (providerId == null) {
			// read event from DB
        	event = DAO.getInstance().getDataManager().getEvent(uid);
		} else {
			// read event via dataProvider class
			event = dataProvider.getEvent(uid);
		}
        
        if (event == null) {
            Growl.showWarningGrowl("Object not found", null);
            LogFactory.getLogger().info("UiAlertBean: object not found: uid=" + uid);
        }
    }


	public DEvent getEvent() {
        return event;
    }

    public DTag getTagObject(String severity) {
        return new DTag(severity, DTagColors.getColor(severity));
    }

    public String getAge() {
        int secUpTotal = 0;
        if (event.getClearTimestamp() == 0) {
            secUpTotal = (int) ((System.currentTimeMillis() - event.getFirstTimestamp()) / 1000);
        } else {
            secUpTotal = (int) ((event.getClearTimestamp() - event.getFirstTimestamp()) / 1000);
        }
        return Formatter.convertToDHMSFormat(secUpTotal);
    }

}
