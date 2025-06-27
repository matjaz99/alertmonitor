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
package si.matjazcerkvenik.alertmonitor.web.uibeans;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.prometheus.*;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.util.AmDateFormat;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.*;

@Named("uiQueryBean")
@ViewScoped
@SuppressWarnings("unused")
public class UiQueryBean extends CommonBean implements Serializable {

    private static final long serialVersionUID = 34412597842163L;

    private String query = "up";
    private String result;
    private List<PQueryResult> queryResult;
    private boolean queryRangeEnabled = false;
    private Date startDate;
    private Date endDate;
    private String step = "1m";
    
    @PostConstruct
	public void init() {
        String query = urlParams.getOrDefault("q", null);
        if (query != null && query.length() > 0) {
            this.query = query;
        }
		LogFactory.getLogger().info("UiQueryBean.init(): " + providerId);
	}
	

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

        PrometheusHttpClient api = dataProvider.getHttpClientPool().getClient();
        try {
            PQueryMessage msg = api.query(query);

            if (msg.getErrorType() != null) {
                result = msg.getErrorType() + ": " + msg.getError();
                LogFactory.getLogger().error("UiQueryBean: executeQuery: result: " + result);
                return;
            }

            queryResult = msg.getData().getResult();

            if (queryResult == null) {
                LogFactory.getLogger().error("UiQueryBean: executeQuery: result is null");
                result = "result is null";
                return;
            }

            LogFactory.getLogger().info("UiQueryBean: executeQuery: size: " + queryResult.size());
            for (PQueryResult r : queryResult) {
                LogFactory.getLogger().debug("UiQueryBean: executeQuery: " + r.toString());
            }

        } catch (PrometheusHttpClientException e) {
            LogFactory.getLogger().error("UiQueryBean: failed executing query; root cause: " + e.getMessage());
            result = "failed to get result: " + e.getMessage();
        } finally {
            dataProvider.getHttpClientPool().returnClient(api);
        }
    }

    public void executeQueryRange() {

        if (startDate == null) {
            result = "start date is missing";
            return;
        }
        if (endDate == null) {
            result = "end date is missing";
            return;
        }
        if (step == null || step.length() == 0) {
            result = "step is missing";
            return;
        }

        PrometheusHttpClient api = dataProvider.getHttpClientPool().getClient();
        try {
            long start = startDate.getTime() / 1000;
            long end = endDate.getTime() / 1000;
            PQueryMessage msg = api.queryRange(query, start, end, step);

            if (msg.getErrorType() != null) {
                result = msg.getErrorType() + ": " + msg.getError();
                LogFactory.getLogger().error("UiQueryBean: executeQueryRange: result: " + result);
                return;
            }

            queryResult = msg.getData().getResult();

            if (queryResult == null) {
                LogFactory.getLogger().error("UiQueryBean: executeQueryRange: result is null");
                result = "result is null";
                return;
            }

            LogFactory.getLogger().info("UiQueryBean: executeQueryRange: size: " + queryResult.size());
            for (PQueryResult r : queryResult) {
                LogFactory.getLogger().debug("UiQueryBean: executeQueryRange: " + r.toString());
            }

        } catch (PrometheusHttpClientException e) {
            LogFactory.getLogger().error(e.getMessage() + e.getMessage());
            result = "failed to get result: " + e.getMessage();
        } finally {
            dataProvider.getHttpClientPool().returnClient(api);
        }
    }

    public String confTimeRange(String s) {
        if (startDate == null) startDate = new Date();
        if (endDate == null) endDate = new Date();
        if (s.equals("1h")) {
            startDate.setTime(System.currentTimeMillis() - 3600 * 1000);
            endDate.setTime(System.currentTimeMillis());
            step = "1m";
        } else if (s.equals("4h")) {
            startDate.setTime(System.currentTimeMillis() - 4 * 3600 * 1000);
            endDate.setTime(System.currentTimeMillis());
            step = "15m";
        } else if (s.equals("24h")) {
            startDate.setTime(System.currentTimeMillis() - 24 * 3600 * 1000);
            endDate.setTime(System.currentTimeMillis());
            step = "1h";
        } else if (s.equals("7d")) {
            startDate.setTime(System.currentTimeMillis() - 7 * 24 * 3600 * 1000);
            endDate.setTime(System.currentTimeMillis());
            step = "24h";
        } else if (s.equals("today")) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            startDate.setTime(c.getTimeInMillis());
            endDate.setTime(System.currentTimeMillis());
            step = "15m";
        } else if (s.equals("yesterday")) {
            Calendar c1 = Calendar.getInstance();
            c1.set(Calendar.DAY_OF_MONTH, c1.get(Calendar.DAY_OF_MONTH) - 1);
            c1.set(Calendar.HOUR_OF_DAY, 0);
            c1.set(Calendar.MINUTE, 0);
            c1.set(Calendar.SECOND, 0);
            startDate.setTime(c1.getTimeInMillis());
            Calendar c2 = Calendar.getInstance();
            c2.set(Calendar.HOUR_OF_DAY, 0);
            c2.set(Calendar.MINUTE, 0);
            c2.set(Calendar.SECOND, 0);
            endDate.setTime(c2.getTimeInMillis());
            step = "1h";
        } else if (s.equals("month")) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            startDate.setTime(c.getTimeInMillis());
            endDate.setTime(System.currentTimeMillis());
            step = "24h";
        } else if (s.equals("30d")) {
            Calendar c1 = Calendar.getInstance();
            c1.set(Calendar.DAY_OF_YEAR, c1.get(Calendar.DAY_OF_YEAR) - 30);
            c1.set(Calendar.HOUR_OF_DAY, 0);
            c1.set(Calendar.MINUTE, 0);
            c1.set(Calendar.SECOND, 0);
            startDate.setTime(c1.getTimeInMillis());
            endDate.setTime(System.currentTimeMillis());
            step = "1d";
        } else if (s.equals("90d")) {
            Calendar c1 = Calendar.getInstance();
            c1.set(Calendar.DAY_OF_YEAR, c1.get(Calendar.DAY_OF_YEAR) - 90);
            c1.set(Calendar.DAY_OF_MONTH, 1);
            c1.set(Calendar.HOUR_OF_DAY, 0);
            c1.set(Calendar.MINUTE, 0);
            c1.set(Calendar.SECOND, 0);
            startDate.setTime(c1.getTimeInMillis());
            endDate.setTime(System.currentTimeMillis());
            step = "1d";
        }
        return "";
    }

    // TODO this is a test method!
    public void doMySpecialFunction() {
        queryResult = null;
        result = null;

        PrometheusHttpClient api = dataProvider.getHttpClientPool().getClient();
        try {
            // syntax: time_of_max(alertmonitor_active_alerts_count[24h])
            //eg. time_of_max(alertmonitor_active_alerts_count[3h])

            String tempQuery = query.replace("time_of_max(", "");
            tempQuery = tempQuery.substring(0, tempQuery.length() - 1);
            LogFactory.getLogger().info("QUERY: " + tempQuery);

            PQueryMessage msg = api.query(tempQuery);

            if (msg.getErrorType() != null) {
                result = msg.getErrorType() + ": " + msg.getError();
                LogFactory.getLogger().error("doMySpecialFunction: result: " + result);
                return;
            }

            queryResult = msg.getData().getResult();

            if (queryResult == null) {
                LogFactory.getLogger().error("doMySpecialFunction: result is null");
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

        } catch (PrometheusHttpClientException e) {
            LogFactory.getLogger().error(e.getMessage(), e);
            result = "failed to get result: " + e.getMessage();
        } finally {
            dataProvider.getHttpClientPool().returnClient(api);
        }
    }

    public String toNormalDate(Object d) {
        try {
            Double dts = Double.parseDouble(d.toString()) * 1000;
            return Formatter.getFormatedTimestamp(dts.longValue(), AmDateFormat.DATE_TIME);
        } catch (Exception e) {
            LogFactory.getLogger().error(e.getMessage(), e);
        }
        return null;
    }

}
