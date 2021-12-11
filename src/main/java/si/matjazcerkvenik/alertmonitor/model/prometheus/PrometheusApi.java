package si.matjazcerkvenik.alertmonitor.model.prometheus;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.HttpClientFactory;
import si.matjazcerkvenik.simplelogger.SimpleLogger;


public class PrometheusApi {

    private SimpleLogger logger = DAO.getLogger();

    public String alerts() throws Exception {

        String responseBody = null;

        OkHttpClient httpClient = HttpClientFactory.instantiateHttpClient();

        Request request = new Request.Builder()
                .url(DAO.ALERTMONITOR_PSYNC_ENDPOINT + "/api/v1/alerts")
                .addHeader("User-Agent", "Alertmonitor/v1")
                .get()
                .build();

        logger.info("PrometheusApi: sending " + request.method().toUpperCase() + " " + DAO.ALERTMONITOR_PSYNC_ENDPOINT + "/api/v1/alerts");

        Response response = httpClient.newCall(request).execute();
        logger.info("PrometheusApi: response: code=" + response.code() + ", success=" + response.isSuccessful());
        if (response.isSuccessful()) {
            responseBody = response.body().string();
            logger.debug("PrometheusApi: response: " + responseBody);
            AmMetrics.alertmonitor_psync_task_total.labels("Success").inc();
            DAO.psyncSuccessCount++;
        } else {
            AmMetrics.alertmonitor_psync_task_total.labels("Failed").inc();
            DAO.psyncFailedCount++;
        }

        response.close();

//        try {
//
//
//
//        } catch (UnknownHostException e) {
//            logger.error("PrometheusApi: UnknownHostException: " + e.getMessage());
//            AmMetrics.alertmonitor_psync_task_total.labels("Failed").inc();
//            DAO.psyncFailedCount++;
//        } catch (SocketTimeoutException e) {
//            logger.error("PrometheusApi: SocketTimeoutException: " + e.getMessage());
//            AmMetrics.alertmonitor_psync_task_total.labels("Failed").inc();
//            DAO.psyncFailedCount++;
//        } catch (SocketException e) {
//            logger.error("PrometheusApi: SocketException: " + e.getMessage());
//            AmMetrics.alertmonitor_psync_task_total.labels("Failed").inc();
//            DAO.psyncFailedCount++;
//        } catch (SSLException e) {
//            logger.error("PrometheusApi: SSLException: " + e.getMessage());
//            AmMetrics.alertmonitor_psync_task_total.labels("Failed").inc();
//            DAO.psyncFailedCount++;
//        } catch (Exception e) {
//            logger.error("PrometheusApi: Exception: ", e);
//            e.printStackTrace();
//            AmMetrics.alertmonitor_psync_task_total.labels("Failed").inc();
//            DAO.psyncFailedCount++;
//        }

        return responseBody;

    }

    public String targets() throws Exception {

        String responseBody = null;

        OkHttpClient httpClient = HttpClientFactory.instantiateHttpClient();

        Request request = new Request.Builder()
                .url(DAO.ALERTMONITOR_PSYNC_ENDPOINT + "/api/v1/targets")
                .addHeader("User-Agent", "Alertmonitor/v1")
                .get()
                .build();

        logger.info("PrometheusApi: sending " + request.method().toUpperCase() + " " + DAO.ALERTMONITOR_PSYNC_ENDPOINT + "/api/v1/targets");

        Response response = httpClient.newCall(request).execute();
        logger.info("PrometheusApi: response: code=" + response.code() + ", success=" + response.isSuccessful());
        if (response.isSuccessful()) {
            responseBody = response.body().string();
            logger.debug("PrometheusApi: response: " + responseBody);
        }

        response.close();

        return responseBody;

    }

}
