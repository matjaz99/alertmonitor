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

public class PQueryMessage {

    private String status;
    private PQueryData data;
    private String errorType;
    private String error;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PQueryData getData() {
        return data;
    }

    public void setData(PQueryData data) {
        this.data = data;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "PQueryMessage{" +
                "status='" + status + '\'' +
                ", data=" + data +
                ", errorType='" + errorType + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
