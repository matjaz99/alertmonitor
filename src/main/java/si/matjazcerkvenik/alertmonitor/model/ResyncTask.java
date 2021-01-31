package si.matjazcerkvenik.alertmonitor.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerResyncMessage;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import javax.net.ssl.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.TimerTask;

public class ResyncTask extends TimerTask {

    private SimpleLogger logger = DAO.getLogger();

    public static void main(String... args) {
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

                System.out.println(arm.toString());
//
//                logger.info("doResync(): received alarms: " + alarms.length);
//                for (int i = 0; i < alarms.length; i++) {
//                    logger.info("doResync(): " + alarms[i].toString());
//                    tempListOfResyncAlarms.add(convertToAlarmObject(alarms[i]));
//                }
//                resynchronizeAlarms();
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
            AmMetrics.alertmonitor_resync_task_total.labels("Failed").inc();
        }

        logger.warn("doResync(): resynchronization on Prometheus is not fully supported yet");



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
