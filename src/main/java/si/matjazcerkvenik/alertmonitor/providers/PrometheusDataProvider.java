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
package si.matjazcerkvenik.alertmonitor.providers;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DTarget;
import si.matjazcerkvenik.alertmonitor.model.DWarning;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerProcessor;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AmMessage;
import si.matjazcerkvenik.alertmonitor.model.prometheus.*;
import si.matjazcerkvenik.alertmonitor.util.*;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

import java.util.*;

public class PrometheusDataProvider extends AbstractDataProvider {

    private static final long serialVersionUID = 257181922040L;

	private PrometheusSyncTask prometheusSyncTask = null;

    public static final String DP_PARAM_KEY_SERVER = "server";
    public static final String DP_PARAM_KEY_SYNC_INTERVAL_SEC = "syncInterval";
    public static final String DP_PARAM_KEY_CLIENT_POOL_SIZE = "clientPoolSize";
    public static final String DP_PARAM_KEY_CLIENT_CONNECT_TIMEOUT_SEC = "clientConnectTimeout";
    public static final String DP_PARAM_KEY_CLIENT_READ_TIMEOUT_SEC = "clientReadTimeout";

    public PrometheusDataProvider() {
    }

    @Override
    public void processIncomingEvent(WebhookMessage m) {

        DAO.getInstance().getDataManager().addWebhookMessage(m);
        webhookRequestsReceivedCount++;
        AmMetrics.alertmonitor_webhook_requests_received_total.labels(providerConfig.getName(), m.getRemoteHost(), m.getMethod().toUpperCase()).inc();

        try {
            AmMessage amMessage = AlertmanagerProcessor.processWebhookMessage(m);
            List<DEvent> eventList = AlertmanagerProcessor.convertToDevent(m, amMessage);
            synchronizeAlerts(eventList, false);
            lastEventTimestamp = System.currentTimeMillis();
        } catch (Exception e) {
            LogFactory.getLogger().error("PrometheusDataProvider: processIncomingEvent(): unable to process incoming message: \n" + m.toString());
            LogFactory.getLogger().error("PrometheusDataProvider: processIncomingEvent(): error: " + e.getMessage());
        }

    }

    @Override
    public String reloadPrometheusAction() {

        LogFactory.getLogger().info("PrometheusDataProvider: reloadPrometheusAction called on " + providerConfig.getName());

        PrometheusHttpClient api = prometheusHttpClientPool.getClient();

        try {
            api.reload();
        } catch (Exception e) {
            logger.error("PrometheusDataProvider: reloadPrometheusAction exception: ", e);
        } finally {
            prometheusHttpClientPool.returnClient(api);
        }

        return "";

    }

    @Override
    public DEvent getEvent(String id) {
        DEvent event = super.getEvent(id);

        if (event == null) return null;
        
        // FIXME if dataProvider does not exist anymore then it will throw null pointer exception

        PrometheusHttpClient api = prometheusHttpClientPool.getClient();

        try {
            List<PRule> ruleList;
            ruleList = api.rules();
            for (PRule r : ruleList) {
                if (event.getAlertname().equals(r.getName())) {
                    event.setRuleExpression(r.getQuery());
                    event.setRuleTimeLimit(r.getDuration());
                }
            }
        } catch (PrometheusHttpClientException e) {
            LogFactory.getLogger().error("PrometheusDataProvider: failed to load rules; root cause: " + e.getMessage());
        } finally {
            prometheusHttpClientPool.returnClient(api);
        }

        return event;
    }

