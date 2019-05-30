FROM tomcat:8.5.37-jre8

COPY target/alertmonitor.war /usr/local/tomcat/webapps/alertmonitor.war


