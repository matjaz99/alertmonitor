FROM tomcat:8.5-jre8-alpine

RUN apk --no-cache add curl

COPY target/alertmonitor.war /usr/local/tomcat/webapps/alertmonitor.war

RUN mkdir -p /opt/alertmonitor/log

#HEALTHCHECK --interval=5m --timeout=3s \
#  CMD curl -f http://localhost:8080/alertmonitor || exit 1

EXPOSE 8080

