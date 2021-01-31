package si.matjazcerkvenik.alertmonitor.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerResyncMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import javax.net.ssl.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.TimerTask;

public class ResyncTask extends TimerTask {

    private SimpleLogger logger = DAO.getLogger();

    protected String alarmsEndpoint = "http://172.29.18.22/prometheus/api/v1/query?query=ALERTS";

    public static void main(String... args) {
        ResyncTask rt = new ResyncTask();
        rt.run();
    }

    @Override
    public void run() {

        logger.info("doResynchronization(): starting resynchronization");

//        if (tempListOfResyncAlarms == null) tempListOfResyncAlarms = new ArrayList<Alarm>();
//        tempListOfResyncAlarms.clear();

        try {

            OkHttpClient httpClient = instantiateHttpClient();

            Request request = new Request.Builder()
                    .url(alarmsEndpoint)
                    .addHeader("User-Agent", "Alertmonitor/v1")
                    .get()
                    .build();

            logger.info("doResynchronization(): sending " + request.method().toUpperCase() + " " + alarmsEndpoint);

            String responseBody = null;
            Response response = httpClient.newCall(request).execute();
            logger.info("doResynchronization(): response: errorcode=" + response.code() + ", success=" + response.isSuccessful());
            if (response.isSuccessful()) {
                responseBody = response.body().string();
                logger.info("doResynchronization(): response: " + responseBody);
            }

            response.close();

            if (responseBody != null && responseBody.trim().length() > 0) {
//                ObjectMapper objectMapper = new ObjectMapper();
//                MetricsLibAlarm[] alarms = objectMapper.readValue(responseBody, MetricsLibAlarm[].class);

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                AlertmanagerResyncMessage arm = gson.fromJson(responseBody, AlertmanagerResyncMessage.class);

                System.out.println(arm.toString());
//
//                logger.info("doResynchronization(): received alarms: " + alarms.length);
//                for (int i = 0; i < alarms.length; i++) {
//                    logger.info("doResynchronization(): " + alarms[i].toString());
//                    tempListOfResyncAlarms.add(convertToAlarmObject(alarms[i]));
//                }
//                resynchronizeAlarms();
            }

        } catch (UnknownHostException e) {
            logger.error("doResynchronization(): Failed to resynchronize alarms: " + e.getMessage());
        } catch (SocketTimeoutException e) {
            logger.error("doResynchronization(): Failed to resynchronize alarms: " + e.getMessage());
        } catch (SocketException e) {
            logger.error("doResynchronization(): Failed to resynchronize alarms: " + e.getMessage());
        } catch (Exception e) {
            logger.error("doResynchronization(): Failed to resynchronize alarms: ", e);
        }

        logger.warn("doResynchronization(): resynchronization on Prometheus is not fully supported yet");



    }



    public OkHttpClient instantiateHttpClient() {

        if (!alarmsEndpoint.startsWith("https")) {
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
