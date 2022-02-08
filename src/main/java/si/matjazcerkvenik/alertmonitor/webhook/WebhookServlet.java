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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerProcessor;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

public class WebhookServlet extends HttpServlet {

	private static final long serialVersionUID = 4274913262329715396L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// $ curl http://localhost:8080/DTools/api/webhook/blablabla
		// $ curl 'http://localhost:8080/DTools/api/webhook/blablabla?a=1&b=2'

		WebhookMessage m = instantiateWebhookMessage(req);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {

		WebhookMessage m = instantiateWebhookMessage(req);

//		String userAgent = m.getHeaderMap().getOrDefault("user-agent", "-");
//		if (userAgent.startsWith("Alertmanager")) {
//			// example headers: host=172.30.19.6:8080, user-agent=Alertmanager/0.15.3, content-length=1889, content-type=application/json,
//			AlertmanagerProcessor.processWebhookMessage(m);
//		}

		try {
			AlertmanagerProcessor.processWebhookMessage(m);
		} catch (Exception e) {
			LogFactory.getLogger().error("doPost(): failed to process webhook message(): " + e.getMessage());
		}

	}

	private WebhookMessage instantiateWebhookMessage(HttpServletRequest req) throws IOException {

//		StringBuilder sb = new StringBuilder();
//		sb.append("{");

		LogFactory.getLogger().info("instantiateWebhookMessage(): getAuthType: " + req.getAuthType());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getCharacterEncoding: " + req.getCharacterEncoding());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getContentLength: " + req.getContentLength());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getContentType: " + req.getContentType());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getContextPath: " + req.getContextPath());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getLocalAddr: " + req.getLocalAddr());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getLocalName: " + req.getLocalName());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getLocalPort: " + req.getLocalPort());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getMethod: " + req.getMethod());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getParameter: " + req.getParameter("aaa"));
		LogFactory.getLogger().info("instantiateWebhookMessage(): getPathInfo: " + req.getPathInfo());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getPathTranslated: " + req.getPathTranslated());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getProtocol: " + req.getProtocol());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getQueryString: " + req.getQueryString());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getRemoteAddr: " + req.getRemoteAddr());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getRemoteHost: " + req.getRemoteHost());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getRemotePort: " + req.getRemotePort());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getRemoteUser: " + req.getRemoteUser());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getRequestedSessionId: " + req.getRequestedSessionId());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getRequestURI: " + req.getRequestURI());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getScheme: " + req.getScheme());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getServerName: " + req.getServerName());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getServerPort: " + req.getServerPort());
		LogFactory.getLogger().info("instantiateWebhookMessage(): getServletPath: " + req.getServletPath());

		LogFactory.getLogger().info("instantiateWebhookMessage(): parameterMap: " + getReqParamsAsString(req));
		LogFactory.getLogger().info("instantiateWebhookMessage(): headers: " + getReqHeadersAsString(req));
		String body = getReqBody(req);
		LogFactory.getLogger().info("instantiateWebhookMessage(): body: " + body);

		WebhookMessage m = new WebhookMessage();
		m.setId(AmMetrics.webhookMessagesReceivedCount);
		m.setTimestamp(System.currentTimeMillis());
		m.setContentLength(req.getContentLength());
		m.setContentType(req.getContentType());
		m.setMethod(req.getMethod());
		m.setPathInfo(req.getPathInfo());
		m.setProtocol(req.getProtocol());
		m.setRemoteHost(req.getRemoteHost());
		m.setRemotePort(req.getRemotePort());
		m.setRequestUri(req.getRequestURI());
		m.setBody(body);
		m.setHeaderMap(generateHeaderMap(req));
		m.setParameterMap(generateParamMap(req));

		DAO.getInstance().addWebhookMessage(m);

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
