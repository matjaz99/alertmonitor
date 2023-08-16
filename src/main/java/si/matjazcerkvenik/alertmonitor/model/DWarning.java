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

public class DWarning {

    private String severity;
    private String message;

    public static final String DWARNING_SEVERITY_CRITICAL = "danger";
    public static final String DWARNING_SEVERITY_WARNING = "warning";
    public static final String DWARNING_SEVERITY_CLEAR = "success";

    public DWarning(String severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    public String getIcon() {
        if (severity.equals(DWARNING_SEVERITY_CLEAR)) {
            return "pi pi-check";
        } else if (severity.equals(DWARNING_SEVERITY_CRITICAL)) {
            return "pi pi-exclamation-circle";
        } else {
            return "pi pi-exclamation-triangle";
        }
    }

    public String getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }
}
