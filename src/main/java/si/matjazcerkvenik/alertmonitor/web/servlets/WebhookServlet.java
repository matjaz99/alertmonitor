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
package si.matjazcerkvenik.alertmonitor.web.servlets;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.providers.AbstractDataProvider;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

public class WebhookServlet extends HttpServlet implements Serializable {

	private static final long serialVersionUID = 4274913262329715396L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET method not allowed");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
			throws IOException {

		AbstractDataProvider dataProvider = DAO.getInstance().getDataProviderByUri(req.getRequestURI());

		if (dataProvider == null) {
			LogFactory.getLogger().warn("WebhookServlet: doPost(): dataprovider not found for endpoint: " + req.getRequestURI());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "dataprovider not found");
			return;
		}

		LogFactory.getLogger().info("WebhookServlet: doPost(): dataprovider found: " + dataProvider.getProviderConfig().getName() + "@" + dataProvider.getProviderConfig().getUri());
		WebhookMessage m = instantiateWebhookRequest(req, dataProvider);
		dataProvider.processIncomingEvent(m);

	}

	private WebhookMessage instantiateWebhookRequest(HttpServletRequest req, AbstractDataProvider dataProvider) throws IOException {

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("protocol=").append(req.getProtocol()).append(", ");
		sb.append("remoteAddr=").append(req.getRemoteAddr()).append(", ");
		sb.append("remoteHost=").append(req.getRemoteHost()).append(", ");
		sb.append("remotePort=").append(req.getRemotePort()).append(", ");
		sb.append("method=").append(req.getMethod()).append(", ");
		sb.append("requestURI=").append(req.getRequestURI()).append(", ");
		sb.append("scheme=").append(req.getScheme()).append(", ");
		sb.append("characterEncoding=").append(req.getCharacterEncoding()).append(", ");
		sb.append("contentLength=").append(req.getContentLength()).append(", ");
		sb.append("contentType=").append(req.getContentType());
		sb.append("}");

		LogFactory.getLogger().info("WebhookServlet: instantiateWebhookMessage(): " + sb.toString());

		LogFactory.getLogger().debug("WebhookServlet: instantiateWebhookMessage(): parameterMap: " + getReqParamsAsString(req));
		LogFactory.getLogger().debug("WebhookServlet: instantiateWebhookMessage(): headers: " + getReqHeadersAsString(req));
		String body = getReqBody(req);
		LogFactory.getLogger().trace("WebhookServlet: instantiateWebhookMessage(): body: " + body);

		WebhookMessage m = new WebhookMessage();
		m.setId(dataProvider.getWebhookRequestsReceivedCount());
		m.setRuntimeId(AmProps.RUNTIME_ID);
		m.setTimestamp(System.currentTimeMillis());
		m.setContentLength(req.getContentLength());
		m.setContentType(req.getContentType());
		m.setMethod(req.getMethod());
		m.setProtocol(req.getProtocol());
		m.setRemoteHost(req.getRemoteHost());
		m.setRemotePort(req.getRemotePort());
		String reqUri = req.getRequestURI();
		// remove trailing slash
		if (reqUri.endsWith("/")) reqUri = reqUri.substring(0, reqUri.length() - 1);
		m.setRequestUri(reqUri);
		m.setBody(body);
		m.setHeaderMap(generateHeaderMap(req));
		m.setParameterMap(generateParamMap(req));

		return m;
	}
	
	
	private Map<String, String> generateHeaderMap(HttpServletRequest req) {
		
		Map<String, String> m = new HashMap<String, String>();
		
		Enumeration<String> headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			String val = req.getHeader(key);
			m.put(key, val);
		}
		return m;
	}
	
	private Map<String, String> generateParamMap(HttpServletRequest req) {
		
		Map<String, String> m = new HashMap<String, String>();
		Map<String, String[]> parameterMap = req.getParameterMap();
		
		for (Iterator<String> it = parameterMap.keySet().iterator(); it.hasNext();) {
			String s = it.next();
			m.put(s, parameterMap.get(s)[0]);
		}
		return m;
	}
	
	
	private String getReqHeadersAsString(HttpServletRequest req) {
		
		String headers = "";
		Enumeration<String> headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			//headerNames.toString();
			String val = req.getHeader(key);
			headers += key + "=" + val + ", ";
		}
		return headers;
		
	}
	
	private String getReqParamsAsString(HttpServletRequest req) {
		Map<String, String[]> parameterMap = req.getParameterMap();
		String params = "";
		for (Iterator<String> it = parameterMap.keySet().iterator(); it.hasNext();) {
			String s = it.next();
			params += s + "=" + parameterMap.get(s)[0] + ", ";
		}
		return params;
	}
	
	private String getReqBody(HttpServletRequest req) throws IOException {

		if (req.getMethod().equalsIgnoreCase("get")) {
			return req.getPathInfo() + " " + generateParamMap(req);
		}

		String body = "";
		String s = req.getReader().readLine();
		while (s != null) {
			body += s;
			s = req.getReader().readLine();
		}
		
		return body;
		
	}
}
