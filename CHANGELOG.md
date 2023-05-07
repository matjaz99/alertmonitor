## 2.4.5-SNAPSHOT

* [CHANGE] Moved all configuration parameters from configuration view to providers
* [ENHANCEMENT] Added link to active alerts directly from providers.

## 2.4.4-SNAPSHOT

* [CHANGE] Removed statistics view (content moved elsewhere)
* [CHANGE] Internal report moved to providers. Report is now shown separately for each provider.
* [CHANGE] Updated primefaces version 11 to 12
* [CHANGE] Environment variable `ALERTMONITOR_PSYNC_INTERVAL_SEC` is now deprecated, instead `ALERTMONITOR_SYNC_INTERVAL_SEC` should be used.
* [FEATURE] Added environment variable `ALERTMONITOR_DATAPROVIDERS_DEFAULT_PROVIDER_NAME` to set the name of default data provider.
* [ENHANCEMENT] Http client supports basic authentication for connection to providers
* [ENHANCEMENT] Improved warning messages, added severity levels

## 2.4.3-SNAPSHOT

* [CHANGE] Wording *psync* is everywhere replaced with *sync*
* [CHANGE] Metrics now contain additional `provider` label
* [CHANGE] Metric `alertmonitor_webhook_messages_received_total` renamed to `alertmonitor_webhook_requests_received_total`
* [CHANGE] Metric `alertmonitor_journal_messages_total` renamed to `alertmonitor_journal_events_total`
* [FEATURE] Providers view with configuration
* [FEATURE] Added environment variables `ALERTMONITOR_DATAPROVIDERS_CONFIG_FILE` to set providers configuration file path 
and `ALERTMONITOR_HTTP_CLIENT_CONNECT_TIMEOUT_SEC` to configure connection establishment timeout

## 2.4.2-SNAPSHOT

* [FEATURE] Introducing Data Providers. Until now only one Prometheus was supported; now you can have many.

## 2.4.1-SNAPSHOT

