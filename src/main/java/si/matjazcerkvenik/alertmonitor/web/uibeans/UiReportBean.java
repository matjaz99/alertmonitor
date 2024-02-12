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

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PQueryMessage;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PQueryResult;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusHttpClient;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.util.AmDateFormat;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ManagedBean
//@RequestScoped
//@SessionScoped
@ViewScoped
@SuppressWarnings("unused")
public class UiReportBean implements Serializable {

    private static final long serialVersionUID = 484765215984854L;

    private AbstractDataProvider adp;

    private final String QUERY_PROM_UP_TIME = "round(time()-process_start_time_seconds{job=\"prometheus\"})";
    private final String QUERY_SCRAPE_INTERVAL_SECONDS = "round((timestamp(up) - timestamp(up offset 1h)) / count_over_time({__name__=~\"up\"}[1h]))";
    private final String QUERY_TOTAL_TARGETS_COUNT = "count(up)";
    private final String QUERY_TOTAL_TARGETS_DOWN_COUNT = "count(probe_success==0 OR up==0)";
    //    private final String QUERY_TOTAL_TARGETS_DOWN_COUNT = "count(up==0) + count(probe_success==0)";
    private final String QUERY_TOTAL_JOBS_COUNT = "count(count(up) by (job))";
    private final String QUERY_TARGETS_CURRENT_STATUS = "probe_success OR up";
    private final String QUERY_TARGETS_DOWNTIME = "(count_over_time(up[7d]) - (sum_over_time(up[7d]))) * __SCRAPE_INTERVAL__";
    private final String QUERY_TARGETS_AVAILABILITY_PERCENTAGE = "(sum_over_time(probe_success[7d]) / count_over_time(probe_success[7d]) * 100) OR (sum_over_time(up[7d]) / count_over_time(up[7d]) * 100)";
    private final String QUERY_TARGETS_MTBF = "sum_over_time(up[30d]) * __SCRAPE_INTERVAL__ / 3600 / resets(up[30d])";
    private final String QUERY_TARGETS_AVERAGE_AVAILABILITY_PERCENTAGE = "(sum(avg_over_time(up[1h])) + sum(avg_over_time(probe_success[1h])) - count(probe_success)) / (count(up) - count(probe_success)) * 100";
    private final String QUERY_TARGETS_ALIVENESS_PERCENTAGE = "count(probe_success==1 OR up==1) / count(up) * 100";
//    private final String QUERY_TARGETS_LIVENESS = "(count(up == 1) + count(probe_success == 1) - count(probe_success)) / (count(up) - count(probe_success)) * 100";
    private final String QUERY_PROMETHEUS_AVERAGE_REQUEST_DURATION_MILLIS_TEMPLATE = "sum(rate(prometheus_http_request_duration_seconds_sum[__INTERVAL__m])) / sum(rate(prometheus_http_request_duration_seconds_count[__INTERVAL__m])) * 1000";
    private final String QUERY_PROMETHEUS_90_PERCENT_REQUEST_DURATION = "histogram_quantile(0.90, sum(rate(prometheus_http_request_duration_seconds_bucket[__INTERVAL__m])) by (le)) * 1000";
    private final String QUERY_PROMETHEUS_AVERAGE_RESPONSE_TIME = "sum(rate(prometheus_http_request_duration_seconds_sum[1m])) / sum(rate(prometheus_http_request_duration_seconds_count[1m])) * 1000";

    @PostConstruct
    public void init() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String id = requestParameterMap.getOrDefault("provider", "null");
        adp = DAO.getInstance().getDataProviderById(id);
        LogFactory.getLogger().info("UiReportBean: init: found provider: " + adp.getProviderConfig().getName() + "@" + adp.getProviderConfig().getUri());