    /**
     * Return a list of targets (instances) from Prometheus.
     * @return list
     */
    @Override
    public List<DTarget> getTargets() {
    	
    	if (warnings.containsKey("prom_api")) {
			return null;
		}
    	
        PrometheusHttpClient api = prometheusHttpClientPool.getClient();

        try {
            List<PTarget> pTargets = api.targets();
            Map<String, DTarget> targetsMap = new HashMap<String, DTarget>();
            PQueryMessage query = api.query("probe_success == 0");
            Map<String, String> probe_success_map = new HashMap<String, String>();
            for (PQueryResult r : query.getData().getResult()){
                probe_success_map.put(r.getMetric().get("instance"), "down");
            }

            // convert from PTarget to DTarget
            for (PTarget pTarget : pTargets) {
                String instance = pTarget.getLabels().get("instance");
                DTarget t = targetsMap.getOrDefault(instance, new DTarget());
                t.setSmartTarget(false);
                t.setHealth(pTarget.getHealth()); // original health from querying targets
                if (probe_success_map.containsKey(instance)) t.setHealth(probe_success_map.get(instance)); // overwrite with the metric probe_success
                t.setHostname(instance);
                t.setJob(pTarget.getLabels().get("job"));
                t.setProviderName(providerConfig.getName());
                t.setId(MD5.getChecksum("host" + t.getHostname() + t.getJob()));
                // load active alerts
                for (DEvent n : activeAlerts.values()) {
                    if (n.getInstance().equals(instance)) t.addAlert(n);
                }
                targetsMap.put(t.getId(), t);
            }

            return new ArrayList<>(targetsMap.values());

        } catch (Exception e) {
            LogFactory.getLogger().error("PrometheusDataProvider: failed getting targets; root cause: " + e.getMessage());
        } finally {
            prometheusHttpClientPool.returnClient(api);
        }

        return null;
    }

    // the only difference is stripped hostname
    @Override
    public List<DTarget> getSmartTargets() {
    	
    	if (warnings.containsKey("prom_api")) {
			return null;
		}
    	
        PrometheusHttpClient api = prometheusHttpClientPool.getClient();

        try {
            List<PTarget> pTargets = api.targets();
            Map<String, DTarget> targetsMap = new HashMap<String, DTarget>();
            PQueryMessage query = api.query("probe_success == 0");
            for (PQueryResult r : query.getData().getResult()){
                String instance = r.getMetric().get("instance");
                for (PTarget pT: pTargets) {
                    // override up metric with probe_success; instance must match in up and probe_success metric
                    if (pT.getLabels().get("instance").equals(instance)) pT.setHealth("down");
                }
            }

            // convert from PTarget to DTarget
            for (PTarget pTarget : pTargets) {
                String host = Formatter.stripInstance(pTarget.getLabels().get("instance"));
                DTarget t = targetsMap.getOrDefault(host, new DTarget());
                t.setSmartTarget(true);
                boolean up = false;
                if (pTarget.getHealth().equalsIgnoreCase("up")) up = true;
                t.setUp(up || t.isUp());
                t.setHostname(host);
                t.setProviderName(providerConfig.getName());
                t.setId(MD5.getChecksum("smarthost" + t.getHostname()));
                // load active alerts
                for (DEvent n : activeAlerts.values()) {
                    if (n.getHostname().equals(host)) t.addAlert(n);
                }
                targetsMap.put(host, t);
            }

            return new ArrayList<>(targetsMap.values());

        } catch (PrometheusHttpClientException e) {
            LogFactory.getLogger().error("PrometheusDataProvider: failed getting targets; root cause: " + e.getMessage());
        } finally {
            prometheusHttpClientPool.returnClient(api);
        }

        return null;
    }

    @Override
    public DTarget getSingleTarget(String id) {
        List<DTarget> t1 = getTargets();
        for (DTarget t : t1) {
            if (t.getId().equals(id)) return t;
        }
        List<DTarget> t2 = getSmartTargets();
        for (DTarget t : t2) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    @Override
    public void restartSyncTimer() {

        stopSyncTimer();

        Integer interval = Integer.parseInt(providerConfig.getParam(DP_PARAM_KEY_SYNC_INTERVAL_SEC));
        AmMetrics.alertmonitor_sync_interval_seconds.labels(providerConfig.getName()).set(interval);
        if (interval == 0) {
            LogFactory.getLogger().info("Sync is disabled");
            addWarning("SyncDisabled", "Synchronization is disabled", DWarning.DWARNING_SEVERITY_WARNING);
            return;
        }

        // start resync timer
        if (prometheusSyncTask == null) {
            LogFactory.getLogger().info("Start periodic sync task with period=" + interval);
            syncTimer = new Timer("SyncTimer");
            prometheusSyncTask = new PrometheusSyncTask(this);
            int randomDelay = (int) (Math.random() * 30 + 5);
            syncTimer.schedule(prometheusSyncTask, randomDelay * 1000, interval * 1000);
        }

    }

    @Override
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
