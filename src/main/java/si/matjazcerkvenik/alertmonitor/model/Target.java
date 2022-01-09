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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Target {

    private String id;
    private String hostname;
    private boolean smartTarget;
    private String job;
    private boolean up;
    private Map<String, DEvent> alerts = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public boolean isSmartTarget() {
        return smartTarget;
    }

    public void setSmartTarget(boolean smartTarget) {
        this.smartTarget = smartTarget;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public void setHealth(String health) {
        if (health.equalsIgnoreCase("up")) {
            this.up = true;
            return;
        }
        this.up = false;
    }

    public List<DEvent> getAlerts() {
        return new ArrayList<>(alerts.values());
    }

    // for test.xhtml
    public List<DEvent> getAlertsBySeverity(String severity) {
        return alerts.values().stream()
                .filter(alert -> alert.getSeverity().toLowerCase().equalsIgnoreCase(severity))
                .collect(Collectors.toList());
    }

    public void addAlert(DEvent notification) {
        if (!alerts.containsKey(notification.getUid())) alerts.put(notification.getUid(), notification);
    }

    @Override
    public String toString() {
        return "Target{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", alerts=" + alerts +
                '}';
    }
}
