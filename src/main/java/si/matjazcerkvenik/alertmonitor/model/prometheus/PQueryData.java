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

public class PQueryData {

    private String resultType;
    private List<PQueryResult> result;

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public List<PQueryResult> getResult() {
        return result;
    }

    public void setResult(List<PQueryResult> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "PQueryData{" +
                "resultType='" + resultType + '\'' +
                ", result=" + result +
                '}';
    }
}
