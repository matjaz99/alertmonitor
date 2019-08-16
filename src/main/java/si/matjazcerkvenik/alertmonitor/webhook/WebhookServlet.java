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

import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerProcessor;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;

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

		if (m.getHeaderMap().containsKey("user-agent")) {
			String userAgent = m.getHeaderMap().get("user-agent");
			
			if (userAgent.startsWith("Alertmanager")) {
				
				// headers: host=172.30.19.6:8080, user-agent=Alertmanager/0.15.3, content-length=1889, content-type=application/json, 
				
				AlertmanagerProcessor.processAlertmanagerMessage(m);
				
			}
			
		}

	}

	private WebhookMessage instantiateWebhookMessage(HttpServletRequest req) throws IOException {

		System.out.println("instantiateWebhookMessage(): getAuthType: " + req.getAuthType());
		System.out.println("instantiateWebhookMessage(): getCharacterEncoding: " + req.getCharacterEncoding());
		System.out.println("instantiateWebhookMessage(): getContentLength: " + req.getContentLength());
		System.out.println("instantiateWebhookMessage(): getContentType: " + req.getContentType());
		System.out.println("instantiateWebhookMessage(): getContextPath: " + req.getContextPath());
		System.out.println("instantiateWebhookMessage(): getLocalAddr: " + req.getLocalAddr());
		System.out.println("instantiateWebhookMessage(): getLocalName: " + req.getLocalName());
		System.out.println("instantiateWebhookMessage(): getLocalPort: " + req.getLocalPort());
		System.out.println("instantiateWebhookMessage(): getMethod: " + req.getMethod());
		System.out.println("instantiateWebhookMessage(): getParameter: " + req.getParameter("aaa"));
		System.out.println("instantiateWebhookMessage(): getPathInfo: " + req.getPathInfo());
		System.out.println("instantiateWebhookMessage(): getPathTranslated: " + req.getPathTranslated());
		System.out.println("instantiateWebhookMessage(): getProtocol: " + req.getProtocol());
		System.out.println("instantiateWebhookMessage(): getQueryString: " + req.getQueryString());
		System.out.println("instantiateWebhookMessage(): getRemoteAddr: " + req.getRemoteAddr());
		System.out.println("instantiateWebhookMessage(): getRemoteHost: " + req.getRemoteHost());
		System.out.println("instantiateWebhookMessage(): getRemotePort: " + req.getRemotePort());
		System.out.println("instantiateWebhookMessage(): getRemoteUser: " + req.getRemoteUser());
		System.out.println("instantiateWebhookMessage(): getRequestedSessionId: " + req.getRequestedSessionId());
		System.out.println("instantiateWebhookMessage(): getRequestURI: " + req.getRequestURI());
		System.out.println("instantiateWebhookMessage(): getScheme: " + req.getScheme());
		System.out.println("instantiateWebhookMessage(): getServerName: " + req.getServerName());
		System.out.println("instantiateWebhookMessage(): getServerPort: " + req.getServerPort());
		System.out.println("instantiateWebhookMessage(): getServletPath: " + req.getServletPath());

		System.out.println("instantiateWebhookMessage(): parameterMap: " + getReqParamsAsString(req));
		System.out.println("instantiateWebhookMessage(): headers: " + getReqHeadersAsString(req));
		String body = getReqBody(req);
		System.out.println("instantiateWebhookMessage(): body: " + body);

		WebhookMessage m = new WebhookMessage();
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
		DAO.webhookMessagesReceivedCount++;

		AmMetrics.alertmonitor_webhook_messages_received_total.labels(req.getRemoteHost(), req.getMethod().toUpperCase()).inc();

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
