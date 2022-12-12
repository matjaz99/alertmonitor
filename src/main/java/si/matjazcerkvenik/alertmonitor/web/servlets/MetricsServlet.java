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
package si.matjazcerkvenik.alertmonitor.web.servlets;

import io.prometheus.client.exporter.common.TextFormat;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@WebServlet(
        name = "MetricsServlet",
        description = "Serving Prometheus metrics",
        urlPatterns = "/metrics"
)
public class MetricsServlet extends HttpServlet {

    private static final long serialVersionUID = -5776148450627134391L;


    public MetricsServlet() {
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {

        // TODO fix metrics for each provider
        for (AbstractDataProvider adp : DAO.getInstance().getAllDataProviders()) {
            AmMetrics.alertmonitor_active_alerts_count.clear();
            for (DEvent n : adp.getActiveAlerts().values()) {
                AmMetrics.alertmonitor_active_alerts_count.labels(adp.getProviderConfig().getName(), n.getAlertname(), n.getSeverity()).inc();
            }
            AmMetrics.alertmonitor_alerts_balance_factor.labels(adp.getProviderConfig().getName()).set(adp.calculateAlertsBalanceFactor());
            AmMetrics.alertmonitor_last_event_timestamp.labels(adp.getProviderConfig().getName()).set(adp.getLastEventTimestamp());
        }



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
            throws IOException {
        doGet(req, resp);
    }
}
