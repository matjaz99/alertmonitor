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
package si.matjazcerkvenik.alertmonitor.webhook;

import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.TaskManager;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PQueryResult;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiException;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.KafkaClient;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ManagedBean
@SessionScoped
public class UiQueryBean {

    private String query = "up";
    private String result;
    private List<PQueryResult> queryResult;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<PQueryResult> getQueryResult() {
        return queryResult;
    }

    public void setQueryResult(List<PQueryResult> queryResult) {
        this.queryResult = queryResult;
    }

    public void executeQuery() {
        result = "";
        PrometheusApi api = new PrometheusApi();
        try {
            queryResult = api.query(query);

            DAO.getLogger().info("size: " + queryResult.size());
            for (PQueryResult r : queryResult) {
                DAO.getLogger().info("result: " + r.toString());
                result += r.toString();
            }

        } catch (PrometheusApiException e) {
            e.printStackTrace();
        }
    }

    public String toNormalDate(Object d) {
        try {
            Double dts = Double.parseDouble(d.toString()) * 1000;
            return DAO.getInstance().getFormatedTimestamp(dts.longValue());
        } catch (Exception e) {
            DAO.getLogger().error(e.getMessage(), e);
        }
        return null;
    }

}
