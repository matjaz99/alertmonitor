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
package si.matjazcerkvenik.alertmonitor.web.uibeans;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DWarning;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusHttpClient;
import si.matjazcerkvenik.alertmonitor.providers.PrometheusDataProvider;
import si.matjazcerkvenik.alertmonitor.util.*;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Named("uiConfigBean")
@RequestScoped
@SuppressWarnings("unused")
public class UiConfigBean implements Serializable {

    private static final long serialVersionUID = 320547795413589L;

    private String selectedDataProvider = AmProps.ALERTMONITOR_DATAPROVIDERS_DEFAULT_PROVIDER_NAME;
    private String selectedLogLevel = "INFO";

    private List<String> allDataProviders;

    public String getSelectedDataProvider() {
        return selectedDataProvider;
    }

    public void setSelectedDataProvider(String selectedDataProvider) {
        this.selectedDataProvider = selectedDataProvider;
        LogFactory.getLogger().info("UiConfigBean: set data provider: " + selectedDataProvider);
    }

    public List<String> getAllDataProviderNames() {
        allDataProviders = new ArrayList<>();
        for (AbstractDataProvider adp : DAO.getInstance().getAllDataProviders()) {
            allDataProviders.add(adp.getProviderConfig().getName());
        }
        return allDataProviders;
    }

    public List<AbstractDataProvider> getAllDataProviders() {
        return DAO.getInstance().getAllDataProviders();
    }

    public String getSelectedLogLevel() {
        return selectedLogLevel;
    }

    public void setSelectedLogLevel(String selectedLogLevel) {
        this.selectedLogLevel = selectedLogLevel;
        if (selectedLogLevel.equalsIgnoreCase("trace")) {
            LogFactory.getLogger().setLogLevel(1);
        } else if (selectedLogLevel.equalsIgnoreCase("debug")) {
            LogFactory.getLogger().setLogLevel(2);
        } else if (selectedLogLevel.equalsIgnoreCase("info")) {
            LogFactory.getLogger().setLogLevel(3);
        } else if (selectedLogLevel.equalsIgnoreCase("warn")) {
            LogFactory.getLogger().setLogLevel(4);
        } else if (selectedLogLevel.equalsIgnoreCase("error")) {
            LogFactory.getLogger().setLogLevel(5);
        } else if (selectedLogLevel.equalsIgnoreCase("fatal")) {
            LogFactory.getLogger().setLogLevel(6);
        } else {
            LogFactory.getLogger().setLogLevel(3);
        }
    }

    /* FOOTER */

    public String getVersion() {
        return AmProps.VERSION;
    }

    public boolean isContainerized() { return AmProps.IS_CONTAINERIZED; }

    public String getLocalIpAddress() {
        return AmProps.LOCAL_IP_ADDRESS;
    }

    public String getRuntimeId() {
        return AmProps.RUNTIME_ID;
    }

    public String getProvidersFilePath() {
        return AmProps.ALERTMONITOR_DATAPROVIDERS_CONFIG_FILE;
    }




    /* CONFIGURATION */

    public List<ConfigParam> getProviderConfigParams(AbstractDataProvider abstractDataProvider) {
        List<ConfigParam> configParamsList = new ArrayList<>();
        for (String k : abstractDataProvider.getProviderConfig().getParams().keySet()) {
            configParamsList.add(new ConfigParam(k, abstractDataProvider.getProviderConfig().getParam(k), abstractDataProvider));
        }
        return configParamsList;
    }

    public void setDataRetention(String time) {
        try {
            AmProps.ALERTMONITOR_DATA_RETENTION_DAYS = Integer.parseInt(time);
            LogFactory.getLogger().info("UiConfigBean: data retention changed: " + AmProps.ALERTMONITOR_DATA_RETENTION_DAYS);
//            Growl.showInfoGrowl("Configuration updated", "");
        } catch (Exception e) {
            LogFactory.getLogger().error("UiConfigBean: Exception: " + e.getMessage());
        }
    }

    public String getDataRetention() {
        return AmProps.ALERTMONITOR_DATA_RETENTION_DAYS + "";
    }

