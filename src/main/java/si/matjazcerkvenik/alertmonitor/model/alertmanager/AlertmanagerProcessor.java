package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DNotification;
import si.matjazcerkvenik.alertmonitor.model.Severity;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.KafkaClient;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.util.*;

public class AlertmanagerProcessor {

    public static void processWebhookMessage(WebhookMessage m) {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        AmAlertMessage am = gson.fromJson(m.getBody(), AmAlertMessage.class);
        DAO.getLogger().info(am.toString());
        DAO.getLogger().info("Number of alerts: " + am.getAlerts().size());

        List<DNotification> dn = convertToDNotif(m, am);

        DAO.amMessagesReceivedCount++;
        DAO.lastEventTimestamp = System.currentTimeMillis();

        for (DNotification n : dn) {

            DAO.getInstance().addToJournal(n);

            if (DAO.ALERTMONITOR_KAFKA_ENABLED) KafkaClient.getInstance().publish(DAO.ALERTMONITOR_KAFKA_TOPIC, Formatter.toJson(n));

//            if (n.getSeverity().equalsIgnoreCase(Severity.INFORMATIONAL)
//                    || n.getSeverity().equals(Severity.INDETERMINATE)) {
//                continue;
//            }

            // correlation
            if (DAO.getInstance().getActiveAlerts().containsKey(n.getCorrelationId())) {
                if (n.getSeverity().equalsIgnoreCase(Severity.CLEAR)) {
                    DAO.getInstance().removeActiveAlert(n);
                    DAO.getLogger().info("Removing active alarm: " + n.getCorrelationId());
                } else {
                    DAO.getInstance().updateActiveAlert(n);
                    DAO.getLogger().info("Updating active alarm: " + n.getCorrelationId());
                }
            } else {
                if (!n.getSeverity().equalsIgnoreCase(Severity.CLEAR)) {
                    DAO.getInstance().addActiveAlert(n);
                    DAO.getLogger().info("Adding active alarm: " + n.getCorrelationId());
                }
            }

        }

    }

    private static List<DNotification> convertToDNotif(WebhookMessage m, AmAlertMessage am) {

        List<DNotification> notifs = new ArrayList<DNotification>();

        for (Iterator<AmAlert> it = am.getAlerts().iterator(); it.hasNext();) {
            AmAlert a = it.next();

            DNotification n = new DNotification();
            n.setTimestamp(System.currentTimeMillis());
            n.setSource(m.getRemoteHost());
            n.setAlertname(a.getLabels().getOrDefault("alertname", "-unknown-"));
            n.setUserAgent(m.getHeaderMap().getOrDefault("user-agent", "-"));
            n.setInfo(a.getLabels().getOrDefault("info", "-"));
            n.setInstance(a.getLabels().getOrDefault("instance", "-"));
            n.setHostname(DAO.getInstance().stripInstance(n.getInstance()));
            n.setNodename(a.getLabels().getOrDefault("nodename", n.getInstance()));
            n.setJob(a.getLabels().getOrDefault("job", "-"));
            n.setTags(a.getLabels().getOrDefault("tags", ""));
            n.setSeverity(a.getLabels().getOrDefault("severity", "indeterminate"));
            n.setPriority(a.getLabels().getOrDefault("priority", "low"));
            n.setTeam(a.getLabels().getOrDefault("team", "unassigned"));
            n.setEventType(a.getLabels().getOrDefault("eventType", "5"));
            n.setProbableCause(a.getLabels().getOrDefault("probableCause", "1024"));
            n.setCurrentValue(a.getAnnotations().getOrDefault("currentValue", "-"));
            n.setUrl(a.getLabels().getOrDefault("url", ""));
            if (a.getLabels().containsKey("description")) {
                n.setDescription(a.getLabels().getOrDefault("description", "-"));
            } else {
                n.setDescription(a.getAnnotations().getOrDefault("description", "-"));
            }
            n.setStatus(a.getStatus());
            n.setGeneratorUrl(a.getGeneratorURL());
            n.setOtherLabels(a.getLabels());

            // set severity=clear for all events that have status=resolved, but not for those with severity=informational
            if (a.getStatus().equalsIgnoreCase("resolved")) {
                n.setSeverity(Severity.CLEAR);
            }

            // add other labels directly into tags
            // eg: severity (but not clear), priority
            if (!n.getSeverity().equals(Severity.CLEAR)) {
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

            DAO.getLogger().info(n.toString());

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
