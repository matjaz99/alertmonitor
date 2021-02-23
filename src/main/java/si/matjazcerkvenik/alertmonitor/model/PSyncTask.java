package si.matjazcerkvenik.alertmonitor.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.*;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.MD5Checksum;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import javax.net.ssl.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

public class PSyncTask extends TimerTask {

    private SimpleLogger logger = DAO.getLogger();

    public static void main(String... args) {
        DAO.ALERTMONITOR_RESYNC_ENDPOINT = "http://pgcentos:9090/api/v1/alerts";
        PSyncTask rt = new PSyncTask();
        rt.run();
    }

    @Override
    public void run() {

        logger.info("PSYNC: === starting resynchronization ===");
        DAO.lastResyncTimestamp = System.currentTimeMillis();

        try {

            OkHttpClient httpClient = instantiateHttpClient();

            Request request = new Request.Builder()
                    .url(DAO.ALERTMONITOR_RESYNC_ENDPOINT)
                    .addHeader("User-Agent", "Alertmonitor/v1")
                    .get()
                    .build();

            logger.info("PSYNC: sending " + request.method().toUpperCase() + " " + DAO.ALERTMONITOR_RESYNC_ENDPOINT);

            String responseBody = null;
            Response response = httpClient.newCall(request).execute();
            logger.info("PSYNC: response: errorcode=" + response.code() + ", success=" + response.isSuccessful());
            if (response.isSuccessful()) {
                responseBody = response.body().string();
                logger.info("PSYNC: response: " + responseBody);
                AmMetrics.alertmonitor_resync_task_total.labels("Success").inc();
                DAO.resyncSuccessCount++;
            } else {
                AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
                DAO.resyncFailedCount++;
            }

            response.close();

            if (responseBody != null && responseBody.trim().length() > 0) {

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                AmPSyncMessage amMsg = gson.fromJson(responseBody, AmPSyncMessage.class);

                // set flag toBeDeleted=true for all active alerts before executing resync
                for (DNotification n : DAO.getInstance().getActiveAlerts().values()) {
                    n.setToBeDeleted(true);
                }

                List<DNotification> resyncAlerts = new ArrayList<>();
                int newAlertsCount = 0;

                for (AmPSyncAlert alert : amMsg.getData().getAlerts()) {
                    logger.debug(alert.toString());

                    DNotification n = new DNotification();
                    n.setTimestamp(System.currentTimeMillis());
                    n.setAlertname(alert.getLabels().getOrDefault("alertname", "-unknown-"));
                    n.setSource("RESYNC");
                    n.setUserAgent("Alertmonitor/v1");
                    n.setInstance(alert.getLabels().getOrDefault("instance", "-"));
                    n.setHostname(AlertmanagerProcessor.stripInstance(n.getInstance()));
                    n.setNodename(alert.getLabels().getOrDefault("nodename", n.getInstance()));
                    n.setInfo(alert.getLabels().getOrDefault("info", "-"));
                    n.setJob(alert.getLabels().getOrDefault("job", "-"));
                    n.setTags(alert.getLabels().getOrDefault("tags", ""));
                    n.setSeverity(alert.getLabels().getOrDefault("severity", "indeterminate"));
                    n.setPriority(alert.getLabels().getOrDefault("priority", "low"));
                    n.setTeam(alert.getLabels().getOrDefault("team", "unassigned"));
                    n.setEventType(alert.getLabels().getOrDefault("eventType", "5"));
                    n.setProbableCause(alert.getLabels().getOrDefault("probableCause", "1024"));
                    n.setCurrentValue(alert.getAnnotations().getOrDefault("currentValue", "-"));
                    n.setUrl(alert.getLabels().getOrDefault("url", ""));
                    if (alert.getLabels().containsKey("description")) {
                        n.setDescription(alert.getLabels().getOrDefault("description", "-"));
                    } else {
                        n.setDescription(alert.getAnnotations().getOrDefault("description", "-"));
                    }
                    n.setStatus("firing");

                    // add other labels directly into tags
                    // eg: severity (but not clear and info), priority
                    if (!n.getSeverity().equals(Severity.CLEAR)
                            && !n.getSeverity().equals(Severity.INFORMATIONAL)) {
                        n.setTags(n.getTags() + "," + n.getSeverity());
                    }
                    n.setTags(n.getTags() + "," + n.getPriority());

                    // environment variable substitution
                    n.setNodename(AlertmanagerProcessor.substitute(n.getNodename()));
                    n.setInfo(AlertmanagerProcessor.substitute(n.getInfo()));
                    n.setDescription(AlertmanagerProcessor.substitute(n.getDescription()));
                    n.setTags(AlertmanagerProcessor.substitute(n.getTags()));
                    n.setUrl(AlertmanagerProcessor.substitute(n.getUrl()));

                    // set unique ID of event
                    n.generateUID();

                    // set correlation ID
                    n.generateCID();

                    logger.debug(n.toString());
                    resyncAlerts.add(n);
                    DAO.getInstance().addToJournal(n);

                    if (DAO.getInstance().getActiveAlerts().containsKey(n.getCorrelationId())) {
                        logger.info("PSYNC: Alert exists: {uid=" + n.getUid() + ", cid=" + n.getCorrelationId() + ", alertname=" + n.getAlertname() + ", instance=" + n.getInstance() + "}");
                        DAO.getInstance().getActiveAlerts().get(n.getCorrelationId()).setToBeDeleted(false);
                    } else {
                        logger.info("PSYNC: New alert: {uid=" + n.getUid() + ", cid=" + n.getCorrelationId() + ", alertname=" + n.getAlertname() + ", instance=" + n.getInstance() + "}");
                        DAO.getInstance().addActiveAlert(n);
                        newAlertsCount++;
                    }

                } // for each alert

                // clear those in activeAlerts which were not received toBeDeleted=true
                List<String> cidToDelete = new ArrayList<>();
                for (DNotification n : DAO.getInstance().getActiveAlerts().values()) {
                    if (n.isToBeDeleted()) cidToDelete.add(n.getCorrelationId());
                }
                for (String cid : cidToDelete) {
                    logger.info("PSYNC: Removing alert: {cid=" + cid + "}");
                    DAO.getInstance().removeActiveAlert(DAO.getInstance().getActiveAlerts().get(cid));
                }

                logger.info("PSYNC: total psync alerts count: " + resyncAlerts.size());
                logger.info("PSYNC: new alerts count: " + newAlertsCount);
                logger.info("PSYNC: alerts to be deleted: " + cidToDelete.size());

            }

        } catch (UnknownHostException e) {
            logger.error("PSYNC: Failed to resynchronize alarms: " + e.getMessage());
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
            DAO.resyncFailedCount++;
        } catch (SocketTimeoutException e) {
            logger.error("PSYNC: Failed to resynchronize alarms: " + e.getMessage());
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
            DAO.resyncFailedCount++;
        } catch (SocketException e) {
            logger.error("PSYNC: Failed to resynchronize alarms: " + e.getMessage());
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
            DAO.resyncFailedCount++;
        } catch (SSLException e) {
            logger.error("PSYNC: Failed to resynchronize alarms: " + e.getMessage());
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
            DAO.resyncFailedCount++;
        } catch (Exception e) {
            logger.error("PSYNC: Failed to resynchronize alarms: ", e);
            e.printStackTrace();
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
            DAO.resyncFailedCount++;
        }

        logger.info("PSYNC: === resynchronization complete ===");

    }



    public OkHttpClient instantiateHttpClient() {

        if (!DAO.ALERTMONITOR_RESYNC_ENDPOINT.startsWith("https")) {
            return new OkHttpClient();
        }

        // continue if https

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        try {

            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();

        } catch (Exception e) {
            return null;
        }

    }


}