        fillInstanceStatusesTable();
    }

    public AbstractDataProvider getAdp() {
        return adp;
    }

    public String getPrometheusUpTime() {
        PQueryMessage queryMessage = executeQuery(QUERY_PROM_UP_TIME);
        if (queryMessage != null) {
            int t = Integer.parseInt(queryMessage.getData().getResult().get(0).getValue()[1].toString());
            return Formatter.convertToDHMSFormat(t);
        }
        return "n/a";
    }

    public String getCountTargetsDown() {
        PQueryMessage queryMessage = executeQuery(QUERY_TOTAL_TARGETS_DOWN_COUNT);
        if (queryMessage != null) {
            return queryMessage.getData().getResult().get(0).getValue()[1].toString();
        }
        return "n/a";
    }

    private List<InstanceStatuses> instanceStatusesList;

    public List<InstanceStatuses> getInstanceStatusesTable() {
        return instanceStatusesList;
    }

    public void fillInstanceStatusesTable() {

        System.out.println("fillInstanceStatusesTable");

        Map<String, InstanceStatuses> map = new HashMap<>();

        PQueryMessage queryMessage1 = executeQuery(QUERY_TARGETS_CURRENT_STATUS);
        if (queryMessage1 != null) {
            for (PQueryResult res : queryMessage1.getData().getResult()) {
                String instance = res.getMetric().get("instance");
                String job = res.getMetric().get("job");
                String value = (String) res.getValue()[1];
                InstanceStatuses is = map.getOrDefault(instance + job, new InstanceStatuses());
                is.setInstance(instance);
                is.setJob(job);
                is.setStatus(value);
                map.put(instance + job, is);
            }
        }

        PQueryMessage queryMessage2 = executeQuery(QUERY_TARGETS_DOWNTIME.replace("__SCRAPE_INTERVAL__", QUERY_SCRAPE_INTERVAL_SECONDS));
        if (queryMessage2 != null) {
            for (PQueryResult res : queryMessage2.getData().getResult()) {
                String instance = res.getMetric().get("instance");
                String job = res.getMetric().get("job");
                Integer value = Integer.parseInt(res.getValue()[1].toString());
                InstanceStatuses is = map.getOrDefault(instance + job, new InstanceStatuses());
                is.setInstance(instance);
                is.setJob(job);
                is.setDownTime(Formatter.convertToDHMSFormat(value));
                map.put(instance + job, is);
            }
        }

        PQueryMessage queryMessage3 = executeQuery(QUERY_TARGETS_AVAILABILITY_PERCENTAGE);
        if (queryMessage3 != null) {
            for (PQueryResult res : queryMessage3.getData().getResult()) {
                String instance = res.getMetric().get("instance");
                String job = res.getMetric().get("job");
                Double value = Double.parseDouble(res.getValue()[1].toString());
                InstanceStatuses is = map.getOrDefault(instance + job, new InstanceStatuses());
                is.setInstance(instance);
                is.setJob(job);
                is.setAvailability(Formatter.roundDouble(value, 3) + "%");
                map.put(instance + job, is);
            }
        }

        PQueryMessage queryMessage4 = executeQuery(QUERY_TARGETS_MTBF.replace("__SCRAPE_INTERVAL__", QUERY_SCRAPE_INTERVAL_SECONDS));
        if (queryMessage4 != null) {
            for (PQueryResult res : queryMessage4.getData().getResult()) {
                String instance = res.getMetric().get("instance");
                String job = res.getMetric().get("job");
                String value = res.getValue()[1].toString();
                InstanceStatuses is = map.getOrDefault(instance + job, new InstanceStatuses());
                is.setInstance(instance);
                is.setJob(job);
                if (value.equalsIgnoreCase("+Inf")
                        || value.equalsIgnoreCase("-Inf")
                        || value.equalsIgnoreCase("NaN")) {
                    is.setMTBF(value);
                } else {
                    Double d = Double.parseDouble(value);
                    is.setMTBF(Formatter.roundDouble(d, 2) + "h");
                }
                map.put(instance + job, is);
            }
        }

        PQueryMessage queryMessage5 = executeQuery(QUERY_SCRAPE_INTERVAL_SECONDS);
        if (queryMessage5 != null) {
            for (PQueryResult res : queryMessage5.getData().getResult()) {
                String instance = res.getMetric().get("instance");
                String job = res.getMetric().get("job");
                Integer value = Integer.parseInt(res.getValue()[1].toString());
                InstanceStatuses is = map.getOrDefault(instance + job, new InstanceStatuses());
                is.setInstance(instance);
                is.setJob(job);
                is.setScrapeInterval(value + "s");
                map.put(instance + job, is);
            }
        }

        instanceStatusesList = new ArrayList<>();
        for (InstanceStatuses i : map.values()) {
            // ignore instances without status
            // as some queries return data for last 7 or 30 days, there are instances which don't exist
            // anymore and therefore have null current status. Typically, containers change their IP addresses
            // after redeployment, making them completely new instance each time.
            if (i.getStatus() != null) instanceStatusesList.add(i);
        }

    }

    public String getAverageTargetAvailability() {
        PQueryMessage queryMessage = executeQuery(QUERY_TARGETS_AVERAGE_AVAILABILITY_PERCENTAGE);
        if (queryMessage != null) {
            Double d = Double.parseDouble(queryMessage.getData().getResult().get(0).getValue()[1].toString());
            return new DecimalFormat("0.00").format(d);
        }
        return "n/a";
    }

    public String getTargetsLiveness() {
        PQueryMessage queryMessage = executeQuery(QUERY_TARGETS_ALIVENESS_PERCENTAGE);
        if (queryMessage != null) {
            Double d = Double.parseDouble(queryMessage.getData().getResult().get(0).getValue()[1].toString());
            return new DecimalFormat("0.00").format(d);
        }
        return "n/a";
    }

    public String getCountTargetsAll() {
        PQueryMessage queryMessage = executeQuery(QUERY_TOTAL_TARGETS_COUNT);
        if (queryMessage != null) {
            return queryMessage.getData().getResult().get(0).getValue()[1].toString();
        }
        return "n/a";
    }

    public String getCountJobsAll() {
        PQueryMessage queryMessage = executeQuery(QUERY_TOTAL_JOBS_COUNT);
        if (queryMessage != null) {
            return queryMessage.getData().getResult().get(0).getValue()[1].toString();
        }
        return "n/a";
    }

    public String getPrometheusAverageRequestDuration(int historyMinutes) {
        PQueryMessage queryMessage = executeQuery(QUERY_PROMETHEUS_AVERAGE_REQUEST_DURATION_MILLIS_TEMPLATE.replace("__INTERVAL__", ""+historyMinutes));
        if (queryMessage != null) {
            Double d = Double.parseDouble(queryMessage.getData().getResult().get(0).getValue()[1].toString());
            return new DecimalFormat("0.00").format(d);
        }
        return "n/a";
    }

    public String getPrometheus90PercentRequestDuration(int historyMinutes) {
        PQueryMessage queryMessage = executeQuery(QUERY_PROMETHEUS_90_PERCENT_REQUEST_DURATION.replace("__INTERVAL__", ""+historyMinutes));
        if (queryMessage != null) {
            Double d = Double.parseDouble(queryMessage.getData().getResult().get(0).getValue()[1].toString());
            return new DecimalFormat("0.00").format(d);
        }
        return "n/a";
    }


    private LineChartModel availabilityLineModel;

    private LineChartModel createSingleValueLineModel(String chartTitle, LineChartModel model, PQueryMessage queryMessage) {
        model = new LineChartModel();
        ChartData data = new ChartData();

        List<Object[]> result = queryMessage.getData().getResult().get(0).getValues();

        LineChartDataSet dataSet = new LineChartDataSet();
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            values.add(Double.parseDouble(result.get(i)[1].toString()));
        }
        dataSet.setData(values);
        dataSet.setFill(false);
        dataSet.setLabel("Dataset");
        dataSet.setBorderColor("rgb(75, 192, 192)");
        dataSet.setTension(0.01);
        data.addChartDataSet(dataSet);

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            Long dt = Long.parseLong(Formatter.convertScientificNotationToString(result.get(i)[0].toString()));
            labels.add(Formatter.getFormatedTimestamp(dt * 1000, AmDateFormat.TIME));
        }
        data.setLabels(labels);

        //Options
        LineChartOptions options = new LineChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText(chartTitle);
        options.setTitle(title);

        model.setOptions(options);
        model.setData(data);

        return model;
    }

    public LineChartModel getAvailabilityLineModel() {
        PQueryMessage queryMessage = executeQueryRange(QUERY_TARGETS_AVERAGE_AVAILABILITY_PERCENTAGE);
        if (queryMessage != null) {
            availabilityLineModel = createSingleValueLineModel("Availability", availabilityLineModel, queryMessage);
        }
        return availabilityLineModel;
    }


    private LineChartModel livenessLineModel;

    public LineChartModel getLivenessLineModel() {
        PQueryMessage queryMessage = executeQueryRange(QUERY_TARGETS_ALIVENESS_PERCENTAGE);
        if (queryMessage != null) {
            livenessLineModel = createSingleValueLineModel("Liveness", livenessLineModel, queryMessage);
        }
        return livenessLineModel;
    }


    private LineChartModel averageResponseTimeLineModel;

    public LineChartModel getAverageResponseTimeLineModel() {
        PQueryMessage queryMessage = executeQueryRange(QUERY_PROMETHEUS_AVERAGE_RESPONSE_TIME);
        if (queryMessage != null) {
            averageResponseTimeLineModel = createSingleValueLineModel("Average response time (ms)", averageResponseTimeLineModel, queryMessage);
        }
        return averageResponseTimeLineModel;
    }



    private PQueryMessage executeQuery(String query) {
        PrometheusHttpClient api = adp.getHttpClientPool().getClient();
        try {
            return api.query(query);
        } catch (Exception e) {
            LogFactory.getLogger().error("UiReportBean: executeQuery: " + query + ": exception: ", e);
        } finally {
            adp.getHttpClientPool().returnClient(api);
        }
        return null;
    }

    private PQueryMessage executeQueryRange(String query) {
        PrometheusHttpClient api = adp.getHttpClientPool().getClient();
        try {
            long t = System.currentTimeMillis() / 1000;
            return api.queryRange(query, (t - 3600), t, "1m");
        } catch (Exception e) {
            LogFactory.getLogger().error("UiReportBean: executeQueryRange: " + query + ": exception: ", e);
        } finally {
            adp.getHttpClientPool().returnClient(api);
        }
        return null;
    }

}
