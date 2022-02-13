package si.matjazcerkvenik.alertmonitor.data;

import java.util.TimerTask;

public class DbMaintenanceTask extends TimerTask {

    @Override
    public void run() {

        DAO.getInstance().getDataManager().cleanDB();

    }
}
