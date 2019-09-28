# Alertmonitor

[![Build Status](https://semaphoreci.com/api/v1/matjaz99/alertmonitor/branches/master/shields_badge.svg)](https://semaphoreci.com/matjaz99/alertmonitor)
[![GitHub release](https://img.shields.io/github/release/matjaz99/alertmonitor.svg)](https://GitHub.com/matjaz99/alertmonitor/releases/)
[![Github all releases](https://img.shields.io/github/downloads/matjaz99/alertmonitor/total.svg)](https://GitHub.com/matjaz99/alertmonitor/releases/)
[![HitCount](http://hits.dwyl.io/matjaz99/alertmonitor.svg)](http://hits.dwyl.io/matjaz99/alertmonitor)
[![Docker Pulls](https://img.shields.io/docker/pulls/matjaz99/alertmonitor.svg)](https://hub.docker.com/r/matjaz99/alertmonitor)
[![GitHub issues](https://img.shields.io/github/issues/matjaz99/alertmonitor.svg)](https://GitHub.com/matjaz99/alertmonitor/issues/)

Alertmonitor is a web GUI for displaying alerts from Prometheus Alertmanager.

A generic webhook accepts any HTTP GET or POST request that comes on URI endpoint: `/alertmonitor/webhook`.

If the request is recognized to come from Alertmanager, it will be processed and displayed as alarm.

Alertmonitor provides three views:
- Raw - anything that is received on webhook
- Journal - history of all events
- Active - only active alerts

Alertmonitor correlates alarms and clears to display selected alarms (ie. alarms which haven't received clear yet).

Easily filter alerts by tags.

Alertmonitor GUI is reachable on: http://hostname:8080/alertmonitor/

> Currently Alertmonitor does not support any persistence. Alerts are stored in memory. After restart alerts are gone.

## Install

The easiest way to start using Alertmonitor is to deploy it on Docker.

Deploy container:

```
docker run -d -p 8080:8080 matjaz99/alertmonitor:latest
```

There is also `docker-compose.yml` file for deployment in Swarm cluster.

## Docker images

Docker images are available on Docker hub: [https://hub.docker.com/r/matjaz99/alertmonitor](https://hub.docker.com/r/matjaz99/alertmonitor)

## Configure alerts in Prometheus

Alert rules in Prometheus should have properly configured labels.

#### Labeling alerts

Alertmonitor recognizes the following labels:

| Label       |      Description        |
|-------------|-------------------------|
| severity    | :arrow_right: Mandatory. Severity is the weight of event. Possible values: `critical`, `major`, `minor`, `warning`, `clear` and `informational` |
| priority    | Optional (default=low). Priority tells how urgent is alarm. Possible values: `high`, `medium`, `low` |
| sourceinfo  | Recommended. Source location of the alert. Eg. GE port 1/1/7 |
| summary     | Recommended. Summary information |
| instance    | Mandatory. Instance is usually already included in metric, but sometimes if alert rule doesn't return instance label, you can provide its value here. Usually IP address and port of exporter |
| nodename    | Optional. Descriptive name of instance. Eg. hostname |
| tags        | Optional. Custom tags that describe the alert (comma separated). Tags will be visible in active alerts view and are used for quick filtering. |
| team        | Optional. Team responsible for such alerts |
| eventType   | Optional. Event type (compliant with IUT-T X.733 recommendation) |
| probableCause | Optional. Probable cause (compliant with IUT-T X.733 recommendation) |
| description | Optional. Additional description. Remark: this is not a label but annotation in rules file. |

> `correlationId` is defined by: `alertname`, `sourceinfo`, `instance` and `summary`. Clear event should produce the same `correlationId`. Putting a variable value (eg. current temperature) in these fields is not recommended.

Example of alert rule in Prometheus (note the labels):

```yaml
groups:
- name: my-alerts
  rules:
  - alert: CPU usage
    expr: sum(rate(process_cpu_seconds_total[5m])) by (instance) * 100 > 80
    for: 1m
    labels:
      severity: critical
      priority: low
      sourceinfo: CPU 1
      tags: hardware, cpu, overload
      instance: '{{$labels.instance}}'
      nodename: '{{$labels.node_name}}'
      summary: CPU alert for Node '{{ $labels.node_name }}'
    annotations:
      description: Node {{ $labels.node_name }} CPU usage is at {{ humanize $value}}%.
```


#### Configure webhook receiver in Alertmanager

In order to send alerts to Alertmonitor, the receiver endpoint must be configured in `alertmanager.yml` configuration file.

```yaml
route:
  receiver: alertmonitor
  group_by: [alertname]
  group_wait: 10s
  group_interval: 5m
  repeat_interval: 3h

receivers:
- name: alertmonitor
  webhook_configs:
  - url: http://alertmonitor:8080/alertmonitor/webhook
    send_resolved: true
```

## Metrics

Alertmonitor supports the following metrics in Prometheus format:
- `alertmonitor_build_info`
- `alertmonitor_webhook_messages_received_total`
- `alertmonitor_journal_messages_total`
- `alertmonitor_active_alerts_count`
- `alertmonitor_alerts_balance_factor`
- `alertmonitor_last_event_timestamp`

Metrics are available on URI endpoint:

```
GET /alertmonitor/metrics
```

## Log files

Configure the log file location with environment variable `SIMPLELOGGER_FILENAME=/opt/alertmonitor/log/alertmonitor.log`

Rolling file policy can be also configured. For complete simple-logger configuration visit [https://github.com/matjaz99/simple-logger](https://github.com/matjaz99/simple-logger)

## For developers

Simple-logger is not available on Maven central repo. You can either build it on your own 
or download jar file from [here](http://matjazcerkvenik.si/download/simple-logger-1.6.4.jar) 
and then manually import it into your local repository:

```
wget http://matjazcerkvenik.si/download/simple-logger-1.6.4.jar

mvn install:install-file -Dfile=simple-logger-1.6.4.jar -DgroupId=si.matjazcerkvenik.simplelogger -DartifactId=simple-logger -Dversion=1.6.4 -Dpackaging=jar
```

Run the project with maven:

```
mvn tomcat7:run
```

Build docker image and push to docker hub:

```
docker build -t {{namespace}}/{{image}}:{{tag}} .
docker push {{namespace}}/{{image}}:{{tag}}
```

Inside container log files are located in directory `/opt/alertmonitor/log`
