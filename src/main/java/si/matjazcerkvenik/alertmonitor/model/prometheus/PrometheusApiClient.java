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
import okhttp3.*;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.HttpClientFactory;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import javax.net.ssl.SSLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles all the communication with Prometheus server via HTTP API.
 */
public class PrometheusApiClient {

    private final SimpleLogger logger = LogFactory.getLogger();

    /** Name of this client - provider name */
    private String name = ".default";

    private final String HTTP_CLIENT_USER_AGENT = "Alertmonitor/v1";

    private static long requestCount;

    private String server;

    private boolean secureClient = false;
    private int connectTimeout = 10;
    private int readTimeout = 120;

    public PrometheusApiClient(boolean secure, int connectTimeout, int readTimeout, String server) {
        this.secureClient = secure;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Execute a simple query
     * @param query the query
     * @return query response object
     * @throws PrometheusApiException error
     */
    public PQueryMessage query(String query) throws PrometheusApiException {

        logger.info("PrometheusApi: query: " + query);

        RequestBody formBody = new FormBody.Builder()
                // add url encoded parameters
                .add("query", query)
                .build();

        Request request = new Request.Builder()
                .url(server + "/api/v1/query")
                .addHeader("User-Agent", HTTP_CLIENT_USER_AGENT)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build();

        return doQueryRequest(request);

    }

    /**
     * Execute query_range
     * @param query the query
     * @param start start time in seconds (UNIX time)
     * @param end end time in seconds (UNIX time)
     * @param step eg. 5m
     * @return query response object
     * @throws PrometheusApiException error
     */
    public PQueryMessage queryRange(String query, long start, long end, String step) throws PrometheusApiException {

        logger.info("PrometheusApi: queryRange: " + query);

        RequestBody formBody = new FormBody.Builder()
                // add url encoded parameters
                .add("query", query)
                .add("start", String.valueOf(start))
                .add("end", String.valueOf(end))
                .add("step", step)
                .build();

        Request request = new Request.Builder()
                .url(server + "/api/v1/query_range")
                .addHeader("User-Agent", HTTP_CLIENT_USER_AGENT)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build();

        return doQueryRequest(request);
    }

    /**
     * This method will actually do the execution of request - query or query_range. The result is
     * the same in both cases.
     * @param request request
     * @return query response object
     * @throws PrometheusApiException error
     */
    private PQueryMessage doQueryRequest(Request request) throws PrometheusApiException {
        String responseBody = execute(request);

        if (responseBody != null && responseBody.trim().length() > 0) {

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            PQueryMessage msg = gson.fromJson(responseBody, PQueryMessage.class);
            logger.info("status: " + msg.getStatus());

            return msg;

        }

        return null;
    }

    public List<PAlert> alerts() throws PrometheusApiException {

        Request request = new Request.Builder()
                .url(server + "/api/v1/alerts")
                .addHeader("User-Agent", HTTP_CLIENT_USER_AGENT)
                .get()
                .build();

        String responseBody = execute(request);

        if (responseBody != null && responseBody.trim().length() > 0) {

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            PAlertsMessage amMsg = gson.fromJson(responseBody, PAlertsMessage.class);

            return amMsg.getData().getAlerts();

        }

        return null;

    }

    public List<PTarget> targets() throws PrometheusApiException {

        Request request = new Request.Builder()
                .url(server + "/api/v1/targets")
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
                .url(server + "/-/reload")
                .addHeader("User-Agent", HTTP_CLIENT_USER_AGENT)
                .post(RequestBody.create("".getBytes()))
                .build();

        execute(request);
    }

    public List<PRule> rules() throws PrometheusApiException {
        Request request = new Request.Builder()
                .url(server + "/api/v1/rules")
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

    /**
     * This method will actually execute the given HTTP request.
     * @param request prepared request
     * @return response body
     * @throws PrometheusApiException error
     */
    private String execute(Request request) throws PrometheusApiException {

        requestCount++;

        String responseBody = null;
        long before = System.currentTimeMillis();
        String code = "0";

        try {

            // TODO new env var connect timeout
            OkHttpClient httpClient = HttpClientFactory.instantiateHttpClient(secureClient, connectTimeout, readTimeout);

            logger.info("PrometheusApi: request[" + requestCount + "] " + request.method().toUpperCase() + " " + request.url().toString());
            Response response = httpClient.newCall(request).execute();
            logger.info("PrometheusApi: request[" + requestCount + "] code=" + response.code() + ", success=" + response.isSuccessful());

            code = Integer.toString(response.code());

            if (response.body() != null) {
                responseBody = response.body().string();
                logger.debug("PrometheusApi: request[" + requestCount + "] body: " + responseBody);
            }

            response.close();

            DAO.getInstance().removeWarning("prom_api");

        } catch (UnknownHostException e) {
            logger.error("PrometheusApi: request[" + requestCount + "] failed: UnknownHostException: " + e.getMessage());
            code = "0";
            DAO.getInstance().addWarning("prom_api", "Prometheus API not reachable");
            throw new PrometheusApiException("Unknown Host");
        } catch (SocketTimeoutException e) {
            logger.error("PrometheusApi: request[" + requestCount + "] failed: SocketTimeoutException: " + e.getMessage());
            code = "0";
            DAO.getInstance().addWarning("prom_api", "Prometheus API not reachable");
            throw new PrometheusApiException("Timeout");
        } catch (SocketException e) {
            logger.error("PrometheusApi: request[" + requestCount + "] failed: SocketException: " + e.getMessage());
            code = "0";
            DAO.getInstance().addWarning("prom_api", "Prometheus API not reachable");
            throw new PrometheusApiException("Socket Error");
        } catch (SSLException e) {
            logger.error("PrometheusApi: request[" + requestCount + "] failed: SSLException: " + e.getMessage());
            code = "0";
            DAO.getInstance().addWarning("prom_api", "Prometheus API not reachable");
            throw new PrometheusApiException("SSL Exception");
        } catch (Exception e) {
            logger.error("PrometheusApi: request[" + requestCount + "] failed: Exception: ", e);
            code = "0";
            DAO.getInstance().addWarning("prom_api", "Prometheus API not reachable");
            throw new PrometheusApiException("Unknown Exception");
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            AmMetrics.alertmonitor_prom_api_duration_seconds.labels(name, request.method(), code, request.url().toString()).observe(duration);
        }

        return responseBody;

    }

}
