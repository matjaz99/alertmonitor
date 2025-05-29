# Build stage
#FROM maven:3.8.1-openjdk-11 AS build
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

ARG SIMPLE_LOGGER_VERSION=1.7.0
RUN wget http://matjazcerkvenik.si/download/simple-logger-${SIMPLE_LOGGER_VERSION}.jar
RUN mvn install:install-file \
    -Dfile=simple-logger-${SIMPLE_LOGGER_VERSION}.jar \
    -DgroupId=si.matjazcerkvenik.simplelogger \
    -DartifactId=simple-logger \
    -Dversion=${SIMPLE_LOGGER_VERSION} \
    -Dpackaging=jar

COPY src /app/src
COPY pom.xml /app
RUN mvn -f /app/pom.xml clean package

# Package stage
FROM quay.io/wildfly/wildfly:latest

ENV WILDFLY_HOME=/opt/jboss/wildfly

COPY --from=build  /app/target/alertmonitor.war $WILDFLY_HOME/standalone/deployments/alertmonitor.war

USER root

RUN microdnf update -y && \
    microdnf install -y tzdata && \
    microdnf clean all

RUN mkdir -p /opt/jboss/alertmonitor/log
COPY LICENSE /opt/jboss/alertmonitor
COPY NOTICE /opt/jboss/alertmonitor
COPY README.md /opt/jboss/alertmonitor
COPY CHANGELOG.md /opt/jboss/alertmonitor

RUN chown -R jboss:jboss /opt/jboss/alertmonitor

USER jboss

#HEALTHCHECK --interval=5m --timeout=3s \
#  CMD curl -f http://localhost:8080/alertmonitor || exit 1

EXPOSE 8080

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]


