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

    public static void processAlertmanagerMessage(WebhookMessage m) {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        AmAlertMessage am = gson.fromJson(m.getBody(), AmAlertMessage.class);
        System.out.println(am.toString());
        System.out.println("Number of alerts: " + am.getAlerts().size());
        //amMessages.add(am);

        List<DNotification> dn = convertToDNotif(m, am);
        DAO.getInstance().addToJournal(dn);
        DAO.amMessagesReceivedCount++;
        DAO.journalReceivedCount = DAO.journalReceivedCount + dn.size();

        for (DNotification a : dn) {
            AmMetrics.alertmonitor_journal_messages_total.labels(a.getSeverity()).inc();
        }

        // resynchronization

        for (DNotification n : dn) {
            if (n.getSeverity().equalsIgnoreCase("informational")) {
                continue;
            }
            if (DAO.getInstance().getActiveAlerts().containsKey(n.getCorrelationId())) {
                if (n.getSeverity().equalsIgnoreCase("clear")) {
                    System.out.println("Removing active alarm: " + n.getCorrelationId());
                    DAO.getInstance().removeActiveAlert(n);
                    DAO.clearingEventCount++;
                } else {
                    DAO.getInstance().updateActiveAlert(n);
                    System.out.println("Updating active alarm: " + n.getCorrelationId());
                }
            } else {
                if (!n.getSeverity().equalsIgnoreCase("clear")) {
                    DAO.getInstance().addActiveAlert(n);
                    DAO.raisingEventCount++;
                    System.out.println("Adding active alarm: " + n.getCorrelationId());
                }
            }
            DAO.lastEventTimestamp = System.currentTimeMillis();
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
            n.setSourceinfo(a.getLabels().getOrDefault("sourceinfo", "-"));
            n.setInstance(a.getLabels().getOrDefault("instance", "-"));
            n.setNodename(a.getLabels().getOrDefault("nodename", n.getInstance()));
            n.setJob(a.getLabels().getOrDefault("job", "-"));
            n.setTags(a.getLabels().getOrDefault("tags", "-"));
            n.setSeverity(a.getLabels().getOrDefault("severity", "indeterminate"));
            n.setPriority(a.getLabels().getOrDefault("priority", "low"));
            n.setSummary(a.getAnnotations().getOrDefault("summary", "-"));
            n.setDescription(a.getAnnotations().getOrDefault("description", "-"));
            n.setStatus(a.getStatus());

            // set severity=clear for all events that have status=resolved, but not for those with severity=informational
            if (a.getStatus().equalsIgnoreCase("resolved")) {
                if (!n.getSeverity().equalsIgnoreCase("informational")) {
                    n.setSeverity("clear");
                }
            }

            // set unique ID of event
            n.setUid(MD5Checksum.getMd5Checksum(n.getTimestamp() + n.hashCode()
                    + n.getPriority() + n.getAlertname() + new Random().nextInt(9999999)
                    + n.getSourceinfo() + n.getInstance() + n.getSummary()
                    + n.getDescription() + new Random().nextInt(9999999) + n.getSource()
                    + n.getUserAgent()));

            // set correlation ID
            n.setCorrelationId(MD5Checksum.getMd5Checksum(n.getAlertname() + n.getSourceinfo()
                    + n.getInstance() + n.getSummary()));

//			DNotification found = null;
//			for (Iterator<DNotification> it1 = dNotifs.iterator(); it1.hasNext();) {
//				DNotification dn = it1.next();
//				if (dn.getUid().equalsIgnoreCase(n.getUid())) {
//					found = dn;
//					if (n.getSeverity().equalsIgnoreCase("clear")) {
//
//					}
//					break;
//				}
//			}
//			if (found == null) {
//				notifs.add(n);
//			}
            notifs.add(n);

        }

        return notifs;

    }

}
