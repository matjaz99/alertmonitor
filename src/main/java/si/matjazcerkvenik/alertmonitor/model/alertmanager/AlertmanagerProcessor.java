package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DNotification;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.MD5Checksum;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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

        AmMetrics.alertmonitor_journal_messages_total.inc(dn.size());

        // resynchronization

        for (DNotification n : dn) {
            if (n.getSeverity().equalsIgnoreCase("informational")) {
                continue;
            }
            if (DAO.getInstance().getActiveAlerts().containsKey(n.getAlertId())) {
                if (n.getSeverity().equalsIgnoreCase("clear")) {
                    System.out.println("Removing active alarm: " + n.getAlertId());
                    DAO.getInstance().removeActiveAlert(n);
                    DAO.clearingEventCount++;
                    AmMetrics.alertmonitor_alerts_total.labels("clearing").inc();
                } else {
                    DAO.getInstance().updateActiveAlert(n);
                    System.out.println("Updating active alarm: " + n.getAlertId());
                }
            } else {
                if (!n.getSeverity().equalsIgnoreCase("clear")) {
                    DAO.getInstance().addActiveAlert(n);
                    DAO.raisingEventCount++;
                    AmMetrics.alertmonitor_alerts_total.labels("raising").inc();
                    System.out.println("Adding active alarm: " + n.getAlertId());
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

            if (m.getHeaderMap().containsKey("user-agent")) {
                n.setUserAgent(m.getHeaderMap().get("user-agent"));
            } else {
                n.setUserAgent("-");
            }

            if (a.getLabels().containsKey("sourceinfo")) {
                n.setSourceinfo(a.getLabels().get("sourceinfo"));
            } else {
                n.setSourceinfo("-");
            }

            if (a.getLabels().containsKey("instance")) {
                n.setInstance(a.getLabels().get("instance"));
            } else {
                n.setInstance("-");
            }

            if (a.getLabels().containsKey("nodename")) {
                n.setNodename(a.getLabels().get("nodename"));
            } else {
                n.setNodename(n.getInstance());
            }

            if (a.getLabels().containsKey("job")) {
                n.setJob(a.getLabels().get("job"));
            } else {
                n.setJob("-");
            }

            if (a.getLabels().containsKey("tags")) {
                n.setTags(a.getLabels().get("tags"));
            } else {
                n.setTags("");
            }

            if (a.getLabels().containsKey("severity")) {
                n.setSeverity(a.getLabels().get("severity"));
            } else {
                n.setSeverity("indeterminate");
            }

            if (a.getLabels().containsKey("priority")) {
                n.setPriority(a.getLabels().get("priority"));
            } else {
                n.setPriority("low");
            }

            if (a.getStatus().equalsIgnoreCase("resolved")) {
                // set severity=clear for all events that have status=resolved, but not for those with severity=informational
                if (!n.getSeverity().equalsIgnoreCase("informational")) {
                    n.setSeverity("clear");
                }
            }

            if (a.getAnnotations().containsKey("summary")) {
                n.setSummary(a.getAnnotations().get("summary"));
            } else {
                n.setSummary("-");
            }

            if (a.getAnnotations().containsKey("description")) {
                n.setDescription(a.getAnnotations().get("description"));
            } else {
                n.setDescription("-");
            }

            n.setStatus(a.getStatus());

            n.setUid(MD5Checksum.getMd5Checksum(n.getTimestamp() + n.hashCode()
                    + n.getPriority() + n.getAlertname() + new Random().nextInt(9999999)
                    + n.getSourceinfo() + n.getInstance() + n.getSummary()
                    + n.getDescription() + new Random().nextInt(9999999) + n.getSource()
                    + n.getUserAgent()));

            n.setAlertId(MD5Checksum.getMd5Checksum(n.getAlertname() + n.getSourceinfo()
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
