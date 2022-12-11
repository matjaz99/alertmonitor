package si.matjazcerkvenik.alertmonitor.providers;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.Target;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerProcessor;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AmAlertMessage;
import si.matjazcerkvenik.alertmonitor.model.prometheus.*;
import si.matjazcerkvenik.alertmonitor.util.*;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

import java.util.*;

public class PrometheusDataProvider extends AbstractDataProvider {

    private PrometheusSyncTask prometheusSyncTask = null;

    public PrometheusDataProvider() {
        restartSyncTimer();
    }

    @Override
    public void processIncomingEvent(WebhookMessage m) {

        DAO.getInstance().getDataManager().addWebhookMessage(m);
        // TODO fix metrics - for each provider
        AmMetrics.webhookMessagesReceivedCount++;
        AmMetrics.alertmonitor_webhook_messages_received_total.labels(m.getRemoteHost(), m.getMethod().toUpperCase()).inc();

        try {
            AmAlertMessage amAlertMessage = AlertmanagerProcessor.processWebhookMessage(m);
            List<DEvent> eventList = AlertmanagerProcessor.convertToDevent(m, amAlertMessage);
            synchronizeAlerts(eventList, false);
            AmMetrics.amMessagesReceivedCount++;
            AmMetrics.lastEventTimestamp = System.currentTimeMillis();
        } catch (Exception e) {
            LogFactory.getLogger().error("DataProvider: processIncomingEvent(): unable to process incoming message: \n" + m.toString());
            LogFactory.getLogger().error("DataProvider: processIncomingEvent(): error: " + e.getMessage());
        }

    }

    @Override
    public DEvent getEvent(String id) {
        DEvent event = super.getEvent(id);

        if (event == null) return null;

        PrometheusApiClient api = PrometheusApiClientPool.getInstance().getClient();

        try {
            List<PRule> ruleList;
            ruleList = api.rules();
            for (PRule r : ruleList) {
                if (event.getAlertname().equals(r.getName())) {
                    event.setRuleExpression(r.getQuery());
                    event.setRuleTimeLimit(r.getDuration());
                }
            }
        } catch (PrometheusApiException e) {
            LogFactory.getLogger().error("DAO: failed to load rules; root cause: " + e.getMessage());
        } finally {
            PrometheusApiClientPool.getInstance().returnClient(api);
        }

        return event;
    }

    /**
     * Return a list of targets (instances) from Prometheus.
     * @return list
     */
    @Override
    public List<Target> getTargets() {
        PrometheusApiClient api = PrometheusApiClientPool.getInstance().getClient();

        try {
            List<PTarget> pTargets = api.targets();
            Map<String, Target> targetsMap = new HashMap<String, Target>();
            PQueryMessage query = api.query("probe_success == 0");
            Map<String, String> probe_success_map = new HashMap<String, String>();
            for (PQueryResult r : query.getData().getResult()){
                probe_success_map.put(r.getMetric().get("instance"), "down");
            }

            // convert from PTarget to Target
            for (PTarget pTarget : pTargets) {
                String instance = pTarget.getLabels().get("instance");
                Target t = targetsMap.getOrDefault(instance, new Target());
                t.setSmartTarget(false);
                t.setHealth(pTarget.getHealth()); // original health from querying targets
                if (probe_success_map.containsKey(instance)) t.setHealth(probe_success_map.get(instance)); // overwrite with the metric probe_success
                t.setHostname(instance);
                t.setJob(pTarget.getLabels().get("job"));
                t.setId(MD5.getChecksum("host" + t.getHostname() + t.getJob()));
                // load active alerts
                for (DEvent n : activeAlerts.values()) {
                    if (n.getInstance().equals(instance)) t.addAlert(n);
                }
                targetsMap.put(t.getId(), t);
            }

            return new ArrayList<>(targetsMap.values());

        } catch (Exception e) {
            LogFactory.getLogger().error("DAO: failed getting targets; root cause: " + e.getMessage());
        } finally {
            PrometheusApiClientPool.getInstance().returnClient(api);
        }

        return null;
    }

    // the only difference is stripped hostname
    @Override
    public List<Target> getSmartTargets() {
        PrometheusApiClient api = PrometheusApiClientPool.getInstance().getClient();

        try {
            List<PTarget> pTargets = api.targets();
            Map<String, Target> targetsMap = new HashMap<String, Target>();
            PQueryMessage query = api.query("probe_success == 0");
            Map<String, String> probe_success_map = new HashMap<String, String>();
            for (PQueryResult r : query.getData().getResult()){
                String instance = r.getMetric().get("instance");
                for (PTarget pT: pTargets) {
                    // override up metric with probe_success; instance must match in up and probe_success metric
                    if (pT.getLabels().get("instance").equals(instance)) pT.setHealth("down");
                }
            }

            // convert from PTarget to Target
            for (PTarget pTarget : pTargets) {
                String host = Formatter.stripInstance(pTarget.getLabels().get("instance"));
                Target t = targetsMap.getOrDefault(host, new Target());
                t.setSmartTarget(true);
                boolean up = false;
                if (pTarget.getHealth().equalsIgnoreCase("up")) up = true;
                t.setUp(up || t.isUp());
                t.setHostname(host);
                t.setId(MD5.getChecksum("smarthost" + t.getHostname()));
                // load active alerts
                for (DEvent n : activeAlerts.values()) {
                    if (n.getHostname().equals(host)) t.addAlert(n);
                }
                targetsMap.put(host, t);
            }

            return new ArrayList<>(targetsMap.values());

        } catch (PrometheusApiException e) {
            LogFactory.getLogger().error("DAO: failed getting targets; root cause: " + e.getMessage());
        } finally {
            PrometheusApiClientPool.getInstance().returnClient(api);
        }

        return null;
    }

    @Override
    public Target getSingleTarget(String id) {
        List<Target> t1 = getTargets();
        for (Target t : t1) {
            if (t.getId().equals(id)) return t;
        }
        List<Target> t2 = getSmartTargets();
        for (Target t : t2) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    @Override
    public void restartSyncTimer() {

        stopSyncTimer();

        if (AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC == 0) {
            LogFactory.getLogger().info("Sync is disabled");
        }
        // TODO fix configuration according to provider config

        // start resync timer
        if (prometheusSyncTask == null) {
            LogFactory.getLogger().info("Start periodic sync task with period=" + AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC);
            AmMetrics.alertmonitor_psync_interval_seconds.set(AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC);
            if (AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC > 0) {
                syncTimer = new Timer("SyncTimer");
                prometheusSyncTask = new PrometheusSyncTask(this);
                syncTimer.schedule(prometheusSyncTask, 5 * 1000, AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC * 1000);
            }
        }

    }

    public void stopSyncTimer() {
        if (syncTimer != null) {
            syncTimer.cancel();
            syncTimer = null;
        }
        if (prometheusSyncTask != null) {
            prometheusSyncTask.cancel();
            prometheusSyncTask = null;
        }
    }

}
