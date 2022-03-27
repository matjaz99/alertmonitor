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
package si.matjazcerkvenik.alertmonitor.web;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DTag;
import si.matjazcerkvenik.alertmonitor.model.TagColors;
import si.matjazcerkvenik.alertmonitor.model.Target;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.util.*;

@ManagedBean
@RequestScoped
public class UiAlertBean {

    private DEvent event;

    @PostConstruct
    public void init() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String uid = requestParameterMap.getOrDefault("uid", "null");
        event = DAO.getInstance().getEvent(uid);
        if (event == null) {
            Growl.showWarningGrowl("Object not found", null);
            LogFactory.getLogger().info("UiAlertBean: object not found: uid=" + uid);
        }
    }

    public DEvent getEvent() {
        return event;
    }

    public DTag getTagObject(String severity) {
        return new DTag(severity, TagColors.getColor(severity));
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