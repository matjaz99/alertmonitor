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
package si.matjazcerkvenik.alertmonitor.data;

import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DTag;
import si.matjazcerkvenik.alertmonitor.model.TagColors;
import si.matjazcerkvenik.alertmonitor.model.Target;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PRule;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PTarget;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiException;
import si.matjazcerkvenik.alertmonitor.util.*;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public class DAO {

    /** Singleton instance */
    private static DAO instance;


    /** List of webhook messages in its raw form. */
    private List<WebhookMessage> webhookMessages = new LinkedList<>();

    /** Journal of events, limited by JOURNAL_TABLE_SIZE */
    private List<DEvent> journal = new LinkedList<>();

    /** Map of active alerts. Key is correlation id */
    private Map<String, DEvent> activeAlerts = new HashMap<>();

    /** Map of tags of active alerts. Key is the tag name */
    private Map<String, DTag> tagMap = new HashMap<>();

    private String localIpAddress;

    private DAO() {
    }

    public static DAO getInstance() {
        if (instance == null) {
            if (DAOInterface.MONGO_ENABLED) {
                //instance = new MongoDbDAO();
            } else {
                instance = new DAO();
            }
        }
        return instance;
    }



    /**
     * Add new webhook message to the list. Also delete oldest messages.
     * @param message incoming message
     */
    public void addWebhookMessage(WebhookMessage message) {
        // webhook messages can be 1% of journal size
        while (webhookMessages.size() > AmProps.JOURNAL_TABLE_SIZE / 100) {
            webhookMessages.remove(0);
        }
        webhookMessages.add(message);
        AmMetrics.webhookMessagesReceivedCount++;
        AmMetrics.alertmonitor_webhook_messages_received_total.labels(message.getRemoteHost(), message.getMethod().toUpperCase()).inc();
    }

    public List<WebhookMessage> getWebhookMessages() {
        return webhookMessages;
    }

    /**
     * Add new notification to journal. Also delete oldest notifications.
     * @param event notification
     */
    public void addToJournal(DEvent event) {
        while (journal.size() > AmProps.JOURNAL_TABLE_SIZE) {
            DEvent m = journal.remove(0);
            LogFactory.getLogger().info("Purging journal: " + m.getUid());
        }
        journal.add(event);
        LogFactory.getLogger().info("Adding to journal: " + event.getUid());
        AmMetrics.journalReceivedCount++;
        AmMetrics.alertmonitor_journal_messages_total.labels(event.getSeverity()).inc();
    }

    /**
     * Return whole journal
     * @return list
     */
    public List<DEvent> getJournal() {
        return journal;
    }

    /**
     * Get single notification from journal
     * @param id unique ID of notification
     * @return notification
     */
    public DEvent getEvent(String id) {

        List<PRule> ruleList = new ArrayList<>();

        try {
            PrometheusApi api = new PrometheusApi();
            ruleList = api.rules();

        } catch (PrometheusApiException e) {
            LogFactory.getLogger().error("DAO: failed to load rules for alert: " + id + "; root cause: " + e.getMessage());
        }

        for (DEvent n : journal) {
            if (n.getUid().equals(id)) {
                for (PRule r : ruleList) {
                    if (n.getAlertname().equals(r.getName())) {
                        n.setRuleExpression(r.getQuery());
                        n.setRuleTimeLimit(r.getDuration());
                    }
                }
                return n;
            }
        }

        return null;
    }

    /**
     * Add new alert to active alerts. This method is called when first alert
     * of this type occurs (according to correlationId). First and last timestamps
     * are set to time of reception (timestamp). Also new tags are added to tagMap.
     * @param n notification
     */
    public void addActiveAlert(DEvent n) {

        n.setFirstTimestamp(n.getTimestamp());
        n.setLastTimestamp(n.getTimestamp());

        activeAlerts.put(n.getCorrelationId(), n);
        AmMetrics.raisingEventCount++;

        // parse tags from tags label
        String[] array = n.getTags().split(",");
        for (int i = 0; i < array.length; i++) {
            String tagName = array[i].trim();
            if (tagName.length() > 0) {
                DTag t = new DTag(tagName, TagColors.getColor(tagName));
                addTag(t);
            }
        }

    }

    /**
     * Return a map of active alerts
     * @return map of all active alerts
     */
    public Map<String, DEvent> getActiveAlerts() {
        return activeAlerts;
    }

    /**
     * New alert has appeared and must replace existing active alert. Since this
     * is new occurrence of existing alert, the firstTimestamp is overwritten
     * by existing firstTimestamp (time when alert occurred for the first time).
     * The lastTimestamp is set to time of reception and counter is increased by 1
     * (according to existing alert).
     * Alert then finally replaces reference in activeAlert map so it points to new
     * alert.
     * @param newNotif last received notificatioin
     */
    public void updateActiveAlert(DEvent newNotif) {
        DEvent existingNotif = activeAlerts.get(newNotif.getCorrelationId());
        // update existing alert
//        existingNotif.setLastTimestamp(newNotif.getTimestamp());
//        if (!newNotif.getSource().equalsIgnoreCase("RESYC")) {
//            // don't count resync alerts
//            existingNotif.setCounter(existingNotif.getCounter() + 1);
//            existingNotif.setSource(newNotif.getSource());
//            existingNotif.setGeneratorUrl(newNotif.getGeneratorUrl());
//        }
//        if (!newNotif.getCurrentValue().equals("-")) {
//            existingNotif.setCurrentValue(newNotif.getCurrentValue());
//        }
        // update new alert
        newNotif.setFirstTimestamp(existingNotif.getFirstTimestamp());
        newNotif.setLastTimestamp(newNotif.getTimestamp());
        if (newNotif.getSource().equalsIgnoreCase("RESYC")) {
            // resync alert
        } else {
            // regular alert
            newNotif.setCounter(existingNotif.getCounter() + 1);
        }
        activeAlerts.put(existingNotif.getCorrelationId(), newNotif);
    }

    /**
     * Clear arrived and active alert must be removed. Before removing,
     * all alerts in journal have clearTimestamp corrected to point to clear event.
     * @param n
     */
    public void removeActiveAlert(DEvent n) {
        for (DEvent jNotif : journal) {
            if (jNotif.getCorrelationId().equals(n.getCorrelationId())
                    && jNotif.getClearTimestamp() == 0) {
                jNotif.setClearTimestamp(n.getTimestamp());
                jNotif.setClearUid(n.getUid());
            }
        }
        n.setFirstTimestamp(n.getTimestamp());
        n.setLastTimestamp(n.getTimestamp());
        activeAlerts.remove(n.getCorrelationId());
        removeObsoleteTags();
        AmMetrics.clearingEventCount++;
    }

    /**
     * Remove tags which have no active alerts left.
     */
    private void removeObsoleteTags() {
        Map<String, Object> allTags = new HashMap<String, Object>();
        for (DEvent n : activeAlerts.values()) {
            String[] array = n.getTags().split(",");
            for (int i = 0; i < array.length; i++) {
                String tagName = array[i].trim();
                if (tagName.length() > 1) {
                    allTags.putIfAbsent(tagName, null);
                }
            }
        }
        if (allTags.isEmpty()) {
            tagMap.clear();
            return;
        }
//        for (String n : tagMap.keySet()) {
//            if (!allTags.containsKey(n)) {
//                tagMap.remove(n);
//            }
//        }
//        Iterator<Map.Entry<String, DTag>> it = tagMap.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry<String, DTag> entry = it.next();
//            if(!allTags.containsKey(entry.getKey())){
//                it.remove();
//            }
//        }
        tagMap.entrySet().removeIf(entry -> !allTags.containsKey(entry.getKey()));

    }

    /**
     * Add new tag if it does not exist yet.
     * @param tag new tag
     */
    public void addTag(DTag tag) {
        tagMap.putIfAbsent(tag.getName(), tag);
    }

    /**
     * Return list of tags.
     * @return list
     */
    public List<DTag> getTags() {
        return new ArrayList<>(tagMap.values());
    }



    /**
     * Return a list of active alerts filtered by severity.
     * @param severity severity
     * @return list
     */
    public List<DEvent> getActiveAlarmsList(String severity) {
        List<DEvent> list = activeAlerts.values().stream()
                .filter(notif -> notif.getSeverity().equals(severity))
                .collect(Collectors.toList());
        return list;

    }

    /**
     * Return a list of targets (instances) from Prometheus.
     * @return list
     */
    public List<Target> getTargets() {

        try {
            PrometheusApi api = new PrometheusApi();
            List<PTarget> pTargets = api.targets();
            Map<String, Target> targetsMap = new HashMap<String, Target>();

            // convert from PTarget to Target
            for (PTarget pTarget : pTargets) {
                String host = pTarget.getLabels().get("instance");
                Target t = targetsMap.getOrDefault(host, new Target());
                t.setSmartTarget(false);
                t.setHealth(pTarget.getHealth());
                t.setHostname(host);
                t.setJob(pTarget.getLabels().get("job"));
                t.setId(MD5.getChecksum("host" + t.getHostname() + t.getJob()));
                // load active alerts
                for (DEvent n : getActiveAlerts().values()) {
                    if (n.getInstance().equals(host)) t.addAlert(n);
                }
                targetsMap.put(t.getId(), t);
            }

            return new ArrayList<>(targetsMap.values());

        } catch (Exception e) {
            LogFactory.getLogger().error("DAO: failed getting targets; root cause: " + e.getMessage());
        }

        return null;
    }

    // the only difference is stripped hostname
    public List<Target> getSmartTargets() {

        try {
            PrometheusApi api = new PrometheusApi();
            List<PTarget> pTargets = api.targets();
            Map<String, Target> targetsMap = new HashMap<String, Target>();

            // convert from PTarget to Target
            for (PTarget pTarget : pTargets) {
                String host = Formatter.stripInstance(pTarget.getLabels().get("instance"));
                Target t = targetsMap.getOrDefault(host, new Target());
                t.setSmartTarget(true);
                boolean up = false;
                if (pTarget.getHealth().equalsIgnoreCase("up")) up = true;
                t.setUp(up || t.isUp());
                t.setHostname(host);
                t.setId(MD5.getChecksum("smarthost" + t.getHostname()));
                // load active alerts
                for (DEvent n : getActiveAlerts().values()) {
                    if (n.getHostname().equals(host)) t.addAlert(n);
                }
                targetsMap.put(host, t);
            }

            return new ArrayList<>(targetsMap.values());

        } catch (PrometheusApiException e) {
            LogFactory.getLogger().error("DAO: failed getting targets; root cause: " + e.getMessage());
        }

        return null;

    }

    public Target getSingleTarget(String id) {
        List<Target> t1 = getTargets();
        for (Target t : t1) {
            if (t.getId().equals(id)) return t;
        }
        List<Target> t2 = getSmartTargets();
        for (Target t : t2) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }



    private List<Target> getTargetsFromProm() {

        try {
            PrometheusApi api = new PrometheusApi();
            List<PTarget> targets = api.targets();

            for (PTarget pTarget : targets) {
                Target t = new Target();
                t.setHostname(Formatter.stripInstance(pTarget.getDiscoveredLabels().get("__address__")));
                t.setId(MD5.getChecksum("host" + t.getHostname()));
            }

            // TODO

        } catch (Exception e) {
            LogFactory.getLogger().error("Exception getting targets", e);
        }

        return new ArrayList<>();
    }


    public double calculateAlertsBalanceFactor() {
        if (activeAlerts.size() == 0) return 0;
        double d = (5 * getActiveAlarmsList("critical").size()
                + 4 * getActiveAlarmsList("major").size()
                + 3 * getActiveAlarmsList("minor").size()
                + 2 * getActiveAlarmsList("warning").size()) * 1.0 / activeAlerts.size();
        return d;
    }

    public String getLocalIpAddress() {
        if (localIpAddress != null) return localIpAddress;
        try {
            localIpAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            localIpAddress = "UnknownHost";
        }
        return localIpAddress;
    }



}