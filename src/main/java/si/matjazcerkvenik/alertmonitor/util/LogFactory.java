package si.matjazcerkvenik.alertmonitor.util;

import si.matjazcerkvenik.simplelogger.SimpleLogger;

public class LogFactory {

    private static SimpleLogger logger = null;
    private static SimpleLogger alertLog = null;

    public static SimpleLogger getLogger() {
        if (logger == null) {
            logger = new SimpleLogger();
            if (logger.getFilename().contains("simple-logger.log")) {
                logger.setFilename("./alertmonitor.log");
            }
        }
        return logger;
    }

    public static SimpleLogger getAlertLog() {
        if (alertLog == null) {
            alertLog = new SimpleLogger("./alerts.log");
        }
        return alertLog;
    }


}
