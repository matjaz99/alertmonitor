/*
   Copyright 2021 Matjaž Cerkvenik

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
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