    public void setMongoDbConnectionString(String mongoDbConnectionString) {
        AmProps.ALERTMONITOR_MONGODB_CONNECTION_STRING = mongoDbConnectionString;
        LogFactory.getLogger().info("UiConfigBean: mongoDB connection string changed: " + AmProps.ALERTMONITOR_MONGODB_CONNECTION_STRING);
        DAO.getInstance().resetDataManager();
    }

    public String getMongoDbEnabled() {
        return Boolean.toString(AmProps.ALERTMONITOR_MONGODB_ENABLED);
    }

    public String getMongoDbConnectionString() {
        String s = AmProps.ALERTMONITOR_MONGODB_CONNECTION_STRING;
        // mask password
        String[] a1 = s.split("://");
        if (a1[1].contains("@")) {
            String[] a2 = a1[1].split("@");
            if (a2[0].contains(":")) {
                String[] a3 = a2[0].split(":");
                s = s.replace(a3[0] + ":" + a3[1] + "@",
                        a3[0] + ":" + "•••••••" + "@");
            }
        }
        return s;
    }

    public String getMongoDbConnectTimeout() {
        return Integer.toString(AmProps.ALERTMONITOR_MONGODB_CONNECT_TIMEOUT_SEC);
    }

    public String getMongoDbReadTimeout() {
        return Integer.toString(AmProps.ALERTMONITOR_MONGODB_READ_TIMEOUT_SEC);
    }

    public void setKafkaEnabled(boolean kafkaEnabled) {
        AmProps.ALERTMONITOR_KAFKA_ENABLED = kafkaEnabled;
        LogFactory.getLogger().info("UiConfigBean: kafka enabled changed: " + AmProps.ALERTMONITOR_KAFKA_ENABLED);
    }

    public boolean isKafkaEnabled() {
        return AmProps.ALERTMONITOR_KAFKA_ENABLED;
    }

    public void setKafkaServer(String kafkaServer) {
        AmProps.ALERTMONITOR_KAFKA_SERVER = kafkaServer;
        LogFactory.getLogger().info("UiConfigBean: kafka server changed: " + AmProps.ALERTMONITOR_KAFKA_SERVER);
        KafkaClient.getInstance().resetClient();
    }

    public String getKafkaServer() {
        return AmProps.ALERTMONITOR_KAFKA_SERVER;
    }

    public void setKafkaTopic(String kafkaTopic) {
        AmProps.ALERTMONITOR_KAFKA_TOPIC = kafkaTopic;
        LogFactory.getLogger().info("UiConfigBean: kafka topic changed: " + AmProps.ALERTMONITOR_KAFKA_TOPIC);
    }

    public String getKafkaTopic() {
        return AmProps.ALERTMONITOR_KAFKA_TOPIC;
    }






    /* STATISTICS */



    public int getAllAlarmingInstancesCount() {
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
        return adp.getActiveTargets().size();
    }

    public int getNumberOfAlertsInLastHour() {
        return DAO.getInstance().getDataManager().getNumberOfAlertsInLastHour();
    }

    public String getAlertsPerSecondInLastHour() {
        return DAO.getInstance().getDataManager().getAlertsPerSecondInLastHour();
    }

    public String getStartTime() {
        return Formatter.getFormatedTimestamp(AmProps.START_UP_TIME, AmDateFormat.DATE_TIME);
    }

    public String getUpTime() {
        int secUpTotal = (int) ((System.currentTimeMillis() - AmProps.START_UP_TIME) / 1000);
        return Formatter.convertToDHMSFormat(secUpTotal);
    }

    public String getLastEventTime() {
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
        return Formatter.getFormatedTimestamp(adp.getLastEventTimestamp(), AmDateFormat.TIME);
    }

    public String getTimeSinceLastEvent() {
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
        int secUp = (int) ((System.currentTimeMillis() - adp.getLastEventTimestamp()) / 1000);
        return Formatter.convertToDHMSFormat(secUp);
    }

    public List<DWarning> getWarnings() {
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
        return adp.getWarnings();
    }

    public String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public String getUpdateVersionWarning() {
        if (AmProps.githubVersion.equals(AmProps.VERSION)) return null;
        return AmProps.githubVersion;
    }

}
