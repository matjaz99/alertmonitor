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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

public class WebhookMessage {

	private long id;
	private long timestamp;
	private int contentLength;
	private String contentType;
	private String method;
	private String pathInfo;
	private String protocol;
	private String remoteHost;
	private int remotePort;
	private String requestUri;
	private String body;
	private Map<String, String> parameterMap;
	private Map<String, String> headerMap;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getFormatedTimestamp() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd H:mm:ss");
		return sdf.format(cal.getTime());
	}

	public int getContentLength() {
		return contentLength;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method.toUpperCase();
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public String getRequestUri() {
		return requestUri;
	}

	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Map<String, String> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public Map<String, String> getHeaderMap() {
		return headerMap;
	}

	public void setHeaderMap(Map<String, String> headerMap) {
		this.headerMap = headerMap;
	}
	
	public String getParameterMapAsString() {
		return mapToString(parameterMap);
	}
	
	public String getHeaderMapAsString() {
		return mapToString(headerMap);
	}
	
	private String mapToString(Map<String, String> m) {
		
		String params = "[";
		for (Iterator<String> it = m.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			params += key + "=" + m.get(key) + ", ";
		}
		params += "]";
		
		return params;
		
	}

}
