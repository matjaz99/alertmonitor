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

import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.TaskManager;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.KafkaClient;

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
        return DAO.version;
    }

    public boolean isContainerized() { return DAO.isContainerized; }

    public String getLocalIpAddress() {
        return DAO.getInstance().getLocalIpAddress();
    }




    /* CONFIGURATION */

    public String getPromServer() {
        return DAO.ALERTMONITOR_PROMETHEUS_SERVER;
    }

    public void setPromServer(String server) {
        if (server.endsWith("/")) server = server.substring(0, server.length()-1);
        DAO.ALERTMONITOR_PROMETHEUS_SERVER = server;
        DAO.getLogger().info("WebhookBean: prometheus server changed: " + server);
//		Growl.showInfoGrowl("Configuration updated", "");
    }

    public void setPsyncInterval(String interval) {

        DAO.ALERTMONITOR_PSYNC_INTERVAL_SEC = Integer.parseInt(interval);
        DAO.getLogger().info("WebhookBean: psync interval changed: " + DAO.ALERTMONITOR_PSYNC_INTERVAL_SEC);
//		Growl.showInfoGrowl("Configuration updated", "");
        TaskManager.getInstance().restartPsyncTimer();
    }

    public String getPsyncInterval() { return Integer.toString(DAO.ALERTMONITOR_PSYNC_INTERVAL_SEC); }

    public void setKafkaEnabled(boolean kafkaEnabled) {
        DAO.ALERTMONITOR_KAFKA_ENABLED = kafkaEnabled;
        DAO.getLogger().info("WebhookBean: kafka enabled changed: " + DAO.ALERTMONITOR_KAFKA_ENABLED);
    }

    public boolean isKafkaEnabled() {
        return DAO.ALERTMONITOR_KAFKA_ENABLED;
    }

    public void setKafkaServer(String kafkaServer) {
        DAO.ALERTMONITOR_KAFKA_SERVER = kafkaServer;
        DAO.getLogger().info("WebhookBean: kafka server changed: " + DAO.ALERTMONITOR_KAFKA_SERVER);
        KafkaClient.getInstance().resetClient();
    }

    public String getKafkaServer() {
        return DAO.ALERTMONITOR_KAFKA_SERVER;
    }

    public void setKafkaTopic(String kafkaTopic) {
        DAO.ALERTMONITOR_KAFKA_TOPIC = kafkaTopic;
        DAO.getLogger().info("WebhookBean: kafka topic changed: " + DAO.ALERTMONITOR_KAFKA_TOPIC);
    }

    public String getKafkaTopic() {
        return DAO.ALERTMONITOR_KAFKA_TOPIC;
    }

    public String reloadPrometheusAction() {

        DAO.getLogger().debug("reloadPrometheusAction called");

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

        DAO.JOURNAL_TABLE_SIZE = Integer.parseInt(size);
        DAO.getLogger().info("WebhookBean: journal max size changed: " + DAO.JOURNAL_TABLE_SIZE);
//		Growl.showInfoGrowl("Configuration updated", "");
    }

    public String getJournalMaxSize() { return Integer.toString(DAO.JOURNAL_TABLE_SIZE); }

    public int getWhMsgCount() {
        return DAO.webhookMessagesReceivedCount;
    }

    public int getAmMsgCount() {
        return DAO.amMessagesReceivedCount;
    }

    public int getJournalCount() {
        return DAO.journalReceivedCount;
    }

    public int getJournalSize() {
        return DAO.getInstance().getJournal().size();
    }

    public int getAlarmsCount() {
        return DAO.raisingEventCount;
    }

    public int getClearsCount() {
        return DAO.clearingEventCount;
    }


    public String getLastPsyncTime() { return DAO.getInstance().getFormatedTimestamp(DAO.lastPsyncTimestamp); }

    public String getPsyncSuccessCount() { return Integer.toString(DAO.psyncSuccessCount); }

    public String getPsyncFailedCount() { return Integer.toString(DAO.psyncFailedCount); }


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
        return DAO.getInstance().getFormatedTimestamp(DAO.startUpTime);
    }

    public String getUpTime() {
        int secUpTotal = (int) ((System.currentTimeMillis() - DAO.startUpTime) / 1000);
        return Formatter.convertToDHMSFormat(secUpTotal);
    }

    public String getLastEventTime() {
        return DAO.getInstance().getFormatedTimestamp(DAO.lastEventTimestamp);
    }

    public String getTimeSinceLastEvent() {
        int secUp = (int) ((System.currentTimeMillis() - DAO.lastEventTimestamp) / 1000);
        return Formatter.convertToDHMSFormat(secUp);
    }

}
