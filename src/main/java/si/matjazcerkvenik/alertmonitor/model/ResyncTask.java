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

        logger.info("doResync(): starting resynchronization");

//        if (tempListOfResyncAlarms == null) tempListOfResyncAlarms = new ArrayList<Alarm>();
//        tempListOfResyncAlarms.clear();

        try {

            OkHttpClient httpClient = instantiateHttpClient();

            Request request = new Request.Builder()
                    .url(DAO.ALERTMONITOR_RESYNC_ENDPOINT)
                    .addHeader("User-Agent", "Alertmonitor/v1")
                    .get()
                    .build();

            logger.info("doResync(): sending " + request.method().toUpperCase() + " " + DAO.ALERTMONITOR_RESYNC_ENDPOINT);

            String responseBody = null;
            Response response = httpClient.newCall(request).execute();
            logger.info("doResync(): response: errorcode=" + response.code() + ", success=" + response.isSuccessful());
            if (response.isSuccessful()) {
                responseBody = response.body().string();
                logger.info("doResync(): response: " + responseBody);
                AmMetrics.alertmonitor_resync_task_total.labels("Success").inc();
            } else {
                AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
            }

            response.close();

            if (responseBody != null && responseBody.trim().length() > 0) {

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                AlertmanagerResyncMessage arm = gson.fromJson(responseBody, AlertmanagerResyncMessage.class);

                List<DNotification> resyncAlerts = new ArrayList<>();
                for (DNotification n : DAO.getInstance().getActiveAlerts().values()) {
                    n.setToBeDeleted(true);
                }

                logger.info("ALERT metrics: " + arm.getData().getResult().size());
                for (AlertmanagerResyncMetricObject armo : arm.getData().getResult()) {
                    logger.info(armo.toString());

                    DNotification n = new DNotification();
                    n.setAlertname(armo.getMetric().getAlertname());
                    n.setSource("RESYNC");
                    n.setUserAgent("Alertmonitor/v1");
                    n.setInfo(armo.getMetric().getInfo());
                    n.setInstance(armo.getMetric().getInstance());
                    n.setHostname(AlertmanagerProcessor.stripInstance(armo.getMetric().getInstance()));
                    n.setNodename(armo.getMetric().getNodename());
                    n.setJob(armo.getMetric().getJob());
                    n.setTags(armo.getMetric().getTags());
                    n.setSeverity(armo.getMetric().getSeverity());
                    n.setPriority(armo.getMetric().getPriority());
                    n.setTeam(armo.getMetric().getTeam());
                    n.setEventType(armo.getMetric().getEventType());
                    n.setProbableCause(armo.getMetric().getProbableCause());
                    n.setCurrentValue("n/a");
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
                        logger.info(n.toString());
                        if (DAO.getInstance().getActiveAlerts().containsKey(n.getCorrelationId())) {
                            logger.info("==> Alert [" + n.getCorrelationId() + "] already active");
                            DAO.getInstance().getActiveAlerts().get(n.getCorrelationId()).setToBeDeleted(false);
                        } else {
                            logger.info("==> Alert [" + n.getCorrelationId() + "] not active");
                            DAO.getInstance().addActiveAlert(n);
                        }
                    }

                }

                // clear those in activeAlerts which were not received toBeDeleted=true
                // some java 8 trick for removing entries while iterating over map
                // DAO.getInstance().getActiveAlerts().entrySet().removeIf(entry -> entry.getValue().isToBeDeleted());

                List<String> cidToDelete = new ArrayList<>();
                for (DNotification n : DAO.getInstance().getActiveAlerts().values()) {
                    if (n.isToBeDeleted()) cidToDelete.add(n.getCorrelationId());
                }
                for (String cid : cidToDelete) {
                    DAO.getInstance().removeActiveAlert(DAO.getInstance().getActiveAlerts().get(cid));
                }

                logger.info("resync alerts count: " + resyncAlerts.size());



            }

        } catch (UnknownHostException e) {
            logger.error("doResync(): Failed to resynchronize alarms: " + e.getMessage());
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
        } catch (SocketTimeoutException e) {
            logger.error("doResync(): Failed to resynchronize alarms: " + e.getMessage());
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
        } catch (SocketException e) {
            logger.error("doResync(): Failed to resynchronize alarms: " + e.getMessage());
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
        } catch (Exception e) {
            logger.error("doResync(): Failed to resynchronize alarms: ", e);
            e.printStackTrace();
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
        }

        logger.error("doResync(): resynchronization on Prometheus is not fully supported yet");



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
