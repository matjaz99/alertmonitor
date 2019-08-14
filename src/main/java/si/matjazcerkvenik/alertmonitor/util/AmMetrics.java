package si.matjazcerkvenik.alertmonitor.util;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class AmMetrics {

    public static CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    public static final Gauge alertmonitor_build_info = Gauge.build()
            .name("alertmonitor_build_info")
            .help("Alertmonitor version, value is start time")
            .labelNames("app", "version", "os")
            .register();

    public static final Counter alertmonitor_webhook_messages_received_total = Counter.build()
            .name("alertmonitor_webhook_messages_received_total")
            .help("Total number of received webhook messages.")
            .labelNames("remoteHost", "method")
            .register();

    public static final Counter alertmonitor_journal_messages_total = Counter.build()
            .name("alertmonitor_journal_messages_total")
            .help("Total number of messages in journal.")
            .labelNames("severity")
            .register();

    public static final Gauge alertmonitor_active_alerts_count = Gauge.build()
            .name("alertmonitor_active_alerts_count")
            .help("Number of active alerts by severity")
            .labelNames("alertname", "severity")
            .register();

    public static final Gauge alertmonitor_alerts_balance_factor = Gauge.build()
            .name("alertmonitor_alerts_balance_factor")
            .help("Balance factor of active alerts")
            .register();

    public static final Gauge alertmonitor_last_event_timestamp = Gauge.build()
            .name("alertmonitor_last_event_timestamp")
            .help("Timestamp of last event (raise, clear or update)")
            .register();

}
