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

import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class PrometheusApiClientPool {

    private int count = 0;
    private List<PrometheusApiClient> pool = new ArrayList<>();

    public PrometheusApiClientPool(String name, int poolSize, boolean secure, int connectTimeout, int readTimeout, String server) {
        for (int i = 0; i < poolSize; i++) {
            PrometheusApiClient c = new PrometheusApiClient(secure, connectTimeout, readTimeout, server);
            c.setName(name);
            pool.add(c);
        }
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
