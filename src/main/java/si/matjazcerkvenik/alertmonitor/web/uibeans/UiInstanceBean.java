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
import org.primefaces.model.timeline.TimelineGroup;
import org.primefaces.model.timeline.TimelineModel;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DTarget;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Named("uiInstanceBean")
@ViewScoped
@SuppressWarnings("unused")
public class UiInstanceBean extends CommonBean implements Serializable {

    private static final long serialVersionUID = 7961535598744624L;

    private DTarget target;

    @PostConstruct
    public void init() {
        String id = urlParams.getOrDefault("target", "null");
        target = dataProvider.getSingleTarget(id);
        LogFactory.getLogger().info("UiInstanceBean: init: found target: " + target.toString());
    }

    public DTarget getTarget() {
        return target;
    }

    public List<DEvent> getInstanceActiveAlarms() {
        List<DEvent> list = new ArrayList<>(dataProvider.getActiveAlerts().values());
        List<DEvent> result = list.stream()
                .filter(notif -> checkInstance(notif))
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
        List<DEvent> result = dataProvider.getJournal().stream()
                .filter(notif -> checkInstance(notif))
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

    private boolean checkInstance(DEvent n) {
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



    private TimelineModel<DEvent, String> model;
    private LocalDateTime start;
    private LocalDateTime end;

    public TimelineModel<DEvent, String> getModel() {

        // get journal alerts for this instance from db
        List<DEvent> journalEvents = getInstanceJournalAlarms();

        // sort series of alerts into lists by correlation id (key)
        Map<String, List<DEvent>> journalEventsMap = new HashMap<>();

        for (DEvent e : journalEvents) {
            List<DEvent> list = journalEventsMap.getOrDefault(e.getCorrelationId(), new ArrayList<>());
            System.out.println("== " + e.toString());
            list.add(e);
            journalEventsMap.put(e.getCorrelationId(), list);
        }

        // filter journal alerts:
        // remove clear alerts
        // if alert was cleared, leave only the first one
        // if alert was not cleared yet, take the last one in series
        for (String key: journalEventsMap.keySet()) {
            List<DEvent> tempList = journalEventsMap.get(key);

            Map<Long, DEvent> tempEventsMapByFirstTimestamp = new HashMap<>();
            for (DEvent te : tempList) {
                if (te.getSeverity().equalsIgnoreCase("clear")
                        && te.getClearTimestamp() != 0) continue;
                if (tempEventsMapByFirstTimestamp.containsKey(te.getFirstTimestamp())) {
                    if (te.getCounter() > tempEventsMapByFirstTimestamp.get(te.getFirstTimestamp()).getCounter()) {
                        tempEventsMapByFirstTimestamp.put(te.getFirstTimestamp(), te);
                    }
                } else {
                    tempEventsMapByFirstTimestamp.put(te.getFirstTimestamp(), te);
                }
            }

            for (Iterator<DEvent> it = tempList.iterator(); it.hasNext();) {
                DEvent e = it.next();
                // ignore clears
                if (e.getSeverity().equalsIgnoreCase("clear")) it.remove();
                // ignore cleared alerts (all except the first one)
                if (e.getClearTimestamp() != 0 && e.getCounter() > 1) it.remove();
                // ignore uncleared alerts except the last one in series (for each runtime id)
                for (Long l : tempEventsMapByFirstTimestamp.keySet()) {
                    if (!tempEventsMapByFirstTimestamp.get(l).getUid().equalsIgnoreCase(e.getUid())) {
                        it.remove();
                    }
                }
            }
        }

        // create timeline model
        model = new TimelineModel<>();

        // set initial start / end dates for the axis of the timeline
        start = LocalDate.of(2023, 7, 1).atStartOfDay();
        end = LocalDate.of(2023, 8, 1).atStartOfDay();

        long now = System.currentTimeMillis();

        for (String s : journalEventsMap.keySet()) {

            TimelineGroup<String> group = new TimelineGroup<>(s, journalEventsMap.get(s).get(0).getAlertname(), 1);
            model.addGroup(group);

            for (DEvent e : journalEventsMap.get(s)) {
                LocalDateTime startEvent = Instant.ofEpochMilli(e.getFirstTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime endEvent = Instant.ofEpochMilli(e.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime();


                TimelineEvent event = TimelineEvent.<DEvent>builder()
                        .data(e)
                        .startDate(startEvent)
                        .endDate(endEvent)
                        .editable(false)
                        .group(s)
                        .styleClass(e.getSeverity().toLowerCase())
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
