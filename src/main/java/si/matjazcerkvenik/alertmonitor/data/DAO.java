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
package si.matjazcerkvenik.alertmonitor.data;

import si.matjazcerkvenik.alertmonitor.model.config.ProviderConfig;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.providers.EventloggerDataProvider;
import si.matjazcerkvenik.alertmonitor.providers.PrometheusDataProvider;
import si.matjazcerkvenik.alertmonitor.util.*;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.*;

public class DAO {

    private SimpleLogger logger = LogFactory.getLogger();

    /** Singleton instance */
    private static DAO instance;

    /** Access to data */
    private IDataManager dataManager;

    /** A map of dataproviders. Key is name. */
    private Map<String, AbstractDataProvider> dataProviders = new HashMap<>();


    private DAO() {
        if (AmProps.yamlConfig != null) {
            for (ProviderConfig pc : AmProps.yamlConfig.getProviders()) {
                AbstractDataProvider dp = null;
                if (pc.getType().equalsIgnoreCase("prometheus")) {
                    dp = new PrometheusDataProvider();
                } else if (pc.getType().equalsIgnoreCase("eventlogger")) {
                    dp = new EventloggerDataProvider();
                } else {
                    logger.warn("DAO: unknown data provider type: " + pc.getType());
                }
                if (dp != null) {
                    dp.setProviderConfig(pc);
                    dp.init();
                    dataProviders.put(pc.getName(), dp);
                }
            }
        }
        // create default provider if not configured
        if (!dataProviders.containsKey(AmProps.ALERTMONITOR_DATAPROVIDERS_DEFAULT_PROVIDER_NAME)) {
            ProviderConfig defaultPC = AmProps.generateProviderConfigFromEnvs();
            AbstractDataProvider defaultDP = new PrometheusDataProvider();
            defaultDP.setProviderConfig(defaultPC);
            defaultDP.init();
            dataProviders.put(defaultPC.getName(), defaultDP);
        }
        // print data providers
        for (AbstractDataProvider adp : dataProviders.values()) {
            LogFactory.getLogger().info("DAO: Registered ProviderConfig[name=" + adp.getProviderConfig().getName()
                    + ", type=" + adp.getProviderConfig().getType()
                    + ", uri=" + adp.getProviderConfig().getUri() + "]");
            AmMetrics.alertmonitor_providers_info.labels(adp.getProviderConfig().getName(), adp.getProviderConfig().getType(), adp.getProviderConfig().getUri()).set(1);
        }

        if (AmProps.ALERTMONITOR_MONGODB_ENABLED) {
            dataManager = new MongoDbDataManager();
        } else {
            dataManager = new MemoryDataManager();
        }

        TaskManager.getInstance().startDbMaintenanceTask();
    }

    public static DAO getInstance() {
        if (instance == null) {
            instance = new DAO();
        }
        return instance;
    }
    
    public void stopProviders() {
    	for (AbstractDataProvider adp : dataProviders.values()) {
			adp.stopSyncTimer();
		}
	}

    public IDataManager getDataManager() {
        return dataManager;
    }

    /**
     * Stop existing Data manager and re-initialize new one.
     * This is typically done when a property (such as MongoDB connection string) is changed.
     */
    public void resetDataManager() {
        TaskManager.getInstance().stopDbMaintenanceTask();
        dataManager.close();
        if (AmProps.ALERTMONITOR_MONGODB_ENABLED) {
            dataManager = new MongoDbDataManager();
        } else {
            dataManager = new MemoryDataManager();
        }
        TaskManager.getInstance().startDbMaintenanceTask();
    }

    public AbstractDataProvider getDataProvider(String name) {
        return dataProviders.getOrDefault(name, null);
    }

    public AbstractDataProvider getDataProviderById(String id) {
        for (AbstractDataProvider adp : DAO.instance.getAllDataProviders()) {
            if (adp.getProviderConfig().getId().equalsIgnoreCase(id)) return adp;
        }
        return null;
    }

    public AbstractDataProvider getDataProviderByUri(String uri) {
        for (AbstractDataProvider adp : DAO.instance.getAllDataProviders()) {
            if (adp.getProviderConfig().getUri().equalsIgnoreCase(uri)) return adp;
        }
        return null;
    }

    /**
     * Return a list of all Data providers.
     * @return list of data providers
     */
    public List<AbstractDataProvider> getAllDataProviders() {
        return new ArrayList<>(dataProviders.values());
    }


    public void addWarningToAllProviders(String msgKey, String msg, String severity) {
        for (AbstractDataProvider adp : dataProviders.values()) {
            adp.addWarning(msgKey, msg, severity);
        }
    }

    public void removeWarningFromAllProviders(String msgKey) {
        for (AbstractDataProvider adp : dataProviders.values()) {
            adp.removeWarning(msgKey);
        }
    }

}
