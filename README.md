# Alertmonitor

[![Build Status](https://semaphoreci.com/api/v1/matjaz99/alertmonitor/branches/master/shields_badge.svg)](https://semaphoreci.com/matjaz99/alertmonitor)
[![GitHub release](https://img.shields.io/github/release/matjaz99/alertmonitor.svg)](https://GitHub.com/matjaz99/alertmonitor/releases/)
[![Github all releases](https://img.shields.io/github/downloads/matjaz99/alertmonitor/total.svg)](https://GitHub.com/matjaz99/alertmonitor/releases/)
[![HitCount](http://hits.dwyl.io/matjaz99/alertmonitor.svg)](http://hits.dwyl.io/matjaz99/alertmonitor)
[![Docker Pulls](https://img.shields.io/docker/pulls/matjaz99/alertmonitor.svg)](https://hub.docker.com/r/matjaz99/alertmonitor)
[![GitHub issues](https://img.shields.io/github/issues/matjaz99/alertmonitor.svg)](https://GitHub.com/matjaz99/alertmonitor/issues/)

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
docker run -d -p 8080:8080 matjaz99/alertmonitor:latest
```

There is also `docker-compose.yml` file for deployment in Swarm cluster.

## Docker images

Docker images are available on Docker hub: [https://hub.docker.com/r/matjaz99/alertmonitor](https://hub.docker.com/r/matjaz99/alertmonitor)

## Configuration

The Alertmonitor application itself doesn't support any configuration options.
But for correlation to work, the alerts in Prometheus rules should have properly configured labels.

#### Labeling alerts

Enrich alert rules with additional labels.

Alertmonitor recognizes the following labels:

| Label       |      Description        |
|-------------|-------------------------|
| severity    | Severity is the weight of event. Possible values: critical, major, minor, warning, clear and informational |
| priority    | Priority tells how urgent is alarm. Possible values: high, medium, low |
| sourceinfo  | Exact location of the alert. Eg. GE port 1/1/7 |
| instance    | Instance is usually already included in metric, but sometimes if alert rule doesn't return instance label, you can provide its value here. Usually IP address and port of exporter |
| nodename    | Descriptive name of instance. Eg. hostname |
| tags        | Custom tags that describe the alert (comma separated)

> `correlationId` is defined by: `alertname`, `sourceinfo`, `instance` and `summary`. Clear event should produce the same `correlationId` to correlate it with alarm. Putting a variable value (eg. current temperature) in these fields is not recommended.

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
      sourceinfo: CPU 1
      tags: hardware, cpu, overload
      instance: '{{$labels.instance}}'
      nodename: '{{$labels.node_name}}'
    annotations:
      description: Node {{ $labels.node_name }} CPU usage is at {{ humanize $value}}%.
      summary: CPU alert for Node '{{ $labels.node_name }}'
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

## For developers

Run the project with maven:

```
mvn tomcat7:run
```

Build docker image and push to docker hub:

```
docker build -t {{namespace}}/{{image}}:{{tag}} .
docker push {{namespace}}/{{image}}:{{tag}}
```

