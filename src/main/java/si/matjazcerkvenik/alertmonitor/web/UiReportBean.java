package si.matjazcerkvenik.alertmonitor.web;

import si.matjazcerkvenik.alertmonitor.model.prometheus.PQueryMessage;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class UiReportBean {

    private final String QUERY_COUNT_TARGET_DOWN = "count(up == 0)";

    public String getCountTargetDown() {

        try {
            PrometheusApi api = new PrometheusApi();
            PQueryMessage queryMessage = api.query(QUERY_COUNT_TARGET_DOWN);
            return queryMessage.getData().getResult().get(0).toString();
        } catch (Exception e) {
            LogFactory.getLogger().error("UiConfigBean: reloadPrometheusAction exception: ", e);
        }

        return "n/a";

    }

}
