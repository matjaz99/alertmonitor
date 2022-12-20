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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import si.matjazcerkvenik.alertmonitor.data.DbMaintenanceTask;

import java.util.Timer;

public class TaskManager {

    private static TaskManager taskManager;

    private Timer dbMaintenanceTimer = null;
    private DbMaintenanceTask dbMaintenanceTask = null;

    private TaskManager() {}

    public static TaskManager getInstance() {
        if (taskManager == null) taskManager = new TaskManager();
        return taskManager;
    }


    public void startDbMaintenanceTask() {

        if (dbMaintenanceTask == null) {
            LogFactory.getLogger().info("Start DB Maintenance Task");
            dbMaintenanceTimer = new Timer("DbMaintenanceTimer");
            dbMaintenanceTask = new DbMaintenanceTask();
            dbMaintenanceTimer.schedule(dbMaintenanceTask, 23 * 1000, 1 * 3600 * 1000);
        }

    }

    public void stopDbMaintenanceTask() {
        if (dbMaintenanceTimer != null) {
            dbMaintenanceTimer.cancel();
            dbMaintenanceTimer = null;
        }
        if (dbMaintenanceTask != null) {
            dbMaintenanceTask.cancel();
            dbMaintenanceTask = null;
        }
    }

    public String getVersionFromGithub() {
        try {

            OkHttpClient httpClient = HttpClientFactory.instantiateHttpClient(true, 10, 30, null, null);

            Request request = new Request.Builder()
                    .url("https://raw.githubusercontent.com/matjaz99/alertmonitor/master/src/main/webapp/WEB-INF/version.txt")
                    .addHeader("User-Agent", "Alertmonitor/v1")
                    .get()
                    .build();

            Response response = httpClient.newCall(request).execute();

            String code = Integer.toString(response.code());
            String responseBody = "";

            if (response.body() != null) {
                responseBody = response.body().string();
            }

            response.close();

            LogFactory.getLogger().info("alertmonitor update check: " + code + " response: " + responseBody);

            return responseBody;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
