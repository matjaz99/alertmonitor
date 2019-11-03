package si.matjazcerkvenik.alertmonitor.webhook;

import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DNotification;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.util.*;

@ManagedBean
@RequestScoped
public class AlertBean {

    private DNotification notif;

    @PostConstruct
    public void init() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String id = requestParameterMap.getOrDefault("uid", "null");
        notif = DAO.getInstance().getNotification(id);
        if (notif == null) Growl.showWarningGrowl("Object not found", null);
        //DAO.getLogger().info("Found alert: " + notif.toString());
    }

    public DNotification getNotif() {
        return notif;
    }

}
