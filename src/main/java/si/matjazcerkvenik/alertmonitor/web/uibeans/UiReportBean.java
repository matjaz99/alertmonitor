package si.matjazcerkvenik.alertmonitor.web.uibeans;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PQueryMessage;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiClient;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiClientPool;
import si.matjazcerkvenik.alertmonitor.util.AmDateFormat;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@SessionScoped
@SuppressWarnings("unused")
public class UiReportBean {

    private final String QUERY_PROM_UP_TIME = "round(time()-process_start_time_seconds{job=\"prometheus\"})";
    private final String QUERY_COUNT_TARGETS_ALL = "count(up)";
    private final String QUERY_COUNT_TARGETS_DOWN = "count(up==0) + count(probe_success==0)";
    private final String QUERY_AVERAGE_TARGETS_AVAILABILITY = "(sum(avg_over_time(up[1h])) + sum(avg_over_time(probe_success[1h])) - count(probe_success)) / (count(up) - count(probe_success)) * 100";
    private final String QUERY_TARGETS_LIVENESS = "(count(up == 1) + count(probe_success == 1) - count(probe_success)) / (count(up) - count(probe_success)) * 100";
    private final String QUERY_COUNT_JOBS_ALL = "count(count(up) by (job))";
    private final String QUERY_PROMETHEUS_AVERAGE_REQUEST_DURATION_MILLIS_TEMPLATE = "sum(rate(prometheus_http_request_duration_seconds_sum[__INTERVAL__m])) / sum(rate(prometheus_http_request_duration_seconds_count[__INTERVAL__m])) * 1000";
    private final String QUERY_PROMETHEUS_90_PERCENT_REQUEST_DURATION = "histogram_quantile(0.90, sum(rate(prometheus_http_request_duration_seconds_bucket[__INTERVAL__m])) by (le)) * 1000";
    private final String QUERY_PROMETHEUS_AVERAGE_RESPONSE_TIME = "sum(rate(prometheus_http_request_duration_seconds_sum[1m])) / sum(rate(prometheus_http_request_duration_seconds_count[1m])) * 1000";


    public String getPrometheusUpTime() {
        PQueryMessage queryMessage = executeQuery(QUERY_PROM_UP_TIME);
        if (queryMessage != null) {
            int t = Integer.parseInt(queryMessage.getData().getResult().get(0).getValue()[1].toString());
            return Formatter.convertToDHMSFormat(t);
        }
        return "n/a";
    }

    public String getCountTargetsDown() {
        PQueryMessage queryMessage = executeQuery(QUERY_COUNT_TARGETS_DOWN);
        if (queryMessage != null) {
            return queryMessage.getData().getResult().get(0).getValue()[1].toString();
        }
        return "n/a";
    }

    public String getAverageTargetAvailability() {
        PQueryMessage queryMessage = executeQuery(QUERY_AVERAGE_TARGETS_AVAILABILITY);
        if (queryMessage != null) {
            Double d = Double.parseDouble(queryMessage.getData().getResult().get(0).getValue()[1].toString());
            return new DecimalFormat("0.00").format(d);
        }
        return "n/a";
    }

    public String getTargetsLiveness() {
        PQueryMessage queryMessage = executeQuery(QUERY_TARGETS_LIVENESS);
        if (queryMessage != null) {
            Double d = Double.parseDouble(queryMessage.getData().getResult().get(0).getValue()[1].toString());
            return new DecimalFormat("0.00").format(d);
        }
        return "n/a";
    }

    public String getCountTargetsAll() {
        PQueryMessage queryMessage = executeQuery(QUERY_COUNT_TARGETS_ALL);
        if (queryMessage != null) {
            return queryMessage.getData().getResult().get(0).getValue()[1].toString();
        }
        return "n/a";
    }

    public String getCountJobsAll() {
        PQueryMessage queryMessage = executeQuery(QUERY_COUNT_JOBS_ALL);
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
        PQueryMessage queryMessage = executeQueryRange(QUERY_AVERAGE_TARGETS_AVAILABILITY);
        if (queryMessage != null) {
            availabilityLineModel = createSingleValueLineModel("Availability", availabilityLineModel, queryMessage);
        }
        return availabilityLineModel;
    }


    private LineChartModel livenessLineModel;

    public LineChartModel getLivenessLineModel() {
        PQueryMessage queryMessage = executeQueryRange(QUERY_TARGETS_LIVENESS);
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
        PrometheusApiClient api = PrometheusApiClientPool.getInstance().getClient();
        try {
            return api.query(query);
        } catch (Exception e) {
            LogFactory.getLogger().error("UiReportBean: executeQuery: " + query + ": exception: ", e);
        } finally {
            PrometheusApiClientPool.getInstance().returnClient(api);
        }
        return null;
    }

    private PQueryMessage executeQueryRange(String query) {
        PrometheusApiClient api = PrometheusApiClientPool.getInstance().getClient();
        try {
            long t = System.currentTimeMillis() / 1000;
            return api.queryRange(query, (t - 3600), t, "1m");
        } catch (Exception e) {
            LogFactory.getLogger().error("UiReportBean: executeQueryRange: " + query + ": exception: ", e);
        } finally {
            PrometheusApiClientPool.getInstance().returnClient(api);
        }
        return null;
    }

}
