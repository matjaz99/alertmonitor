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

    // internal counters
    public static long webhookMessagesReceivedCount = 0;
    public static long amMessagesReceivedCount = 0;
    public static long journalReceivedCount = 0;
    // TODO remove these two, it can be calculated from number of alerts by severity
    public static long raisingEventCount = 0;
    public static long clearingEventCount = 0;
    public static long lastEventTimestamp = 0;
    public static long lastPsyncTimestamp = 0;
    public static int psyncSuccessCount = 0;
    public static int psyncFailedCount = 0;

    public static CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    public static final Gauge alertmonitor_build_info = Gauge.build()
            .name("alertmonitor_build_info")
            .help("Alertmonitor meta data, value contains start time")
            .labelNames("app", "runtimeId", "version", "os")
            .register();

    public static final Counter alertmonitor_webhook_messages_received_total = Counter.build()
            .name("alertmonitor_webhook_messages_received_total")
            .help("Total number of received webhook messages.")
            .labelNames("remotehost", "method")
            .register();

    public static final Counter alertmonitor_journal_messages_total = Counter.build()
            .name("alertmonitor_journal_messages_total")
            .help("Total number of messages in journal.")
            .labelNames("severity")
            .register();

    public static final Gauge alertmonitor_active_alerts_count = Gauge.build()
            .name("alertmonitor_active_alerts_count")
            .help("Currently active alerts by severity and alertname")
            .labelNames("alertname", "severity")
            .register();

    public static final Gauge alertmonitor_alerts_balance_factor = Gauge.build()
            .name("alertmonitor_alerts_balance_factor")
            .help("Balance factor of active alerts")
            .register();

    public static final Gauge alertmonitor_last_event_timestamp = Gauge.build()
            .name("alertmonitor_last_event_timestamp")
            .help("Timestamp of last event")
            .register();

    public static final Histogram alertmonitor_prom_api_duration_seconds = Histogram.build()
            .buckets(0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1.0)
            .name("alertmonitor_prom_api_duration_seconds")
            .labelNames("method", "code", "endpoint")
            .help("Prometheus HTTP API response time")
            .register();

    public static final Gauge alertmonitor_psync_interval_seconds = Gauge.build()
            .name("alertmonitor_psync_interval_seconds")
            .help("Periodic synchronization interval.")
            .register();

}
