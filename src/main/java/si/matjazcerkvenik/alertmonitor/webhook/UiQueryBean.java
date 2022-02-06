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
import si.matjazcerkvenik.alertmonitor.model.prometheus.PQueryMessage;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PQueryResult;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiException;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@ManagedBean
@SessionScoped
public class UiQueryBean {

    private String query = "up";
    private String result;
    private List<PQueryResult> queryResult;
    private boolean queryRangeEnabled = false;
    private Date startDate;
    private Date endDate;
    private String step = "1m";

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

    public boolean isQueryRangeEnabled() {
        return queryRangeEnabled;
    }

    public void setQueryRangeEnabled(boolean queryRangeEnabled) {
        this.queryRangeEnabled = queryRangeEnabled;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }


    @PostConstruct
    public void init() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String query = requestParameterMap.getOrDefault("q", null);
        if (query != null && query.length() > 0) {
            this.query = query;
        }
    }


    public void execute() {

        queryResult = null;
        result = null;

        if (queryRangeEnabled) {
            executeQueryRange();
        } else {
            executeQuery();
        }

    }

    public void executeQuery() {

        if (query.startsWith("time_of_max")) {
            doMySpecialFunction();
            return;
        }

        PrometheusApi api = new PrometheusApi();
        try {
            PQueryMessage msg = api.query(query);  // TODO put this in DAO

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
            DAO.getLogger().error("UiQueryBean: failed executing query; root cause: " + e.getMessage());
            result = "failed to get result: " + e.getMessage();
        }
    }

    public void executeQueryRange() {
        PrometheusApi api = new PrometheusApi();
        try {

//            long start = (System.currentTimeMillis() / 1000) - 7200;
//            long end = System.currentTimeMillis() / 1000;
            long start = startDate.getTime() / 1000;
            long end = endDate.getTime() / 1000;
            PQueryMessage msg = api.queryRange(query, start, end, step);

            if (msg.getErrorType() != null) {
                result = msg.getErrorType() + ": " + msg.getError();
                DAO.getLogger().error("executeQueryRange: result: " + result);
                return;
            }

            queryResult = msg.getData().getResult();

            if (queryResult == null) {
                DAO.getLogger().error("executeQueryRange: result is null");
                result = "result is null";
                return;
            }

            DAO.getLogger().info("executeQueryRange: size: " + queryResult.size());
            for (PQueryResult r : queryResult) {
                DAO.getLogger().debug("executeQueryRange: " + r.toString());
            }

        } catch (PrometheusApiException e) {
            DAO.getLogger().error(e.getMessage(), e);
            result = "failed to get result: " + e.getMessage();
        }
    }

    // TODO this is a test method!
    public void doMySpecialFunction() {
        queryResult = null;
        result = null;

        PrometheusApi api = new PrometheusApi();
        try {
            // syntax: time_of_max(alertmonitor_active_alerts_count[24h])
            //eg. time_of_max(alertmonitor_active_alerts_count[3h])

            String tempQuery = query.replace("time_of_max(", "");
            tempQuery = tempQuery.substring(0, tempQuery.length() - 1);
            DAO.getLogger().info("QUERY: " + tempQuery);

            PQueryMessage msg = api.query(tempQuery);

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
                // find max value
                double maxVal = 0;
                for (Object[] oArray : r.getValues()) {
                    double d = Double.parseDouble(oArray[1].toString());
                    if (d > maxVal) maxVal = d;
                }
                // delete those which are less than max
                for (Iterator<Object[]> it = r.getValues().iterator(); it.hasNext(); ) {
                    double d = Double.parseDouble(it.next()[1].toString());
                    if (d < maxVal) it.remove();
                }
                // fix the metric name
                r.getMetric().put("__name__", "time_of_max(" + r.getMetric().get("__name__") + ")");
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
