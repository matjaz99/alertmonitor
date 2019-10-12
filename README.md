# Alertmonitor

[![Build Status](https://semaphoreci.com/api/v1/matjaz99/alertmonitor/branches/master/shields_badge.svg)](https://semaphoreci.com/matjaz99/alertmonitor)
[![GitHub release](https://img.shields.io/github/release/matjaz99/alertmonitor.svg)](https://GitHub.com/matjaz99/alertmonitor/releases/)
[![Github all releases](https://img.shields.io/github/downloads/matjaz99/alertmonitor/total.svg)](https://GitHub.com/matjaz99/alertmonitor/releases/)
[![HitCount](http://hits.dwyl.io/matjaz99/alertmonitor.svg)](http://hits.dwyl.io/matjaz99/alertmonitor)
[![Docker Pulls](https://img.shields.io/docker/pulls/matjaz99/alertmonitor.svg)](https://hub.docker.com/r/matjaz99/alertmonitor)
[![GitHub issues](https://img.shields.io/github/issues/matjaz99/alertmonitor.svg)](https://GitHub.com/matjaz99/alertmonitor/issues/)

Alertmonitor is a webapp for displaying alerts from Prometheus. It offers a nice GUI with lots of cool features for browsing alerts.

A built-in webhook accepts any HTTP GET or POST request that comes on URI endpoint: `/alertmonitor/webhook`.

If the request is recognized to come from Prometheus Alertmanager, it will be processed further and displayed as alarm.

Alertmonitor provides three views:
- Webhook - any http get or post request that is received on webhook
- Journal - history of all events
- Active - only active alerts
- Targets - alerts sorted by targets

Alertmonitor correlates alerts firing and resolving to display active alarms (ie. alarms which haven't received clear yet).

Alerts can easily be filtered by tags.

Alertmonitor GUI is reachable on: [http://hostname:8080/alertmonitor/](http://hostname:8080/alertmonitor/)

![Alertmonitor](docs/overview.png)


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

Alertmonitor relies on properly configured labels in Prometheus alert rules .

#### Labeling alerts

Alertmonitor recognizes the following labels:

| Label       |      Description        |
|-------------|-------------------------|
| severity    | Mandatory (default=indeterminate). Severity is the weight of event. Possible values: `critical`, `major`, `minor`, `warning`, `clear` and `informational` |
| priority    | Optional (default=low). Priority tells how urgent is alarm. Possible values: `high`, `medium`, `low` |
| info        | Mandatory. Information about the alert. |
| hostname    | Mandatory. Instance is usually included in metric, but sometimes if alert rule doesn't return hostname, you can provide its value here by any other means. Usually IP address and port of exporter. |
| nodename    | Optional. Descriptive name of hostname. Eg. hostname |
| currentValue | Optional. Current metric value. Get it with: `{{ humanize $value }}`. Optionally you can append units (eg. % or MB).
| tags        | Optional. Custom tags that describe the alert (comma separated). Tags are used for quick filtering in Alertmonitor. |
| team        | Optional. Team responsible for this kind of alerts. |
| url        | Optional. Custom URL that is related to alert. |
| eventType   | Optional. Event type compliant with IUT-T X.733 recommendation |
| probableCause | Optional. Probable cause compliant with IUT-T X.733 recommendation |
| description | Optional. Additional description. Value is read from a label if exists, otherwise from annotation. |

> `correlationId` is defined by: `alertname`, `info`, `hostname` and `job`. Clear event should produce the same `correlationId`.

Example of alert rule in Prometheus (note the labels):

```yaml
groups:
- name: my-alerts
  rules:
  - alert: CPU usage
    expr: sum(rate(process_cpu_seconds_total[5m])) by (hostname) * 100 > 80
    for: 1m
    labels:
      # mandatory labels
      hostname: '{{$labels.hostname}}'
      severity: critical
      priority: low
      info: CPU alert for Node '{{ $labels.node_name }}'
      # optional labels
      currentValue: '{{ humanize $value }}'
      nodename: '{{$labels.node_name}}'
      tags: hardware, cpu, overload
      team: Team1
      url: 'http://${GRAFANA_HOSTNAME}/dashboard/'
      eventType: 5
      probableCause: 1024
      description: Node {{ $labels.node_name }} CPU usage is at {{ humanize $value}}%.
    annotations:
      description: Node {{ $labels.node_name }} CPU usage is at {{ humanize $value}}%.
      summary: CPU alert for Node '{{ $labels.node_name }}'
```

> For other integrations you might still need `description` and `summary` in annotations. Alertmonitor reads them from labels.


#### Configure webhook receiver in Alertmanager

In order to send alerts to Alertmonitor, configure a receiver endpoint in `alertmanager.yml` configuration file.

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

## Target view

Alertmonitor strips protocol and port from hostname label and what remains is target's hostname or IP address or FQDN.

Alertmonitor then filters alerts and displays those who's hostnames match.

## Environment variable substitution

Prometheus doesn't support substitution of environment variables in alert rules. Alertmonitor does that for you.

Environment variables can be set on system level or directly on docker containers. Example in docker-compose file.

```yaml
    environment:
      - GRAFANA_HOSTNAME: my.grafana.domain
```

Template syntax in labels to be replaced: `${GRAFANA_HOSTNAME}`.

Alertmonitor will replace all occurrences of templates with corresponding environment variables.

You can use environment variable substitution on the following labels:
- nodename
- info
- tags
- url
- description

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
wget http://matjazcerkvenik.si/download/simple-logger-1.7.0.jar

mvn install:install-file -Dfile=simple-logger-1.7.0.jar -DgroupId=si.matjazcerkvenik.simplelogger -DartifactId=simple-logger -Dversion=1.7.0 -Dpackaging=jar
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
