package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DNotification;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.MD5Checksum;
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
        DAO.getInstance().addToJournal(dn);
        DAO.amMessagesReceivedCount++;
        DAO.journalReceivedCount = DAO.journalReceivedCount + dn.size();

        DAO.lastEventTimestamp = System.currentTimeMillis();

        // correlation
        for (DNotification n : dn) {

            AmMetrics.alertmonitor_journal_messages_total.labels(n.getSeverity()).inc();

            if (n.getSeverity().equalsIgnoreCase("informational")) {
                continue;
            }

            if (DAO.getInstance().getActiveAlerts().containsKey(n.getCorrelationId())) {
                if (n.getSeverity().equalsIgnoreCase("clear")) {
                    DAO.getInstance().removeActiveAlert(n);
                    DAO.getLogger().info("Removing active alarm: " + n.getCorrelationId());
                } else {
                    DAO.getInstance().updateActiveAlert(n);
                    DAO.getLogger().info("Updating active alarm: " + n.getCorrelationId());
                }
            } else {
                if (!n.getSeverity().equalsIgnoreCase("clear")) {
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
            n.setNodename(a.getLabels().getOrDefault("nodename", n.getInstance()));
            n.setJob(a.getLabels().getOrDefault("job", "-"));
            n.setTags(a.getLabels().getOrDefault("tags", ""));
            n.setSeverity(a.getLabels().getOrDefault("severity", "indeterminate"));
            n.setPriority(a.getLabels().getOrDefault("priority", "low"));
            n.setSummary(a.getLabels().getOrDefault("summary", "-"));
            n.setTeam(a.getLabels().getOrDefault("team", "unassigned"));
            n.setEventType(a.getLabels().getOrDefault("eventType", "5"));
            n.setProbableCause(a.getLabels().getOrDefault("probableCause", "1024"));
            n.setCurrentValue(a.getLabels().getOrDefault("currentValue", "-"));
            n.setUrl(a.getLabels().getOrDefault("url", "-"));
            if (a.getLabels().containsKey("description")) {
                n.setDescription(a.getLabels().getOrDefault("description", "-"));
            } else {
                n.setDescription(a.getAnnotations().getOrDefault("description", "-"));
            }
            n.setStatus(a.getStatus());

            // set severity=clear for all events that have status=resolved, but not for those with severity=informational
            if (a.getStatus().equalsIgnoreCase("resolved")) {
                if (!n.getSeverity().equalsIgnoreCase("informational")
                        && !n.getSeverity().equalsIgnoreCase("indeterminate")) {
                    n.setSeverity("clear");
                }
            }

            // set unique ID of event
            n.setUid(MD5Checksum.getMd5Checksum(n.getTimestamp()
                    + n.hashCode()
                    + n.getPriority()
                    + n.getAlertname()
                    + new Random().nextInt(9999999)
                    + n.getInfo()
                    + n.getInstance()
                    + new Random().nextInt(9999999)
                    + n.getSummary()
                    + n.getDescription()
                    + new Random().nextInt(9999999)
                    + n.getSource()
                    + n.getUserAgent()));

            // set correlation ID
            n.setCorrelationId(MD5Checksum.getMd5Checksum(n.getAlertname()
                    + n.getInfo()
                    + n.getInstance()
                    + n.getSummary()));

            notifs.add(n);

        }

        return notifs;

    }

}
