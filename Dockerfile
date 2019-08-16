FROM tomcat:8.5-jre8-alpine

COPY target/alertmonitor.war /usr/local/tomcat/webapps/alertmonitor.war

# RUN mkdir -p /opt/alertmonitor

# COPY version.txt /opt/alertmonitor/version.txt
