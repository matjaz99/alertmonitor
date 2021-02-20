## 1.5.4-SNAPSHOT

* [FEATURE] Resynchronize alerts
* [ENHANCEMENT] Added resync statistics

## 1.5.3-SNAPSHOT

* [FEATURE] Added ResyncTask for periodically checking active alerts
* [FEATURE] Added environment variables for configuration: `ALERTMONITOR_RESYNC_INTERVAL_SEC`, `ALERTMONITOR_RESYNC_ENDPOINT`, `ALERTMONITOR_DATE_FORMAT`
* [ENHANCEMENT] Added metric `alertmonitor_resync_task_total` and `alertmonitor_resync_interval_seconds`

## 1.5.2-SNAPSHOT

* [BUG] Strip hostname from instance

## 1.5.1-SNAPSHOT

* [CHANGE] Target view shows also targets with no active alerts (targets with at least one record in journal).
* [CHANGE] `currentValue` moved to annotations (see bug below)
* [ENHANCEMENT] Show number of alerts in last hour in statistics
* [ENHANCEMENT] Instance view shows active alerts and journal
* [ENHANCEMENT] Added `clearTimestamp` and `clearUid` to alerts; timestamp when alert was cleared
* [ENHANCEMENT] Added some javadoc
* [ENHANCEMENT] Configure the journal size with `ALERTMONITOR_JOURNAL_SIZE` environment variable (#13)
* [BUG] Label may not contain a variable such as `currentValue`. Must be moved to `annotations`

## 1.5.0 / 2019-11-3

* [CHANGE] Added label `job` to calculate `correlationId`
* [FEATURE] Alert view
* [FEATURE] Growl notifications
* [ENHANCEMENT] Resolving instances to IP addresses
* [BUGFIX] Fixed timestamps in active alerts
* [BUGFIX] Fixed updating active alerts

## 1.4.0 / 2019-10-12

* [CHANGE] Renamed label `sourceInfo` to `info`
* [CHANGE] Annotation `summary` is ignored, all data should go to label `info`
* [CHANGE] `description` is read from a label if exists, otherwise from annotation
* [FEATURE] Target view, strip instance down to hostname (#10)
* [FEATURE] Show active alerts by targets
* [FEATURE] Environment variable substitution (#11)
* [FEATURE] Added small icon representing highest severity on targets
* [ENHANCEMENT] Added security constraints for restricted access to some directories (web.xml)
* [ENHANCEMENT] URLs without .xhtml extension
* [ENHANCEMENT] Added timestamp of first occurrence of alert
* [ENHANCEMENT] Update timestamp of last occurrence of alert
* [ENHANCEMENT] Supported new labels `team`, `currentValue`, `url`, `eventType`, `probableCause`
* [BUGFIX] Fixed exception when removing obsolete tags if there are no more active alerts

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
* [BUGFIX] Fixed calculation of up time

## 1.1.0 / 2019-07-15

Release includes:

* [FEATURE] Pagination (#1)
* [FEATURE] Sorting by multiple columns (hold command key) (#2)
* [ENHANCEMENT] Changed base image from tomcat:8.5.37-jre8 to tomcat:8.5-jre8-alpine (because it's smaller)
* [ENHANCEMENT] Updated Primefaces from 6.1 to 6.2
* [ENHANCEMENT] Added support for labels: `job`, `tags`, `info`; `alertdomain` label is now deprecated
* [BUGFIX] Bug fixes and refactoring

## 1.0.0 / 2019-06-30

This is first release. Release includes:

* [FEATURE] Correlate alarm and clear pairs (selected alerts)
* [FEATURE] Color alerts according to severity
* [FEATURE] GUI showing selected alerts, journal (history) and requests in raw format
* [FEATURE] Recognize alertmanager alerts and process alerts in json format
* [FEATURE] Generic webhook - it accepts GET and POST requests in raw format (http headers, url parameters, body)
* [FEATURE] The application is written in Java and runs in Apache Tomcat with JSF 2.2 and Primefaces
