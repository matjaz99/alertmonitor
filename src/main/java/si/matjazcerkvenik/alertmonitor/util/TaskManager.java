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
package si.matjazcerkvenik.alertmonitor.util;

import si.matjazcerkvenik.alertmonitor.data.DbMaintenanceTask;
import si.matjazcerkvenik.alertmonitor.model.PrometheusSyncTask;

import java.util.Timer;

public class TaskManager {

    private static TaskManager taskManager;

    private Timer pSyncTimer = null;
    private PrometheusSyncTask prometheusSyncTask = null;

    private Timer dbMaintenanceTimer = null;
    private DbMaintenanceTask dbMaintenanceTask = null;

    private TaskManager() {}

    public static TaskManager getInstance() {
        if (taskManager == null) taskManager = new TaskManager();
        return taskManager;
    }

    public void restartPsyncTimer() {

        stopPsyncTimer();

        if (AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC == 0) {
            LogFactory.getLogger().info("PSync is disabled");
        }

        // start resync timer
        if (prometheusSyncTask == null) {
            LogFactory.getLogger().info("Start periodic sync task with period=" + AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC);
            AmMetrics.alertmonitor_psync_interval_seconds.set(AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC);
            if (AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC > 0) {
                pSyncTimer = new Timer("PSyncTimer");
                prometheusSyncTask = new PrometheusSyncTask();
                pSyncTimer.schedule(prometheusSyncTask, 5 * 1000, AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC * 1000);
            }
        }

    }

    public void stopPsyncTimer() {
        if (pSyncTimer != null) {
            pSyncTimer.cancel();
            pSyncTimer = null;
        }
        if (prometheusSyncTask != null) {
            prometheusSyncTask.cancel();
            prometheusSyncTask = null;
        }
    }

    public void startDbMaintenanceTimer() {

        if (dbMaintenanceTask == null) {
            LogFactory.getLogger().info("Start DB Maintenance Task");
            dbMaintenanceTimer = new Timer("DbMaintenanceTimer");
            dbMaintenanceTask = new DbMaintenanceTask();
            dbMaintenanceTimer.schedule(dbMaintenanceTask, 23 * 1000, 1 * 3600 * 1000);
        }

    }

}
