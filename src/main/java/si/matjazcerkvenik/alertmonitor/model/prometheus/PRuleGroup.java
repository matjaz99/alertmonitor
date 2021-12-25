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
package si.matjazcerkvenik.alertmonitor.model.prometheus;

import java.util.List;

public class PRuleGroup {

    private String name;
    private String file;
    private List<PRule> rules;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public List<PRule> getRules() {
        return rules;
    }

    public void setRules(List<PRule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "PRuleGroups{" +
                "name='" + name + '\'' +
                ", file='" + file + '\'' +
                ", rules=" + rules +
                '}';
    }

}
