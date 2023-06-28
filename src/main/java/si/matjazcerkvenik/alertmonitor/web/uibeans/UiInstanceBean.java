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

import org.primefaces.model.timeline.TimelineEvent;
import org.primefaces.model.timeline.TimelineModel;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DTarget;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.web.uibeans.UiConfigBean;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean
@ViewScoped
@SuppressWarnings("unused")
public class UiInstanceBean implements Serializable {

    private static final long serialVersionUID = 7961535598744624L;

    @ManagedProperty(value="#{uiConfigBean}")
    private UiConfigBean uiConfigBean;

    private DTarget target;

    @PostConstruct
    public void init() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String id = requestParameterMap.getOrDefault("target", "null");
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(uiConfigBean.getSelectedDataProvider());
        target = adp.getSingleTarget(id);
        LogFactory.getLogger().info("UiInstanceBean: init: found target: " + target.toString());
    }

    public UiConfigBean getUiConfigBean() {
        return uiConfigBean;
    }

    public void setUiConfigBean(UiConfigBean uiConfigBean) {
        this.uiConfigBean = uiConfigBean;
    }

    public DTarget getTarget() {
        return target;
    }

    public List<DEvent> getInstanceActiveAlarms() {
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(uiConfigBean.getSelectedDataProvider());
        List<DEvent> list = new ArrayList<>(adp.getActiveAlerts().values());
        List<DEvent> result = list.stream()
                .filter(notif -> checkAlert(notif))
                .collect(Collectors.toList());
        Collections.sort(result, new Comparator<DEvent>() {
            @Override
            public int compare(DEvent lhs, DEvent rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getTimestamp() > rhs.getTimestamp() ? -1 : (lhs.getTimestamp() < rhs.getTimestamp()) ? 1 : 0;
            }
        });
        return result;
    }

    public List<DEvent> getInstanceJournalAlarms() {
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(uiConfigBean.getSelectedDataProvider());
        List<DEvent> result = adp.getJournal().stream()
                .filter(notif -> checkAlert(notif))
                .collect(Collectors.toList());
        Collections.sort(result, new Comparator<DEvent>() {
            @Override
            public int compare(DEvent lhs, DEvent rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getTimestamp() > rhs.getTimestamp() ? -1 : (lhs.getTimestamp() < rhs.getTimestamp()) ? 1 : 0;
            }
        });
        return result;
    }

    private boolean checkAlert(DEvent n) {
        if (target.isSmartTarget()) {
            if (n.getHostname().equals(target.getHostname())) return true;
        } else {
            if (n.getInstance().equals(target.getHostname())) return true;
        }
        return false;
    }

    public String getTargetType() {
        if (target.isSmartTarget()) return "SmartTarget";
        return "Instance";
    }

    public String getUpStatus() {
        if (target.isUp()) return "Up";
        return "Down";
    }

    public String getUpStatusSeverity() {
        if (target.isUp()) return "success";
        return "danger";
    }

    public String getUpStatusTooltip() {
        if (target.isUp()) return "At least one job is up";
        return "All jobs are down";
    }



    private TimelineModel<String, ?> model;
    private LocalDateTime start;
    private LocalDateTime end;

    public TimelineModel<String, ?> getModel() {

        List<DEvent> dEvents = getInstanceJournalAlarms();

        HashMap<String, List<DEvent>> tempMap = new HashMap<>();

        for (DEvent e : dEvents) {
            System.out.println("getModel: event: " + e.toString());
            List<DEvent> list = tempMap.getOrDefault(e.getCorrelationId(), new ArrayList<>());
            list.add(e);
            tempMap.put(e.getCorrelationId(), list);
        }

        // create timeline model
        model = new TimelineModel<>();

        // set initial start / end dates for the axis of the timeline
        start = LocalDate.of(-140, 1, 1).atStartOfDay();
        end = LocalDate.of(-140, 1, 2).atStartOfDay();

        for (String s : tempMap.keySet()) {
            for (DEvent e : tempMap.get(s)) {
                TimelineEvent event = TimelineEvent.builder()
                        .data(e.getInfo())
                        .startDate(start)
                        .endDate(end)
                        .editable(true)
                        .group(e.getAlertname())
                        .styleClass("sdfg".toLowerCase())
                        .build();

                model.add(event);
            }
        }



//        // groups
//        String[] names = new String[]{"User 1", "User 2", "User 3", "User 4", "User 5", "User 6"};
//
//        for (String name : names) {
//            LocalDateTime end = start.minusHours(12).withMinute(0).withSecond(0).withNano(0);
//
//            for (int i = 0; i < 5; i++) {
//                LocalDateTime start = end.plusHours(Math.round(Math.random() * 5));
//                end = start.plusHours(4 + Math.round(Math.random() * 5));
//
//                long r = Math.round(Math.random() * 2);
//                String availability = (r == 0 ? "Unavailable" : (r == 1 ? "Available" : "Maybe"));
//
//                // create an event with content, start / end dates, editable flag, group name and custom style class
//                TimelineEvent event = TimelineEvent.builder()
//                        .data(availability)
//                        .startDate(start)
//                        .endDate(end)
//                        .editable(true)
//                        .group(name)
//                        .styleClass(availability.toLowerCase())
//                        .build();
//
//                model.add(event);
//            }
//        }
        return model;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

}
