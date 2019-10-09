package si.matjazcerkvenik.alertmonitor.webhook;

import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DNotification;
import si.matjazcerkvenik.alertmonitor.model.Severity;
import si.matjazcerkvenik.alertmonitor.model.Target;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ManagedBean
@ViewScoped
public class TargetBean {

    private Target target;

    @PostConstruct
    public void init() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String id = requestParameterMap.getOrDefault("target_id", "null");
        List<Target> list = new ArrayList<Target>(DAO.getInstance().getTargets());
        List<Target> result = list.stream()
                .filter(t -> t.getId().equals(id))
                .collect(Collectors.toList());
        target = result.get(0);
        DAO.getLogger().info("Found target: " + target.toString());
    }



}
