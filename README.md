# Alertmonitor

[![Build Status](https://semaphoreci.com/api/v1/matjaz99/alertmonitor/branches/master/shields_badge.svg)](https://semaphoreci.com/matjaz99/alertmonitor)

Alertmonitor is a GUI for displaying alerts from Prometheus Alertmanager.

A generic webhook in Alertmonitor accepts any HTTP GET or POST request that comes on URI endpoint: `/alertmonitor/webhook`.

If the request is recognized to come from Alertmanager, it will be processed and displayed as alarm.

Alertmonitor provides three views:
- Raw - anything that is received on webhook
- Journal - history of all alerts
- Active - only active alerts

Alertmonitor correlates alarms and clears to display only active alarms (ie. alarms which haven't received clear yet).

Alertmonitor GUI is reachable on: http://hostname:7070/alertmonitor/

> Alertmonitor does not support any persistence. Alerts are stored in memory. After restart alerts are gone.

## Install

The easiest way to start using Alertmonitor is to deploy it on Docker.

Deploy container:

```
docker run -d -p 7070:8080 matjaz99/alertmonitor:1.0.0
```

There is also `docker-compose.yml` file for deployment in Swarm cluster.

> Remark: The port mapping 7070:8080 means that alertmonitor is available from outside on port 7070, but other services in Swarm cluster can communicate with alertmonitor on port 8080. Anyway you can change this to whatever you like.

## Docker images

Docker images are available on Docker hub: [https://hub.docker.com/r/matjaz99/alertmonitor](https://hub.docker.com/r/matjaz99/alertmonitor)

## Configuration

The Alertmonitor application itself doesn't support any configuration options.
But for correlation to work, the alerts in Prometheus rules should have properly configured labels.

#### Labeling alerts

Enrich alert rules with additional labels.

Alertmonitor supports the following labels:

| Label       |      Description        |
|-------------|-------------------------|
| severity    | Severity is the weight of event. Possible values: critical, major, minor, warning, clear and informational |
| priority    | Priority tells how urgent is alarm. Possible values: high, medium, low |
| alertdomain | Domain which covers the alert or context of alert. Eg. hardware or network |
| instance    | Instance which fired the alert by means of Prometheus. Usually IP address and port of exporter |
| nodename    | Descriptive name of instance. Eg. hostname |

Example of alert rule in Prometheus (note the labels):

```yaml
groups:
- name: my-alerts
  rules:
  - alert: CPU usage
    expr: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1m]) * ON(instance) GROUP_LEFT(node_name) node_meta * 100) BY (node_name)) > 80
    for: 1m
    labels:
      severity: critical
      priority: low
      alertdomain: cpu
      instance: '{{$labels.instance}}'
      nodename: '{{$labels.node_name}}'
    annotations:
      description: Node {{ $labels.node_name }} CPU usage is at {{ humanize $value}}%.
      summary: CPU alert for Node '{{ $labels.node_name }}'
```


> Never put a metric value in labels. Why? Alerts are also stored in Prometheus as time-series data and using value in label will generate a new time-series for each alert. Consequently `for` condition will never be met, and the alert will never be sent.

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

## For developers

Run the project with maven:

```
mvn tomcat7:run
```

Manually build docker image and push to docker hub:

```
docker build -t matjaz99/alertmonitor:1.0.0 .
docker push matjaz99/alertmonitor:1.0.0
```

