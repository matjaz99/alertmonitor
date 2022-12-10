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
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiClientPool;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiClient;
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
public class UiConfigBean {

    private String selectedDataProvider;
    private List<String> allDataProviders;

    public String getSelectedDataProvider() {
        if (selectedDataProvider == null) {
            selectedDataProvider = "/alertmonitor/webhook";
        }
        return selectedDataProvider;
    }

    public void setSelectedDataProvider(String selectedDataProvider) {
        this.selectedDataProvider = selectedDataProvider;
        LogFactory.getLogger().info("UiConfigBean: set data provider: " + selectedDataProvider);
    }

    public List<String> getAllDataProviders() {
        allDataProviders = new ArrayList<>();
        for (AbstractDataProvider adp : DAO.getInstance().getAllDataProviders()) {
            allDataProviders.add(adp.getProviderConfig().getUri());
        }
        return allDataProviders;
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
        return AmProps.ALERTMONITOR_PROMETHEUS_SERVER;
    }

    public void setPromServer(String server) {
        if (server.endsWith("/")) server = server.substring(0, server.length()-1);
        AmProps.ALERTMONITOR_PROMETHEUS_SERVER = server;
        LogFactory.getLogger().info("UiConfigBean: prometheus server changed: " + server);
//		Growl.showInfoGrowl("Configuration updated", "");
    }

    public void setPsyncInterval(String interval) {

        AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC = Integer.parseInt(interval);
        LogFactory.getLogger().info("UiConfigBean: sync interval changed: " + AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC);
//		Growl.showInfoGrowl("Configuration updated", "");
        DAO.getInstance().getDataProvider(selectedDataProvider).restartSyncTimer();
    }

    public String getPsyncInterval() { return Integer.toString(AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC); }

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

        AmProps.ALERTMONITOR_HTTP_CLIENT_READ_TIMEOUT_SEC = Integer.parseInt(interval);
        LogFactory.getLogger().info("UiConfigBean: http client read timeout changed: " + AmProps.ALERTMONITOR_HTTP_CLIENT_READ_TIMEOUT_SEC);
    }

    public String getHttpReadTimeout() { return Integer.toString(AmProps.ALERTMONITOR_HTTP_CLIENT_READ_TIMEOUT_SEC); }


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

        PrometheusApiClient api = PrometheusApiClientPool.getInstance().getClient();

        try {
            api.reload();
        } catch (Exception e) {
            LogFactory.getLogger().error("UiConfigBean: reloadPrometheusAction exception: ", e);
        } finally {
            PrometheusApiClientPool.getInstance().returnClient(api);
        }

        return "";

    }






    /* STATISTICS */

    public long getWhMsgCount() {
        return AmMetrics.webhookMessagesReceivedCount;
    }

    public long getAmMsgCount() {
        return AmMetrics.amMessagesReceivedCount;
    }

    public long getJournalCount() {
        return AmMetrics.journalReceivedCount;
    }

    public long getJournalSize() {
        return DAO.getInstance().getJournalSize();
    }

    public long getAlarmsCount() {
        return AmMetrics.raisingEventCount;
    }

    public long getClearsCount() {
        return AmMetrics.clearingEventCount;
    }


    public String getLastPsyncTime() { return Formatter.getFormatedTimestamp(AmMetrics.lastPsyncTimestamp, AmDateFormat.TIME); }

    public String getPsyncSuccessCount() { return Integer.toString(AmMetrics.psyncSuccessCount); }

    public String getPsyncFailedCount() { return Integer.toString(AmMetrics.psyncFailedCount); }

    public int getActiveAlarmsCount(String severity) {
        return DAO.getInstance().getActiveAlarmsList(severity).size();
    }

    public int getAllActiveAlarmsCount() {
        return DAO.getInstance().getActiveAlerts().size();
    }

    public int getAllAlarmingInstancesCount() {
        return DAO.getInstance().getActiveTargets().size();
    }

    public int getNumberOfAlertsInLastHour() {
        return DAO.getInstance().getDataManager().getNumberOfAlertsInLastHour();
    }

    public String getAlertsPerSecondInLastHour() {
        return DAO.getInstance().getDataManager().getAlertsPerSecondInLastHour();
    }

    public String getBalanceFactor() {
        DecimalFormat df2 = new DecimalFormat("#.##");
        return df2.format(DAO.getInstance().calculateAlertsBalanceFactor());
    }

    public String getStartTime() {
        return Formatter.getFormatedTimestamp(AmProps.START_UP_TIME, AmDateFormat.DATE_TIME);
    }

    public String getUpTime() {
        int secUpTotal = (int) ((System.currentTimeMillis() - AmProps.START_UP_TIME) / 1000);
        return Formatter.convertToDHMSFormat(secUpTotal);
    }

    public String getLastEventTime() {
        return Formatter.getFormatedTimestamp(AmMetrics.lastEventTimestamp, AmDateFormat.TIME);
    }

    public String getTimeSinceLastEvent() {
        int secUp = (int) ((System.currentTimeMillis() - AmMetrics.lastEventTimestamp) / 1000);
        return Formatter.convertToDHMSFormat(secUp);
    }

    public List<String> getWarnings() {
        if (DAO.getInstance().getWarnings().size() == 0) return null;
        return DAO.getInstance().getWarnings();
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
