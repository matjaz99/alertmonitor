package si.matjazcerkvenik.alertmonitor.web;

import si.matjazcerkvenik.alertmonitor.model.prometheus.PQueryMessage;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class UiReportBean {

    private final String QUERY_COUNT_TARGETS_ALL = "count(up)";
    private final String QUERY_COUNT_TARGETS_DOWN = "count(up==0) + count(probe_success==0)";
    private final String QUERY_AVERAGE_TARGETS_AVAILABILITY = "avg(avg_over_time(up[1h])) * 100";
    private final String QUERY_COUNT_JOBS_ALL = "count(count(up) by (job))";

    public String getCountTargetsDown() {
        try {
            PrometheusApi api = new PrometheusApi();
            PQueryMessage queryMessage = api.query(QUERY_COUNT_TARGETS_DOWN);
            return queryMessage.getData().getResult().get(0).getValue()[1].toString();
        } catch (Exception e) {
            LogFactory.getLogger().error("UiReportBean: getCountTargetsDown: exception: ", e);
        }
        return "n/a";
    }

    public String getAverageTargetAvailability() {
        PQueryMessage queryMessage = executeQuery(QUERY_AVERAGE_TARGETS_AVAILABILITY);
        if (queryMessage != null) {
            return queryMessage.getData().getResult().get(0).getValue()[1].toString();
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

    private PQueryMessage executeQuery(String query) {
        try {
            PrometheusApi api = new PrometheusApi();
            return api.query(query);
        } catch (Exception e) {
            LogFactory.getLogger().error("UiReportBean: executeQuery: " + query + ": exception: ", e);
        }
        return null;
    }

}
