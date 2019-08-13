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
            .help("Number of received webhook messages.")
            .labelNames("remoteHost", "method")
            .register();

    public static final Counter alertmonitor_journal_messages_total = Counter.build()
            .name("alertmonitor_journal_messages_total")
            .help("Number of messages in journal.")
            .register();

    public static final Gauge alertmonitor_active_alerts_count = Gauge.build()
            .name("alertmonitor_active_alerts_count")
            .help("Number of active alerts by severity")
            .labelNames("severity")
            .register();

    public static final Counter alertmonitor_alerts_total = Counter.build()
            .name("alertmonitor_alerts_total")
            .help("Number of alerts raised or cleared.")
            .labelNames("state")
            .register();

    public static final Gauge alertmonitor_alerts_balance_factor = Gauge.build()
            .name("alertmonitor_alerts_balance_factor")
            .help("Balance factor of active alerts")
            .register();

    public static final Gauge alertmonitor_last_event_timestamp = Gauge.build()
            .name("alertmonitor_last_event_timestamp")
            .help("Timestamp of last event (raise, clear or update")
            .register();

}
