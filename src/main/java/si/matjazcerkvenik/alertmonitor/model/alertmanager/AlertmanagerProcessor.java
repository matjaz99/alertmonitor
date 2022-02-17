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
package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DSeverity;
import si.matjazcerkvenik.alertmonitor.util.*;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.util.*;

public class AlertmanagerProcessor {

    public static void processWebhookMessage(WebhookMessage wm) throws Exception {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        AmAlertMessage am = gson.fromJson(wm.getBody(), AmAlertMessage.class);
        LogFactory.getLogger().debug(am.toString());
        LogFactory.getLogger().info("Number of alerts: " + am.getAlerts().size());

        List<DEvent> dn = convertToDNotif(wm, am);

        AmMetrics.amMessagesReceivedCount++;
        AmMetrics.lastEventTimestamp = System.currentTimeMillis();

        DAO.getInstance().addToJournal(dn);

        for (DEvent n : dn) {

            if (AmProps.ALERTMONITOR_KAFKA_ENABLED) KafkaClient.getInstance().publish(AmProps.ALERTMONITOR_KAFKA_TOPIC, Formatter.toJson(n));

            // correlation
            if (DAO.getInstance().getActiveAlerts().containsKey(n.getCorrelationId())) {
                if (n.getSeverity().equalsIgnoreCase(DSeverity.CLEAR)) {
                    DAO.getInstance().removeActiveAlert(n);
                    LogFactory.getLogger().info("Removing active alarm: " + n.getCorrelationId());
                } else {
                    DAO.getInstance().updateActiveAlert(n);
                    LogFactory.getLogger().info("Updating active alarm: " + n.getCorrelationId());
                }
            } else {
                if (!n.getSeverity().equalsIgnoreCase(DSeverity.CLEAR)) {
                    DAO.getInstance().addActiveAlert(n);
                    LogFactory.getLogger().info("Adding active alarm: " + n.getCorrelationId());
                }
            }

        }

    }

    private static List<DEvent> convertToDNotif(WebhookMessage m, AmAlertMessage am) {

        List<DEvent> notifs = new ArrayList<DEvent>();

        for (Iterator<AmAlert> it = am.getAlerts().iterator(); it.hasNext();) {
            AmAlert a = it.next();

            DEvent n = new DEvent();
            n.setTimestamp(System.currentTimeMillis());
            n.setSource(m.getRemoteHost());
            n.setAlertname(a.getLabels().getOrDefault(DEvent.KEY_ALERTNAME, "-unknown-"));
            n.setUserAgent(m.getHeaderMap().getOrDefault("user-agent", "-"));
            n.setInfo(a.getLabels().getOrDefault(DEvent.KEY_INFO, "-"));
            n.setInstance(a.getLabels().getOrDefault(DEvent.KEY_INSTANCE, "-"));
            n.setHostname(Formatter.stripInstance(n.getInstance()));
            n.setNodename(a.getLabels().getOrDefault(DEvent.KEY_NODENAME, n.getInstance()));
            n.setJob(a.getLabels().getOrDefault(DEvent.KEY_JOB, "-"));
            n.setTags(a.getLabels().getOrDefault(DEvent.KEY_TAGS, ""));
            n.setSeverity(a.getLabels().getOrDefault(DEvent.KEY_SEVERITY, "indeterminate"));
            n.setPriority(a.getLabels().getOrDefault(DEvent.KEY_PRIORITY, "low"));
            n.setGroup(a.getLabels().getOrDefault(DEvent.KEY_GROUP, "unknown"));
            n.setEventType(a.getLabels().getOrDefault(DEvent.KEY_EVENTTYPE, "5"));
            n.setProbableCause(a.getLabels().getOrDefault(DEvent.KEY_PROBABLECAUSE, "1024"));
            n.setCurrentValue(a.getAnnotations().getOrDefault(DEvent.KEY_CURRENTVALUE, "-"));
            n.setUrl(a.getLabels().getOrDefault(DEvent.KEY_URL, ""));
            if (a.getLabels().containsKey(DEvent.KEY_DESCRIPTION)) {
                n.setDescription(a.getLabels().getOrDefault(DEvent.KEY_DESCRIPTION, "-"));
            } else {
                n.setDescription(a.getAnnotations().getOrDefault(DEvent.KEY_DESCRIPTION, "-"));
            }
            n.setStatus(a.getStatus());
            n.setGeneratorUrl(a.getGeneratorURL());

            // set prometheusId
            String[] lblArray = AmProps.ALERTMONITOR_PROMETHEUS_ID_LABELS.split(",");
            String s = "{";
            for (int i = 0; i < lblArray.length; i++) {
                s += lblArray[i].trim() + "=\"" + a.getLabels().getOrDefault(lblArray[i].trim(), "-") + "\", ";
            }
            s = s.substring(0, s.length()-2) + "}";
            n.setPrometheusId(s);

            // set all other labels
            n.setOtherLabels(a.getLabels());

            // set severity=clear for all events that have status=resolved, but not for those with severity=informational
            if (a.getStatus().equalsIgnoreCase("resolved")) {
                n.setSeverity(DSeverity.CLEAR);
            }

            // add other labels directly into tags
            // eg: severity (but not clear), priority
            if (!n.getSeverity().equals(DSeverity.CLEAR)) {
                n.setTags(n.getTags() + "," + n.getSeverity());
            }
            n.setTags(n.getTags() + "," + n.getPriority());

            // environment variable substitution
            n.setNodename(substitute(n.getNodename()));
            n.setInfo(substitute(n.getInfo()));
            n.setDescription(substitute(n.getDescription()));
            n.setTags(substitute(n.getTags()));
            n.setUrl(substitute(n.getUrl()));

            // set unique ID of event
            n.generateUID();

            // set correlation ID
            n.generateCID();

            notifs.add(n);

            LogFactory.getLogger().info(n.toString());

        }

        return notifs;

    }



    /**
     * This method does environment variable substitution
     */
    public static String substitute(String s) {

        if (s == null || !s.contains("${")) {
            return s;
        }

        Map<String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
            s = s.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return s;

    }

}
