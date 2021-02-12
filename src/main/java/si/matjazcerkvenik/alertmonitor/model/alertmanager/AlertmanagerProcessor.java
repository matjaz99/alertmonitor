package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DNotification;
import si.matjazcerkvenik.alertmonitor.model.Severity;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.MD5Checksum;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

            if (n.getSeverity().equalsIgnoreCase(Severity.INFORMATIONAL)
                    || n.getSeverity().equals(Severity.INDETERMINATE)) {
                continue;
            }

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
            n.setAlertname(a.getLabels().get("alertname"));
            n.setUserAgent(m.getHeaderMap().getOrDefault("user-agent", "-"));
            n.setInfo(a.getLabels().getOrDefault("info", "-"));
            n.setInstance(a.getLabels().getOrDefault("instance", "-"));
            n.setHostname(stripInstance(n.getInstance()));
            n.setNodename(a.getLabels().getOrDefault("nodename", n.getInstance()));
            n.setJob(a.getLabels().getOrDefault("job", "-"));
            n.setTags(a.getLabels().getOrDefault("tags", ""));
            n.setSeverity(a.getLabels().getOrDefault("severity", "indeterminate"));
            n.setPriority(a.getLabels().getOrDefault("priority", "low"));
            n.setTeam(a.getLabels().getOrDefault("team", "unassigned"));
            n.setEventType(a.getLabels().getOrDefault("eventType", "5"));
            n.setProbableCause(a.getLabels().getOrDefault("probableCause", "1024"));
            n.setCurrentValue(a.getAnnotations().getOrDefault("currentValue", "-"));
            n.setUrl(a.getLabels().getOrDefault("url", "-"));
            if (a.getLabels().containsKey("description")) {
                n.setDescription(a.getLabels().getOrDefault("description", "-"));
            } else {
                n.setDescription(a.getAnnotations().getOrDefault("description", "-"));
            }
            n.setStatus(a.getStatus());
            n.setGeneratorUrl(a.getGeneratorURL());

            // set severity=clear for all events that have status=resolved, but not for those with severity=informational
            if (a.getStatus().equalsIgnoreCase("resolved")) {
                if (!n.getSeverity().equalsIgnoreCase(Severity.INFORMATIONAL)
                        && !n.getSeverity().equalsIgnoreCase(Severity.INDETERMINATE)) {
                    n.setSeverity(Severity.CLEAR);
                }
            }

            // add other labels directly into tags
            // eg: severity (but not clear and info), priority
            if (!n.getSeverity().equals(Severity.CLEAR)
                    && !n.getSeverity().equals(Severity.INFORMATIONAL)) {
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
            n.setUid(MD5Checksum.getMd5Checksum(n.getTimestamp()
                    + n.hashCode()
                    + new Random().nextInt(Integer.MAX_VALUE)
                    + n.getPriority()
                    + new Random().nextInt(Integer.MAX_VALUE)
                    + n.getAlertname()
                    + new Random().nextInt(Integer.MAX_VALUE)
                    + n.getInfo()
                    + new Random().nextInt(Integer.MAX_VALUE)
                    + n.getInstance()
                    + new Random().nextInt(Integer.MAX_VALUE)
                    + n.getDescription()
                    + new Random().nextInt(Integer.MAX_VALUE)
                    + n.getSource()
                    + new Random().nextInt(Integer.MAX_VALUE)
                    + n.getUserAgent()));

            // set correlation ID
            n.setCorrelationId(MD5Checksum.getMd5Checksum(n.getAlertname()
                    + n.getInfo()
                    + n.getInstance()
                    + n.getJob()));

            notifs.add(n);

            DAO.getLogger().info(n.toString());

        }

        return notifs;

    }

    /**
     * Remove leading protocol (eg. http://) and trailing port (eg. :8080).
     * @param instance
     * @return hostname
     */
    public static String stripInstance(String instance) {

        if (instance == null) return instance;

        // remove protocol
        if (instance.contains("://")) {
            instance = instance.split("://")[1];
        }
        // remove port
        instance = instance.split(":")[0];

        // remove relative URL
        instance = instance.split("/")[0];

        // resolve to IP address
//        try {
//            InetAddress address = InetAddress.getByName(instance);
//            instance = address.getHostAddress();
//        } catch (UnknownHostException e) {
//            // nothing to do, leave as it is
//            DAO.getLogger().warn("Cannot resolve: " + instance);
//        }
        return instance;
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
