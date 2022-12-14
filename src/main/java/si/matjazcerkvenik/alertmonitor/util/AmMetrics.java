/*
   Copyright 2021 Matjaž Cerkvenik

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package si.matjazcerkvenik.alertmonitor.util;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

public class AmMetrics {

    public static CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    public static final Gauge alertmonitor_build_info = Gauge.build()
            .name("alertmonitor_build_info")
            .help("Alertmonitor meta data, value contains start time")
            .labelNames("app", "runtimeId", "version", "os")
            .register();

    public static final Gauge alertmonitor_providers_info = Gauge.build()
            .name("alertmonitor_providers_info")
            .help("Configured data providers")
            .labelNames("provider", "type", "uri")
            .register();

    public static final Counter alertmonitor_webhook_requests_received_total = Counter.build()
            .name("alertmonitor_webhook_requests_received_total")
            .help("Total number of received webhook requests from remotehost.")
            .labelNames("provider", "remotehost", "method")
            .register();

    public static final Counter alertmonitor_journal_events_total = Counter.build()
            .name("alertmonitor_journal_events_total")
            .help("Total number of events in journal.")
            .labelNames("provider", "severity")
            .register();

    public static final Gauge alertmonitor_active_alerts_count = Gauge.build()
            .name("alertmonitor_active_alerts_count")
            .help("Currently active alerts by severity and alertname")
            .labelNames("provider", "alertname", "severity")
            .register();

    public static final Gauge alertmonitor_alerts_balance_factor = Gauge.build()
            .name("alertmonitor_alerts_balance_factor")
            .help("Balance factor of active alerts")
            .labelNames("provider")
            .register();

    public static final Gauge alertmonitor_last_event_timestamp = Gauge.build()
            .name("alertmonitor_last_event_timestamp")
            .help("Timestamp of last event")
            .labelNames("provider")
            .register();

    public static final Histogram alertmonitor_prom_api_duration_seconds = Histogram.build()
            .buckets(0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1.0)
            .name("alertmonitor_prom_api_duration_seconds")
            .labelNames("provider", "method", "code", "endpoint")
            .help("Prometheus HTTP API response time")
            .register();

    /** Return 0 or 1 weather synchronization was successful (1) or failed (0) */
    public static final Gauge alertmonitor_sync_success = Gauge.build()
            .name("alertmonitor_sync_success")
            .help("Synchronization success flag.")
            .labelNames("provider")
            .register();

    public static final Gauge alertmonitor_sync_interval_seconds = Gauge.build()
            .name("alertmonitor_sync_interval_seconds")
            .help("Synchronization interval.")
            .labelNames("provider")
            .register();

    public static final Counter alertmonitor_db_inserts_total = Counter.build()
            .name("alertmonitor_db_inserts_total")
            .help("Total number of insert operations into DB.")
            .labelNames("catalog")
            .register();

    public static final Counter alertmonitor_db_queries_total = Counter.build()
            .name("alertmonitor_db_queries_total")
            .help("Total number of query operations into DB.")
            .labelNames("catalog")
            .register();

    public static final Counter alertmonitor_db_updates_total = Counter.build()
            .name("alertmonitor_db_updates_total")
            .help("Total number of update operations into DB.")
            .labelNames("catalog")
            .register();

    public static final Counter alertmonitor_db_deletes_total = Counter.build()
            .name("alertmonitor_db_deletes_total")
            .help("Total number of delete operations into DB.")
            .labelNames("catalog")
            .register();

    public static final Counter alertmonitor_db_failures_total = Counter.build()
            .name("alertmonitor_db_failures_total")
            .help("Total number of failed operations with DB.")
            .register();

}
