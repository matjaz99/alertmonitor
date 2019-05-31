package si.matjazcerkvenik.alertmonitor.webhook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import si.matjazcerkvenik.alertmonitor.util.MD5Checksum;

public class WebhookServlet extends HttpServlet {

	private static final long serialVersionUID = 4274913262329715396L;
	
	public static List<HttpMessage> messages = new LinkedList<HttpMessage>();
	public static List<AmAlertMessage> amMessages = new LinkedList<AmAlertMessage>();
	public static List<DNotification> dNotifs = new LinkedList<DNotification>();
	public static Map<String, DNotification> activeAlerts = new HashMap<String, DNotification>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// $ curl http://localhost:8080/DTools/api/webhook/blablabla
		// $ curl 'http://localhost:8080/DTools/api/webhook/blablabla?a=1&b=2'

		System.out.println("doGet(): getAuthType: " + req.getAuthType());
		System.out.println("doGet(): getCharacterEncoding: " + req.getCharacterEncoding());
		System.out.println("doGet(): getContentLength: " + req.getContentLength());
		System.out.println("doGet(): getContentType: " + req.getContentType());
		System.out.println("doGet(): getContextPath: " + req.getContextPath());
		System.out.println("doGet(): getLocalAddr: " + req.getLocalAddr());
		System.out.println("doGet(): getLocalName: " + req.getLocalName());
		System.out.println("doGet(): getLocalPort: " + req.getLocalPort());
		System.out.println("doGet(): getMethod: " + req.getMethod());
		System.out.println("doGet(): getParameter: " + req.getParameter("aaa"));
		System.out.println("doGet(): getPathInfo: " + req.getPathInfo());
		System.out.println("doGet(): getPathTranslated: " + req.getPathTranslated());
		System.out.println("doGet(): getProtocol: " + req.getProtocol());
		System.out.println("doGet(): getQueryString: " + req.getQueryString());
		System.out.println("doGet(): getRemoteAddr: " + req.getRemoteAddr());
		System.out.println("doGet(): getRemoteHost: " + req.getRemoteHost());
		System.out.println("doGet(): getRemotePort: " + req.getRemotePort());
		System.out.println("doGet(): getRemoteUser: " + req.getRemoteUser());
		System.out.println("doGet(): getRequestedSessionId: " + req.getRequestedSessionId());
		System.out.println("doGet(): getRequestURI: " + req.getRequestURI());
		System.out.println("doGet(): getScheme: " + req.getScheme());
		System.out.println("doGet(): getServerName: " + req.getServerName());
		System.out.println("doGet(): getServerPort: " + req.getServerPort());
		System.out.println("doGet(): getServletPath: " + req.getServletPath());
		
		System.out.println("doGet(): parameterMap: " + getReqParams(req));
		System.out.println("doGet(): headers: " + getReqHeaders(req));
		
		HttpMessage m = new HttpMessage();
		m.setTimestamp(System.currentTimeMillis());
		m.setContentLength(req.getContentLength());
		m.setContentType(req.getContentType());
		m.setMethod(req.getMethod());
		m.setPathInfo(req.getPathInfo());
		m.setProtocol(req.getProtocol());
		m.setRemoteHost(req.getRemoteHost());
		m.setRemotePort(req.getRemotePort());
		m.setRequestUri(req.getRequestURI());
		
		m.setBody(req.getPathInfo() + " " + generateParamMap(req));
		m.setHeaderMap(generateHeaderMap(req));
		m.setParameterMap(generateParamMap(req));
		