* [ENHANCEMENT] GUI enhancements: Targets view improved grid
* [BUG] Bugfix (#17)

## 2.4.0 / 2022-10-29

* [FEATURE] Version check at start
* [FEATURE] Internal report, overview of the system, KPIs, line-charts...
* [ENHANCEMENT] Pool of Prometheus http clients (preferably 1 to avoid parallelism)
* [ENHANCEMENT] GUI enhancements: scroll-up button

## 2.3.0 / 2022-04-09

* [CHANGE] Webhook accepts only POST requests now
* [CHANGE] Environment variable `ALERTMONITOR_JOURNAL_SIZE` is now obsolete, and it is replaced by `ALERTMONITOR_DATA_RETENTION_DAYS`
* [FEATURE] Introducing MongoDB. Webhook messages and journal of alerts is stored in MongoDb (if enabled, otherwise in-memory storage is used)
* [FEATURE] Warning messages, in Statistics view (aka. Alertmonitor's internal alarms)
* [FEATURE] Separate alert.log only for alerts
* [ENHANCEMENT] Redesigned synchronization procedure
* [ENHANCEMENT] Added metric `alertmonitor_psync_success` which returns 1 if last psync was successful.
* [ENHANCEMENT] Added metrics for database operations: `alertmonitor_db_inserts_total`, `alertmonitor_db_queries_total`,
 `alertmonitor_db_updates_total`, `alertmonitor_db_deletes_total`, `alertmonitor_db_failures_total`
* [ENHANCEMENT] Added `age` parameter to alert to show difference between first and clear timestamp.
* [ENHANCEMENT] Added buttons for easier configuration of time intervals in query ranges.
* [ENHANCEMENT] Added hashes (#) in front of tags to emphasize their meaning
* [ENHANCEMENT] Added `ALERTMONITOR_HTTP_CLIENT_READ_TIMEOUT_SEC` environment variable to set the 
HTTP client read timeout. Also, configurable in Configuration view.
* [ENHANCEMENT] Added `prometheusId` which consists of a set of labels, that identify this prometheus server. 
Which labels denote to the ID are configurable with environment variable `ALERTMONITOR_PROMETHEUS_ID_LABELS`. 
Example: `cluster, region, monitor`.
* [ENHANCEMENT] Improved error handling with Prometheus API
* [ENHANCEMENT] A lot of refactoring
* [BUG] Fixed (#18)
* [BUG] Bugfix (#18)

## 2.2.0 / 2022-01-29

* [FEATURE] Support for Prometheus `query_range` API.
* [FEATURE] Query range requests configurable in GUI.
* [ENHANCEMENT] Extended http read timeout to 120 seconds (default was 10).

## 2.1.0 / 2022-01-21

* [CHANGE] Journal view link removed from toolbar and moved to Statistics view.
* [FEATURE] Support for Prometheus `query` API. Supported resultTypes are `vector` and `matrix`.
* [FEATURE] Query view for making PromQL queries. Results are displayed with human-readable timestamps.
* [ENHANCEMENT] Styling improvements

## 2.0.0 / 2021-12-26

* [CHANGE] There are so many improvements in this release, the version is upgraded to 2.
* [CHANGE] Licensed under Apache-2.0 License
* [CHANGE] Webhook view link removed from toolbar
* [CHANGE] Updated GUI styling due to update of Primefaces library
* [CHANGE] Environment variable `ALERTMONITOR_PSYNC_ENDPOINT`is now deprecated. Instead, 
use `ALERTMONITOR_PROMETHEUS_SERVER` and without `/api/v1/alerts`. Just schema, host and port.
* [CHANGE] Alerts with severity `indeterminate` and `informational` are now also displayed in list of active alerts
* [FEATURE] Prometheus API implementation
* [FEATURE] Retrieving targets from Prometheus
* [FEATURE] SmartTargets. The concept of SmartTargets is not new as Alertmonitor already did it in Targets view. It's just 
that Targets are now retrieved from Prometheus. And you can now switch between: instance or SmartTarget view.
* [FEATURE] Reload Prometheus config button
* [FEATURE] Load alert rules and show them in alerts
* [FEATURE] Kafka publisher will send each received alert to Kafka topic. Added environment variables for 
configuration: `ALERTMONITOR_KAFKA_ENABLED` (default=false), `ALERTMONITOR_KAFKA_SERVER`, and 
`ALERTMONITOR_KAFKA_TOPIC`. This is an experimental feature.
* [FEATURE] Configuration view for changing runtime parameters
* [ENHANCEMENT] Added metric `alertmonitor_prom_api_duration_seconds` for monitoring Prometheus API response time. 
This is a replacement for `alertmonitor_psync_task_total` which is now obsolete.
* [ENHANCEMENT] Styling improvements
* [ENHANCEMENT] Targets have colored background according to its health (status)
* [ENHANCEMENT] Search/filter also checks fields: alert name, info, instance/hostname, job, description
* [ENHANCEMENT] Targets view now supports searching targets
* [ENHANCEMENT] Updated prometheus simple client java library to 0.12.0
* [ENHANCEMENT] Updated Primefaces java library to 11.0.0
* [ENHANCEMENT] Get other labels as well, not just 'supported' ones. Eg. external_labels
* [ENHANCEMENT] Severity `indeterminate` and `informational` are shown as tags as well
* [BUG] Fixed typos

## 1.6.0 / 2021-02-28

* [CHANGE] Target view shows also targets with no active alerts (targets with at least one record in journal).
* [CHANGE] `currentValue` moved to annotations (see bug below)
* [FEATURE] Periodic synchronisation of active alerts (#16)
* [FEATURE] About view with basic configuration options
* [FEATURE] Added PsyncTask for periodically checking active alerts
* [FEATURE] Added environment variables for configuration: `ALERTMONITOR_PSYNC_INTERVAL_SEC`, `ALERTMONITOR_PSYNC_ENDPOINT`, `ALERTMONITOR_DATE_FORMAT`
* [ENHANCEMENT] Added periodic sync statistics
* [ENHANCEMENT] Added metric `alertmonitor_psync_task_total` and `alertmonitor_psync_interval_seconds`
* [ENHANCEMENT] Show number of alerts in last hour in statistics
* [ENHANCEMENT] Instance view shows active alerts and journal
* [ENHANCEMENT] Added `clearTimestamp` and `clearUid` to alerts; timestamp when alert was cleared
* [ENHANCEMENT] Added javadoc
* [ENHANCEMENT] Configure the journal size with `ALERTMONITOR_JOURNAL_SIZE` environment variable (#13)
* [BUG] Label may not contain a variable such as `currentValue`. Must be moved to `annotations`
* [BUG] Fixed stripping hostname from instance

## 1.5.0 / 2019-11-3

* [CHANGE] Added label `job` to calculate `correlationId`
* [FEATURE] Alert detailed view
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
