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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@SessionScoped
@SuppressWarnings("unused")
public class UiConfigBean {

    private String selectedDataProvider = ".default";
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




    /* CONFIGURATION */

    public String getPromServer() {
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
        return adp.getProviderConfig().getParam(PrometheusDataProvider.DP_PARAM_KEY_SERVER);
    }

    public void setPromServer(String server) {
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
        if (server.endsWith("/")) server = server.substring(0, server.length()-1);
        adp.getProviderConfig().setParam(PrometheusDataProvider.DP_PARAM_KEY_SERVER, server);
        LogFactory.getLogger().info("UiConfigBean: prometheus server changed: " + server);
//		Growl.showInfoGrowl("Configuration updated", "");
    }

    public void setDataRetention(String time) {
        try {
            AmProps.ALERTMONITOR_DATA_RETENTION_DAYS = Integer.parseInt(time);
            LogFactory.getLogger().info("UiConfigBean: data retention changed: " + AmProps.ALERTMONITOR_DATA_RETENTION_DAYS);
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

    public void setHttpReadTimeout(String interval) {
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
        adp.getProviderConfig().setParam(PrometheusDataProvider.DP_PARAM_KEY_CLIENT_READ_TIMEOUT_SEC, interval);
        LogFactory.getLogger().info("UiConfigBean: http client read timeout changed: " + interval);
    }

    public String getHttpReadTimeout() {
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
        return adp.getProviderConfig().getParam(PrometheusDataProvider.DP_PARAM_KEY_CLIENT_READ_TIMEOUT_SEC);
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

    public String reloadPrometheusAction() {

        LogFactory.getLogger().debug("UiConfigBean: reloadPrometheusAction called");

        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
        PrometheusHttpClient api = adp.getPrometheusApiClientPool().getClient();

        try {
            api.reload();
        } catch (Exception e) {
            LogFactory.getLogger().error("UiConfigBean: reloadPrometheusAction exception: ", e);
        } finally {
            adp.getPrometheusApiClientPool().returnClient(api);
        }

        return "";

    }






    /* STATISTICS */

//    public long getWhMsgCount() {
//        // TODO moved to provider
//        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
//        return adp.getWebhookMessagesReceivedCount();
//    }

//    public long getJournalCount() {
//        // TODO moved to provider
//        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
//        return adp.getJournalCount();
//    }

//    public long getJournalSize() {
//        // TODO moved to provider
//        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
//        return adp.getJournalSize();
//    }
//
//    public long getAlarmsCount() {
//        // TODO moved to provider
//        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
//        return adp.getRaisingEventCount();
//    }

//    public long getClearsCount() {
//        // TODO moved to provider
//        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
//        return adp.getClearingEventCount();
//    }

    public void setSyncInterval(String interval) {
        // TODO implement
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
        Integer i = Integer.parseInt(interval);
        adp.getProviderConfig().setParam(PrometheusDataProvider.DP_PARAM_KEY_SYNC_INTERVAL_SEC, String.valueOf(i));
        LogFactory.getLogger().info("UiConfigBean: sync interval changed: " + i);
//		Growl.showInfoGrowl("Configuration updated", "");
        adp.restartSyncTimer();
    }

    public String getSyncInterval() {
        // TODO moved to provider
        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
        return adp.getProviderConfig().getParam(PrometheusDataProvider.DP_PARAM_KEY_SYNC_INTERVAL_SEC);
    }

//    public String getLastSyncTime() {
//        // TODO moved to provider
//        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
//        return Formatter.getFormatedTimestamp(adp.getLastSyncTimestamp(), AmDateFormat.TIME); }
//
//    public String getSyncSuccessCount() {
//        // TODO moved to provider
//        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
//        return Integer.toString(adp.getSyncSuccessCount()); }

//    public String getSyncFailedCount() {
//        // TODO moved to provider
//        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
//        return Integer.toString(adp.getSyncFailedCount()); }
//
//    public int getActiveAlarmsCount(String severity) {
//        // TODO moved to provider
//        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
//        return adp.getActiveAlarmsList(severity).size();
//    }
//
//    public int getAllActiveAlarmsCount() {
//        // TODO moved to provider
//        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
//        return adp.getActiveAlerts().size();
//    }

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

//    public String getBalanceFactor() {
//        // TODO moved to provider
//        AbstractDataProvider adp = DAO.getInstance().getDataProvider(selectedDataProvider);
//        DecimalFormat df2 = new DecimalFormat("#.##");
//        return df2.format(adp.calculateAlertsBalanceFactor());
//    }

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
