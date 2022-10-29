package si.matjazcerkvenik.alertmonitor.model.prometheus;

import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class PrometheusApiClientPool {

    private int count = 0;
    private List<PrometheusApiClient> pool = new ArrayList<>();
    private static PrometheusApiClientPool instance;

    private PrometheusApiClientPool() {
        for (int i = 0; i < AmProps.ALERTMONITOR_PROMETHEUS_CLIENT_POOL_SIZE; i++) {
            PrometheusApiClient c = new PrometheusApiClient();
            pool.add(c);
        }
    }

    public static PrometheusApiClientPool getInstance() {
        if (instance == null) {
            instance = new PrometheusApiClientPool();
        }
        return instance;
    }

    public synchronized PrometheusApiClient getClient() {

        while (pool.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ex) {
                LogFactory.getLogger().error("getClient: Error " + ex.getMessage());
            }
        }
        return pool.remove(0);

    }

    public synchronized void returnClient(PrometheusApiClient client) {
        pool.add(client);
        notifyAll();
    }

    /**
     * Close and remove all clients
     */
    public void destroy() {
        while (!pool.isEmpty()) {
            pool.remove(0);
        }
    }

}
