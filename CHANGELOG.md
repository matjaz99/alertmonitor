## 1.3.1-SNAPSHOT

* [ENHANCEMENT] Added security constraints for restricted access to some directories (web.xml)
* [ENHANCEMENT] URLs without .xhtml extension

## 1.3.0 / 2019-09-27

* [FEATURE] Introducing tags (#6)
* [FEATURE] Custom JSF component for rendering tags
* [FEATURE] Filtering active alerts by tags
* [FEATURE] Added simple-logger for logging (#9)
* [ENHANCEMENT] Upgrade of Grafana dashboard (v2)
* [ENHANCEMENT] Install curl in container
* [ENHANCEMENT] Added healthcheck to docker-compose file (by using curl)
* [ENHANCEMENT] Added local IP address in footer

## 1.2.0 / 2019-08-28

* [FEATURE] Added startUpTime (OnStart listener)
* [FEATURE] Metrics endpoint for prometheus (#5)
* [FEATURE] Supported metrics: `alertmonitor_build_info`, `alertmonitor_webhook_messages_received_total`, `alertmonitor_journal_messages_total`, `alertmonitor_active_alerts_count`, `alertmonitor_alerts_balance_factor`, `alertmonitor_last_event_timestamp`
* [FEATURE] Show version in GUI (in footer)
* [ENHANCEMENT] Added example Grafana dashboard (see docs/grafana_dashboards)
* [ENHANCEMENT] A lot of refactoring
* [BUGFIX] fixed calculation of up time

## 1.1.0 / 2019-07-15

Release includes:

* [FEATURE] Pagination (#1)
* [FEATURE] Sorting by multiple columns (hold command key) (#2)
* [ENHANCEMENT] Changed base image from tomcat:8.5.37-jre8 to tomcat:8.5-jre8-alpine (because it's smaller)
* [ENHANCEMENT] Updated Primefaces from 6.1 to 6.2
* [ENHANCEMENT] Added support for labels: `job`, `tags`, `sourceinfo`; `alertdomain` label is now deprecated
* [BUGFIX] Bug fixes and refactoring

## 1.0.0 / 2019-06-30

This is first release. Release includes:

* [FEATURE] Correlate alarm and clear pairs (selected alerts)
* [FEATURE] Color alerts according to severity
* [FEATURE] GUI showing selected alerts, journal (history) and requests in raw format
* [FEATURE] Recognize alertmanager alerts and process alerts in json format
* [FEATURE] Generic webhook - it accepts GET and POST requests in raw format (http headers, url parameters, body)
* [FEATURE] The application is written in Java and runs in Apache Tomcat with JSF 2.2 and Primefaces
