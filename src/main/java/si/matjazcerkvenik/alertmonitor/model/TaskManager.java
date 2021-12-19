package si.matjazcerkvenik.alertmonitor.model;

import si.matjazcerkvenik.alertmonitor.util.AmMetrics;

import java.util.Timer;

public class TaskManager {

    private static TaskManager taskManager;

    private Timer pSyncTimer = null;
    private PSyncTask pSyncTask = null;

    private TaskManager() {}

    public static TaskManager getInstance() {
        if (taskManager == null) taskManager = new TaskManager();
        return taskManager;
    }

    public void restartPsyncTimer() {

        stopPsyncTimer();

        // start resync timer
        if (pSyncTask == null) {
            DAO.getLogger().info("Start periodic sync task with period=" + DAO.ALERTMONITOR_PSYNC_INTERVAL_SEC);
            AmMetrics.alertmonitor_psync_interval_seconds.set(DAO.ALERTMONITOR_PSYNC_INTERVAL_SEC);
            if (DAO.ALERTMONITOR_PSYNC_INTERVAL_SEC > 0) {
                pSyncTimer = new Timer("PSyncTimer");
                pSyncTask = new PSyncTask();
                pSyncTimer.schedule(pSyncTask, 5 * 1000, DAO.ALERTMONITOR_PSYNC_INTERVAL_SEC * 1000);
            }
        }

    }

    public void stopPsyncTimer() {
        if (pSyncTimer != null) {
            pSyncTimer.cancel();
            pSyncTimer = null;
        }
        if (pSyncTask != null) {
            pSyncTask.cancel();
            pSyncTask = null;
        }
    }

}