		messages.add(m);

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println("doPost(): getAuthType: " + req.getAuthType());
		System.out.println("doPost(): getCharacterEncoding: " + req.getCharacterEncoding());
		System.out.println("doPost(): getContentLength: " + req.getContentLength());
		System.out.println("doPost(): getContentType: " + req.getContentType());
		System.out.println("doPost(): getContextPath: " + req.getContextPath());
		System.out.println("doPost(): getLocalAddr: " + req.getLocalAddr());
		System.out.println("doPost(): getLocalName: " + req.getLocalName());
		System.out.println("doPost(): getLocalPort: " + req.getLocalPort());
		System.out.println("doPost(): getMethod: " + req.getMethod());
		System.out.println("doPost(): getParameter: " + req.getParameter("aaa"));
		System.out.println("doPost(): getPathInfo: " + req.getPathInfo());
		System.out.println("doPost(): getPathTranslated: " + req.getPathTranslated());
		System.out.println("doPost(): getProtocol: " + req.getProtocol());
		System.out.println("doPost(): getQueryString: " + req.getQueryString());
		System.out.println("doPost(): getRemoteAddr: " + req.getRemoteAddr());
		System.out.println("doPost(): getRemoteHost: " + req.getRemoteHost());
		System.out.println("doPost(): getRemotePort: " + req.getRemotePort());
		System.out.println("doPost(): getRemoteUser: " + req.getRemoteUser());
		System.out.println("doPost(): getRequestedSessionId: " + req.getRequestedSessionId());
		System.out.println("doPost(): getRequestURI: " + req.getRequestURI());
		System.out.println("doPost(): getScheme: " + req.getScheme());
		System.out.println("doPost(): getServerName: " + req.getServerName());
		System.out.println("doPost(): getServerPort: " + req.getServerPort());
		System.out.println("doPost(): getServletPath: " + req.getServletPath());
		
		String body = getReqBody(req);
		System.out.println("doGet(): parameterMap: " + getReqParams(req));
		System.out.println("doPost(): body: " + body);
		System.out.println("doPost(): headers: " + getReqHeaders(req));
		
		HttpMessage m = new HttpMessage();
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
		
		messages.add(m);
		
