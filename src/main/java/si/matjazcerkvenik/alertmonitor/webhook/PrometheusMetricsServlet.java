package si.matjazcerkvenik.alertmonitor.webhook;

import io.prometheus.client.exporter.common.TextFormat;
import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DNotification;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class PrometheusMetricsServlet extends HttpServlet {

    private static final long serialVersionUID = -5776148450627134391L;


    public PrometheusMetricsServlet() {
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        AmMetrics.alertmonitor_build_info.labels("Alertmonitor", "Version 1.2.0", System.getProperty("os.name")).set(DAO.startUpTime);
        AmMetrics.alertmonitor_active_alerts_count.clear();
        for (DNotification n : DAO.getInstance().getActiveAlerts().values()) {
            AmMetrics.alertmonitor_active_alerts_count.labels(n.getAlertname(), n.getSeverity()).inc();
        }
//        AmMetrics.alertmonitor_active_alerts_count.labels("critical").set(DAO.getInstance().getActiveAlarmsList("critical").size());
//        AmMetrics.alertmonitor_active_alerts_count.labels("major").set(DAO.getInstance().getActiveAlarmsList("major").size());
//        AmMetrics.alertmonitor_active_alerts_count.labels("minor").set(DAO.getInstance().getActiveAlarmsList("minor").size());
//        AmMetrics.alertmonitor_active_alerts_count.labels("warning").set(DAO.getInstance().getActiveAlarmsList("warning").size());
        AmMetrics.alertmonitor_alerts_balance_factor.set(DAO.getInstance().calculateAlertsBalanceFactor());
        AmMetrics.alertmonitor_last_event_timestamp.set(DAO.lastEventTimestamp);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(TextFormat.CONTENT_TYPE_004);

        Writer writer = resp.getWriter();
        try {
            TextFormat.write004(writer, AmMetrics.registry.filteredMetricFamilySamples(parse(req)));
            writer.flush();
        } finally {
            writer.close();
        }
    }

    private Set<String> parse(HttpServletRequest req) {
        String[] includedParam = req.getParameterValues("name[]");
        if (includedParam == null) {
            return Collections.emptySet();
        } else {
            return new HashSet<String>(Arrays.asList(includedParam));
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
