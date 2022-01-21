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
import si.matjazcerkvenik.alertmonitor.model.prometheus.PQueryMessage;
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

        if (query.startsWith("time_of_max")) {
            doMySpecialFunction();
            return;
        }

        queryResult = null;
        result = null;

        PrometheusApi api = new PrometheusApi();
        try {
            PQueryMessage msg = api.query(query);

            if (msg.getErrorType() != null) {
                result = msg.getErrorType() + ": " + msg.getError();
                DAO.getLogger().error("executeQuery: result: " + result);
                return;
            }

            queryResult = msg.getData().getResult();

            if (queryResult == null) {
                DAO.getLogger().error("executeQuery: result is null");
                result = "result is null";
                return;
            }

            DAO.getLogger().info("executeQuery: size: " + queryResult.size());
            for (PQueryResult r : queryResult) {
                DAO.getLogger().debug("executeQuery: " + r.toString());
            }

        } catch (PrometheusApiException e) {
            DAO.getLogger().error(e.getMessage(), e);
            result = "failed to get result: " + e.getMessage();
        }
    }

    public void doMySpecialFunction() {
        queryResult = null;
        result = null;

        PrometheusApi api = new PrometheusApi();
        try {
            // syntax: time_of_max(alertmonitor_active_alerts_count[24h])
            query = query.replace("time_of_max(", "");
            query = query.substring(0, query.length() - 1);
            DAO.getLogger().info("QUERY: " + query);

            PQueryMessage msg = api.query(query);

            if (msg.getErrorType() != null) {
                result = msg.getErrorType() + ": " + msg.getError();
                DAO.getLogger().error("executeQuery: result: " + result);
                return;
            }

            queryResult = msg.getData().getResult();

            if (queryResult == null) {
                DAO.getLogger().error("executeQuery: result is null");
                result = "result is null";
                return;
            }

            for (PQueryResult r : queryResult) {
                DAO.getLogger().debug("executeQuery: " + r.toString());
            }

        } catch (PrometheusApiException e) {
            DAO.getLogger().error(e.getMessage(), e);
            result = "failed to get result: " + e.getMessage();
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