		if (m.getHeaderMap().containsKey("user-agent")) {
			String userAgent = m.getHeaderMap().get("user-agent");
			
			if (userAgent.startsWith("Alertmanager")) {
				
				// headers: host=172.30.19.6:8080, user-agent=Alertmanager/0.15.3, content-length=1889, content-type=application/json, 
				
				processAlertmanagerMessage(m);
				
			}
			
		}

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
	
	
	private String getReqHeaders(HttpServletRequest req) {
		
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
	
	private String getReqParams(HttpServletRequest req) {
		Map<String, String[]> parameterMap = req.getParameterMap();
		String params = "";
		for (Iterator<String> it = parameterMap.keySet().iterator(); it.hasNext();) {
			String s = it.next();
			params += s + "=" + parameterMap.get(s)[0] + ", ";
		}
		return params;
	}
	
	private String getReqBody(HttpServletRequest req) throws IOException {
		
		String body = "";
		String s = req.getReader().readLine();
		while (s != null) {
			body += s;
			s = req.getReader().readLine();
		}
		
		return body;
		
	}
	
	private void processAlertmanagerMessage(HttpMessage m) {
		
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		AmAlertMessage am = gson.fromJson(m.getBody(), AmAlertMessage.class);
		System.out.println(am.toString());
		System.out.println("Number of alerts: " + am.getAlerts().size());
		amMessages.add(am);
		
		List<DNotification> dn = convertToDNotif(m, am);
		dNotifs.addAll(dn);
		
		
		// resynchronization
		
		for (DNotification n : dn) {
			if (n.getSeverity().equalsIgnoreCase("informational")) {
				continue;
			}
			if (activeAlerts.containsKey(n.getNid())) {
				if (n.getSeverity().equalsIgnoreCase("clear")) {
					System.out.println("Removing active alarm: " + n.getNid());
					activeAlerts.remove(n.getNid());
				} else {
					activeAlerts.get(n.getNid()).setLastTimestamp(n.getTimestamp());
					activeAlerts.get(n.getNid()).setCounter(n.getCounter() + 1);
					System.out.println("Updating active alarm: " + n.getNid());
				}
			} else {
				// if clear comes to here, then it means there is no such NID to be deleted. Such clear can be ignored.
				if (!n.getSeverity().equalsIgnoreCase("clear")) {
					activeAlerts.put(n.getNid(), n);
					System.out.println("Adding active alarm: " + n.getNid());
				}
			}
		}
		
		
		
	}
	
	private List<DNotification> convertToDNotif(HttpMessage m, AmAlertMessage am) {
		
		List<DNotification> notifs = new ArrayList<DNotification>();
		
		for (Iterator<Alert> it = am.getAlerts().iterator(); it.hasNext();) {
			Alert a = it.next();
			
			DNotification n = new DNotification();
			n.setTimestamp(System.currentTimeMillis());
			n.setSource(m.getRemoteHost());
			n.setAlertname(a.getLabels().get("alertname"));
			
			if (m.getHeaderMap().containsKey("user-agent")) {
				n.setUserAgent(m.getHeaderMap().get("user-agent"));
			} else {
				n.setUserAgent("unknown");
			}

			if (a.getLabels().containsKey("alertdomain")) {
				n.setAlertdomain(a.getLabels().get("alertdomain"));
			} else {
				n.setAlertdomain("unknown");
			}
			
			if (a.getLabels().containsKey("alerttype")) {
				n.setAlerttype(a.getLabels().get("alerttype"));
			} else {
				n.setAlerttype("unknown");
			}
			
			if (a.getLabels().containsKey("instance")) {
				n.setInstance(a.getLabels().get("instance"));
			} else {
				n.setInstance("unknown");
			}
			
			if (a.getLabels().containsKey("nodename")) {
				n.setNodename(a.getLabels().get("nodename"));
			} else {
				n.setNodename(n.getInstance());
			}
			
			if (a.getLabels().containsKey("severity")) {
				n.setSeverity(a.getLabels().get("severity"));
			} else {
				n.setSeverity("indeterminate");
			}
			
			if (a.getLabels().containsKey("priority")) {
				n.setPriority(a.getLabels().get("priority"));
			} else {
				n.setPriority("low");
			}
			
			if (a.getStatus().equalsIgnoreCase("resolved")) {
				// set severity=clear for all events that have status=resolved, but not for those with severity=informational
				if (!n.getSeverity().equalsIgnoreCase("informational")) {
					n.setSeverity("clear");
				}
			}
			
			if (a.getAnnotations().containsKey("summary")) {
				n.setSummary(a.getAnnotations().get("summary"));
			} else {
				n.setSummary("-");
			}
			
			if (a.getAnnotations().containsKey("description")) {
				n.setDescription(a.getAnnotations().get("description"));
			} else {
				n.setDescription("-");
			}
			
			n.setStatus(a.getStatus());
			n.setUid(MD5Checksum.getMd5Checksum(n.getTimestamp() + n.hashCode()
				+ n.getPriority() + n.getAlertname() + n.getAlertdomain() 
				+ n.getAlerttype() + n.getInstance() + n.getSummary() 
				+ n.getDescription() + new Random().nextInt(9999999) + n.getSource()
				+ n.getUserAgent()));
			
			n.setNid(MD5Checksum.getMd5Checksum(n.getAlertname() + n.getAlertdomain() 
			+ n.getAlerttype() + n.getInstance() + n.getSummary()));
			
//			DNotification found = null;
//			for (Iterator<DNotification> it1 = dNotifs.iterator(); it1.hasNext();) {
//				DNotification dn = it1.next();
//				if (dn.getUid().equalsIgnoreCase(n.getUid())) {
//					found = dn;
//					if (n.getSeverity().equalsIgnoreCase("clear")) {
//						
//					}
//					break;
//				}
//			}
//			if (found == null) {
//				notifs.add(n);
//			}
			notifs.add(n);
			
		}
		
		return notifs;
		
	}

}
