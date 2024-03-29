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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PQueryResult implements Serializable {

    private static final long serialVersionUID = 164835749125L;

    private Map<String, String> metric;
    private Object[] value; // timestamp-value pair
    private List<Object[]> values; // timestamp-value pair

    public Map<String, String> getMetric() {
        return metric;
    }

    public void setMetric(Map<String, String> metric) {
        this.metric = metric;
    }

    public Object[] getValue() {
        return value;
    }

    public void setValue(Object[] value) {
        this.value = value;
    }

    public List<Object[]> getValues() {
        return values;
    }

    public void setValues(List<Object[]> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "PQueryResult{" +
                "metric=" + metric +
                ", value=" + Arrays.toString(value) +
                ", values=" + values +
                '}';
    }
}
