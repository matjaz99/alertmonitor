#FROM tomcat:8.5.37-jre8
FROM tomcat:8.5-jre8-alpine

COPY target/alertmonitor.war /usr/local/tomcat/webapps/alertmonitor.war


