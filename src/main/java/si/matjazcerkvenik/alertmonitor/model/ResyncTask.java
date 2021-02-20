package si.matjazcerkvenik.alertmonitor.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerProcessor;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerResyncMessage;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerResyncMetricObject;
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

public class ResyncTask extends TimerTask {

    private SimpleLogger logger = DAO.getLogger();

    public static void main(String... args) {
        DAO.ALERTMONITOR_RESYNC_ENDPOINT = "http://pgcentos:9090/api/v1/query?query=ALERTS";
        ResyncTask rt = new ResyncTask();
        rt.run();
    }

    @Override
    public void run() {

        logger.info("RESYNC: === starting resynchronization ===");
        DAO.lastResyncTimestamp = System.currentTimeMillis();

        try {

            OkHttpClient httpClient = instantiateHttpClient();

            Request request = new Request.Builder()
                    .url(DAO.ALERTMONITOR_RESYNC_ENDPOINT)
                    .addHeader("User-Agent", "Alertmonitor/v1")
                    .get()
                    .build();

            logger.info("RESYNC: sending " + request.method().toUpperCase() + " " + DAO.ALERTMONITOR_RESYNC_ENDPOINT);

            String responseBody = null;
            Response response = httpClient.newCall(request).execute();
            logger.info("RESYNC: response: errorcode=" + response.code() + ", success=" + response.isSuccessful());
            if (response.isSuccessful()) {
                responseBody = response.body().string();
                logger.info("RESYNC: response: " + responseBody);
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
                AlertmanagerResyncMessage arm = gson.fromJson(responseBody, AlertmanagerResyncMessage.class);

                // set flag toBeDeleted=true for all active alerts before executing resync
                for (DNotification n : DAO.getInstance().getActiveAlerts().values()) {
                    n.setToBeDeleted(true);
                }

                List<DNotification> resyncAlerts = new ArrayList<>();
                int newAlertsCount = 0;

                logger.info("RESYNC: ALERT metrics size: " + arm.getData().getResult().size());
                for (AlertmanagerResyncMetricObject armo : arm.getData().getResult()) {
                    logger.debug(armo.toString());

                    DNotification n = new DNotification();
                    n.setTimestamp(System.currentTimeMillis());
                    n.setAlertname(armo.getMetric().getAlertname());
                    n.setSource("RESYNC");
                    n.setUserAgent("Alertmonitor/v1");
                    if (armo.getMetric().getInstance() != null && armo.getMetric().getInstance().length() > 0) {
                        n.setInstance(armo.getMetric().getInstance());
                    } else {
                        n.setInstance("-");
                    }
                    n.setHostname(AlertmanagerProcessor.stripInstance(n.getInstance()));
                    if (armo.getMetric().getNodename() != null && armo.getMetric().getNodename().length() > 0) {
                        n.setNodename(armo.getMetric().getNodename());
                    } else {
                        n.setNodename(armo.getMetric().getInstance());
                    }
                    n.setInfo(armo.getMetric().getInfo());
                    n.setJob(armo.getMetric().getJob());
                    n.setTags(armo.getMetric().getTags());
                    n.setSeverity(armo.getMetric().getSeverity());
                    if (armo.getMetric().getPriority() != null && armo.getMetric().getPriority().length() > 0) {
                        n.setPriority(armo.getMetric().getPriority());
                    } else {
                        n.setPriority("low");
                    }
                    n.setTeam(armo.getMetric().getTeam());
                    if (armo.getMetric().getEventType() != null && armo.getMetric().getEventType().length() > 0) {
                        n.setEventType(armo.getMetric().getEventType());
                    }
                    if (armo.getMetric().getProbableCause() != null && armo.getMetric().getProbableCause().length() > 0) {
                        n.setProbableCause(armo.getMetric().getProbableCause());
                    }
                    n.setCurrentValue("-");
                    n.setUrl(armo.getMetric().getUrl());
                    n.setDescription(armo.getMetric().getDescription());

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
                    n.setUid(MD5Checksum.getMd5Checksum(n.getTimestamp()
                            + n.hashCode()
                            + new Random().nextInt(Integer.MAX_VALUE)
                            + n.getPriority()
                            + new Random().nextInt(Integer.MAX_VALUE)
                            + n.getAlertname()
                            + new Random().nextInt(Integer.MAX_VALUE)
                            + n.getInfo()
                            + new Random().nextInt(Integer.MAX_VALUE)
                            + n.getInstance()
                            + new Random().nextInt(Integer.MAX_VALUE)
                            + n.getDescription()
                            + new Random().nextInt(Integer.MAX_VALUE)
                            + n.getSource()
                            + new Random().nextInt(Integer.MAX_VALUE)
                            + n.getUserAgent()));

                    // set correlation ID
                    n.setCorrelationId(MD5Checksum.getMd5Checksum(n.getAlertname()
                            + n.getInfo()
                            + n.getInstance()
                            + n.getJob()));

                    if (armo.getMetric().getAlertstate().equalsIgnoreCase("firing")) {

                        logger.debug(n.toString());
                        resyncAlerts.add(n);
                        DAO.getInstance().addToJournal(n);

                        if (DAO.getInstance().getActiveAlerts().containsKey(n.getCorrelationId())) {
                            logger.info("RESYNC: Alert exists: {uid=" + n.getUid() + ", cid=" + n.getCorrelationId() + ", alertname=" + n.getAlertname() + ", instance=" + n.getInstance() + "}");
                            DAO.getInstance().getActiveAlerts().get(n.getCorrelationId()).setToBeDeleted(false);
                        } else {
                            logger.info("RESYNC: New alert: {uid=" + n.getUid() + ", cid=" + n.getCorrelationId() + ", alertname=" + n.getAlertname() + ", instance=" + n.getInstance() + "}");
                            DAO.getInstance().addActiveAlert(n);
                            newAlertsCount++;
                        }

                    }

                } // for each alert

                // clear those in activeAlerts which were not received toBeDeleted=true
                List<String> cidToDelete = new ArrayList<>();
                for (DNotification n : DAO.getInstance().getActiveAlerts().values()) {
                    if (n.isToBeDeleted()) cidToDelete.add(n.getCorrelationId());
                }
                for (String cid : cidToDelete) {
                    logger.info("RESYNC: Removing alert: {cid=" + cid + "}");
                    DAO.getInstance().removeActiveAlert(DAO.getInstance().getActiveAlerts().get(cid));
                }

                logger.info("RESYNC: total resync alerts count: " + resyncAlerts.size());
                logger.info("RESYNC: new alerts count: " + newAlertsCount);
                logger.info("RESYNC: alerts to be deleted: " + cidToDelete.size());

            }

        } catch (UnknownHostException e) {
            logger.error("RESYNC: Failed to resynchronize alarms: " + e.getMessage());
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
            DAO.resyncFailedCount++;
        } catch (SocketTimeoutException e) {
            logger.error("RESYNC: Failed to resynchronize alarms: " + e.getMessage());
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
            DAO.resyncFailedCount++;
        } catch (SocketException e) {
            logger.error("RESYNC: Failed to resynchronize alarms: " + e.getMessage());
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
            DAO.resyncFailedCount++;
        } catch (SSLException e) {
            logger.error("RESYNC: Failed to resynchronize alarms: " + e.getMessage());
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
            DAO.resyncFailedCount++;
        } catch (Exception e) {
            logger.error("RESYNC: Failed to resynchronize alarms: ", e);
            e.printStackTrace();
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
            DAO.resyncFailedCount++;
        }

        logger.info("RESYNC: === resynchronization complete ===");

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
