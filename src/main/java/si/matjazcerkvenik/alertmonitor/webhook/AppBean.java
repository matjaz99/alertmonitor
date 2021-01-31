package si.matjazcerkvenik.alertmonitor.webhook;

import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.ResyncTask;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.util.Timer;

@ManagedBean
@ApplicationScoped
public class AppBean {

    private Timer resyncTimer = null;
    private ResyncTask resyncTask = null;
    private static int resyncInterval = 60;

    public AppBean() {

        if (resyncTimer == null) {
            DAO.getLogger().info("Start Resync timer with period=" + resyncInterval);
            if (resyncInterval > 0) {
                resyncTimer = new Timer("ResyncTimer");
                resyncTask = new ResyncTask();
                resyncTimer.schedule(resyncTask, 5 * 1000, resyncInterval * 1000);
            }
        }


    }

}
