# Build stage
FROM maven:3.8.1-openjdk-11 AS build

COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

# Package stage
FROM tomcat:8.5-jre8-alpine

RUN apk --no-cache add curl
RUN apk add tzdata

COPY --from=build  /home/app/target/alertmonitor.war /usr/local/tomcat/webapps/alertmonitor.war

RUN mkdir -p /opt/alertmonitor/log
COPY LICENSE /opt/alertmonitor
COPY NOTICE /opt/alertmonitor
COPY README.md /opt/alertmonitor
COPY CHANGELOG.md /opt/alertmonitor

#HEALTHCHECK --interval=5m --timeout=3s \
#  CMD curl -f http://localhost:8080/alertmonitor || exit 1

EXPOSE 8080

