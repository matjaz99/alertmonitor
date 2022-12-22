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
package si.matjazcerkvenik.alertmonitor.model.prometheus;

import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.providers.PrometheusDataProvider;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class PrometheusHttpClientPool {

    private int count = 0;
    private List<PrometheusHttpClient> pool = new ArrayList<>();

    public PrometheusHttpClientPool(AbstractDataProvider dataProvider) {
        Integer poolSize = Integer.parseInt(dataProvider.getProviderConfig().getParam(PrometheusDataProvider.DP_PARAM_KEY_CLIENT_POOL_SIZE));
        for (int i = 0; i < poolSize; i++) {
            PrometheusHttpClient c = new PrometheusHttpClient(dataProvider);
            pool.add(c);
        }
    }

    public synchronized PrometheusHttpClient getClient() {

        while (pool.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ex) {
                LogFactory.getLogger().error("getClient: Error " + ex.getMessage());
            }
        }
        return pool.remove(0);

    }

    public synchronized void returnClient(PrometheusHttpClient client) {
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
