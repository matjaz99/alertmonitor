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
package si.matjazcerkvenik.alertmonitor.webhook;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.TaskManager;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
import si.matjazcerkvenik.alertmonitor.util.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ManagedBean
@SessionScoped
public class UiConfigBean {

    /* FOOTER */

    public String getVersion() {
        return AmProps.version;
    }

    public boolean isContainerized() { return AmProps.isContainerized; }

    public String getLocalIpAddress() {
        return DAO.getInstance().getLocalIpAddress();
    }




    /* CONFIGURATION */

    public String getPromServer() {
        return AmProps.ALERTMONITOR_PROMETHEUS_SERVER;
    }

    public void setPromServer(String server) {
        if (server.endsWith("/")) server = server.substring(0, server.length()-1);
        AmProps.ALERTMONITOR_PROMETHEUS_SERVER = server;
        LogFactory.getLogger().info("WebhookBean: prometheus server changed: " + server);
//		Growl.showInfoGrowl("Configuration updated", "");
    }

    public void setPsyncInterval(String interval) {

        AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC = Integer.parseInt(interval);
        LogFactory.getLogger().info("WebhookBean: psync interval changed: " + AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC);
//		Growl.showInfoGrowl("Configuration updated", "");
        TaskManager.getInstance().restartPsyncTimer();
    }

    public String getPsyncInterval() { return Integer.toString(AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC); }

    public void setKafkaEnabled(boolean kafkaEnabled) {
        AmProps.ALERTMONITOR_KAFKA_ENABLED = kafkaEnabled;
        LogFactory.getLogger().info("WebhookBean: kafka enabled changed: " + AmProps.ALERTMONITOR_KAFKA_ENABLED);
    }

    public boolean isKafkaEnabled() {
        return AmProps.ALERTMONITOR_KAFKA_ENABLED;
    }

    public void setKafkaServer(String kafkaServer) {
        AmProps.ALERTMONITOR_KAFKA_SERVER = kafkaServer;
        LogFactory.getLogger().info("WebhookBean: kafka server changed: " + AmProps.ALERTMONITOR_KAFKA_SERVER);
        KafkaClient.getInstance().resetClient();
    }

    public String getKafkaServer() {
        return AmProps.ALERTMONITOR_KAFKA_SERVER;
    }

    public void setKafkaTopic(String kafkaTopic) {
        AmProps.ALERTMONITOR_KAFKA_TOPIC = kafkaTopic;
        LogFactory.getLogger().info("WebhookBean: kafka topic changed: " + AmProps.ALERTMONITOR_KAFKA_TOPIC);
    }

    public String getKafkaTopic() {
        return AmProps.ALERTMONITOR_KAFKA_TOPIC;
    }

    public String reloadPrometheusAction() {

        LogFactory.getLogger().debug("reloadPrometheusAction called");

        try {
            PrometheusApi api = new PrometheusApi();
            api.reload();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }






    /* STATISTICS */

    public void setJournalMaxSize(String size) {

        AmProps.ALERTMONITOR_JOURNAL_SIZE = Integer.parseInt(size);
        LogFactory.getLogger().info("WebhookBean: journal max size changed: " + AmProps.ALERTMONITOR_JOURNAL_SIZE);
//		Growl.showInfoGrowl("Configuration updated", "");
    }

    public String getJournalMaxSize() { return Integer.toString(AmProps.ALERTMONITOR_JOURNAL_SIZE); }

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
        return DAO.getInstance().getJournal().size();
    }

    public long getAlarmsCount() {
        return AmMetrics.raisingEventCount;
    }

    public long getClearsCount() {
        return AmMetrics.clearingEventCount;
    }


    public String getLastPsyncTime() { return Formatter.getFormatedTimestamp(AmMetrics.lastPsyncTimestamp); }

    public String getPsyncSuccessCount() { return Integer.toString(AmMetrics.psyncSuccessCount); }

    public String getPsyncFailedCount() { return Integer.toString(AmMetrics.psyncFailedCount); }


    public int getActiveAlarmsCount(String severity) {
        return DAO.getInstance().getActiveAlarmsList(severity).size();
    }

    public int getAllActiveAlarmsCount() {
        return DAO.getInstance().getActiveAlerts().size();
    }

    public int getNumberOfAlertsInLastHour() {
        List<DEvent> list = new ArrayList<DEvent>(DAO.getInstance().getJournal());
        List<DEvent> result = list.stream()
                .filter(notif -> checkIfYoungerThan(notif, 60))
                .collect(Collectors.toList());
        return result.size();
    }

    public String getAlertsPerSecondInLastHour() {
        List<DEvent> list = new ArrayList<DEvent>(DAO.getInstance().getJournal());
        List<DEvent> result = list.stream()
                .filter(notif -> checkIfYoungerThan(notif, 60))
                .collect(Collectors.toList());
        int count = result.size();
        double perSecond = count / 3600.0;
        DecimalFormat df2 = new DecimalFormat("#.###");
        return df2.format(perSecond);
    }

    private boolean checkIfYoungerThan(DEvent notif, int minutes) {
        if (System.currentTimeMillis() - notif.getTimestamp() < minutes * 60 * 1000) return true;
        return false;
    }

    public String getBalanceFactor() {
        DecimalFormat df2 = new DecimalFormat("#.##");
        return df2.format(DAO.getInstance().calculateAlertsBalanceFactor());
    }


    public String getStartTime() {
        return Formatter.getFormatedTimestamp(AmProps.startUpTime);
    }

    public String getUpTime() {
        int secUpTotal = (int) ((System.currentTimeMillis() - AmProps.startUpTime) / 1000);
        return Formatter.convertToDHMSFormat(secUpTotal);
    }

    public String getLastEventTime() {
        return Formatter.getFormatedTimestamp(AmMetrics.lastEventTimestamp);
    }

    public String getTimeSinceLastEvent() {
        int secUp = (int) ((System.currentTimeMillis() - AmMetrics.lastEventTimestamp) / 1000);
        return Formatter.convertToDHMSFormat(secUp);
    }

    public List<String> getWarnings() {
        if (DAO.getInstance().getWarnings().size() == 0) return null;
        return DAO.getInstance().getWarnings();
    }

    public String getRuntimeId() {
        return AmProps.ALERTMONITOR_RUNTIME_ID;
    }

}
