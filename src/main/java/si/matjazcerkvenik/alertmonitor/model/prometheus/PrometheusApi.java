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
package si.matjazcerkvenik.alertmonitor.model.prometheus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.HttpClientFactory;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import javax.net.ssl.SSLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PrometheusApi {

    private SimpleLogger logger = DAO.getLogger();

    private String HTTP_CLIENT_USER_AGENT = "Alertmonitor/v1";

    public List<PAlert> query(String query) throws PrometheusApiException {

        Request request = new Request.Builder()
                .url(DAO.ALERTMONITOR_PROMETHEUS_SERVER + "/api/v1/query")
                .addHeader("User-Agent", HTTP_CLIENT_USER_AGENT)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(RequestBody.create("".getBytes()))
                .build();

        String responseBody = execute(request);
        logger.info(responseBody);

        return null;

    }

    public List<PAlert> alerts() throws PrometheusApiException {

        Request request = new Request.Builder()
                .url(DAO.ALERTMONITOR_PROMETHEUS_SERVER + "/api/v1/alerts")
                .addHeader("User-Agent", HTTP_CLIENT_USER_AGENT)
                .get()
                .build();

        String responseBody = execute(request);

        if (responseBody != null && responseBody.trim().length() > 0) {

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            PAlertsMessage amMsg = gson.fromJson(responseBody, PAlertsMessage.class);

            DAO.psyncSuccessCount++;

            return amMsg.getData().getAlerts();

        } else {
            DAO.psyncFailedCount++;
        }

        return null;

    }

    public List<PTarget> targets() throws PrometheusApiException {

        Request request = new Request.Builder()
                .url(DAO.ALERTMONITOR_PROMETHEUS_SERVER + "/api/v1/targets")
                .addHeader("User-Agent", HTTP_CLIENT_USER_AGENT)
                .get()
                .build();

        String responseBody = execute(request);

        if (responseBody != null && responseBody.trim().length() > 0) {

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            PTargetMessage targetMessage = gson.fromJson(responseBody, PTargetMessage.class);

            return targetMessage.getData().getActiveTargets();

        }

        return null;

    }

    public void reload() throws PrometheusApiException {
        Request request = new Request.Builder()
                .url(DAO.ALERTMONITOR_PROMETHEUS_SERVER + "/-/reload")
                .addHeader("User-Agent", HTTP_CLIENT_USER_AGENT)
                .post(RequestBody.create("".getBytes()))
                .build();

        execute(request);
    }

    public List<PRule> rules() throws PrometheusApiException {
        Request request = new Request.Builder()
                .url(DAO.ALERTMONITOR_PROMETHEUS_SERVER + "/api/v1/rules")
                .addHeader("User-Agent", HTTP_CLIENT_USER_AGENT)
                .get()
                .build();

        String responseBody = execute(request);

        if (responseBody != null && responseBody.trim().length() > 0) {

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            PRuleMessage rulesMessage = gson.fromJson(responseBody, PRuleMessage.class);
            
            Map<String, PRule> rulesMap = new HashMap<>();

            for (PRuleGroup g : rulesMessage.getData().getGroups()) {
                for (PRule rule : g.getRules()) {
                    if (!rulesMap.containsKey(rule.getName() + rule.getQuery() + rule.getDuration())) {
                        rulesMap.put(rule.getName() + rule.getQuery() + rule.getDuration(), rule);
                    }
                }
            }

            return new ArrayList<>(rulesMap.values());

        }

        return null;
    }

    private String execute(Request request) throws PrometheusApiException {

        String responseBody = null;
        long before = System.currentTimeMillis();
        String code = "0";

        try {

            OkHttpClient httpClient = HttpClientFactory.instantiateHttpClient();

            logger.info("PrometheusApi: request " + request.method().toUpperCase() + " " + request.url().toString());
            Response response = httpClient.newCall(request).execute();
            logger.info("PrometheusApi: response: code=" + response.code() + ", success=" + response.isSuccessful());

            code = Integer.toString(response.code());

            if (response.isSuccessful()) {
                responseBody = response.body().string();
                logger.debug("PrometheusApi: response: " + responseBody);
            }

            response.close();

        } catch (UnknownHostException e) {
            logger.error("PrometheusApi: UnknownHostException: " + e.getMessage());
            DAO.psyncFailedCount++;
            code = "0";
            throw new PrometheusApiException("UnknownHostException");
        } catch (SocketTimeoutException e) {
            logger.error("PrometheusApi: SocketTimeoutException: " + e.getMessage());
            DAO.psyncFailedCount++;
            code = "0";
            throw new PrometheusApiException("SocketTimeoutException");
        } catch (SocketException e) {
            logger.error("PrometheusApi: SocketException: " + e.getMessage());
            DAO.psyncFailedCount++;
            code = "0";
            throw new PrometheusApiException("SocketException");
        } catch (SSLException e) {
            logger.error("PrometheusApi: SSLException: " + e.getMessage());
            DAO.psyncFailedCount++;
            code = "0";
            throw new PrometheusApiException("SSLException");
        } catch (Exception e) {
            logger.error("PrometheusApi: Exception: ", e);
            e.printStackTrace();
            DAO.psyncFailedCount++;
            code = "0";
            throw new PrometheusApiException("Exception");
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            AmMetrics.alertmonitor_prom_api_duration_seconds.labels(request.method(), code, request.url().toString()).observe(duration);
        }

        return responseBody;

    }

}
