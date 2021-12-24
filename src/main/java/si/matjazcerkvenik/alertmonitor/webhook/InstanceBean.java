package si.matjazcerkvenik.alertmonitor.webhook;

import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DNotification;
import si.matjazcerkvenik.alertmonitor.model.Target;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean
@ViewScoped
public class InstanceBean {

    private Target target;

    @PostConstruct
    public void init() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String id = requestParameterMap.getOrDefault("tid", "null");
        target = DAO.getInstance().getSingleTarget(id);
        DAO.getLogger().info("Found target: " + target.toString());
    }

    public Target getTarget() {
        return target;
    }

    public List<DNotification> getInstanceActiveAlarms() {
        List<DNotification> list = new ArrayList<>(DAO.getInstance().getActiveAlerts().values());
        List<DNotification> result = list.stream()
                .filter(notif -> checkAlert(notif))
                .collect(Collectors.toList());
        Collections.sort(result, new Comparator<DNotification>() {
            @Override
            public int compare(DNotification lhs, DNotification rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getTimestamp() > rhs.getTimestamp() ? -1 : (lhs.getTimestamp() < rhs.getTimestamp()) ? 1 : 0;
            }
        });
        return result;
    }

    public List<DNotification> getInstanceJournalAlarms() {
        List<DNotification> result = DAO.getInstance().getJournal().stream()
                .filter(notif -> checkAlert(notif))
                .collect(Collectors.toList());
        Collections.sort(result, new Comparator<DNotification>() {
            @Override
            public int compare(DNotification lhs, DNotification rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getTimestamp() > rhs.getTimestamp() ? -1 : (lhs.getTimestamp() < rhs.getTimestamp()) ? 1 : 0;
            }
        });
        return result;
    }

    private boolean checkAlert(DNotification n) {
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

}
