package si.matjazcerkvenik.alertmonitor.util;

import si.matjazcerkvenik.simplelogger.SimpleLogger;

public class LogFactory {

    private static SimpleLogger logger = null;
    private static SimpleLogger alertLog = null;

    {
        getLogger(); // force calling getLogger just to initialize devEnv.
    }

    public static SimpleLogger getLogger() {
        if (logger == null) {
            logger = new SimpleLogger();
            if (logger.getFilename().contains("simple-logger.log")) {
                // if env variable would be set, logger will be already configured
                // so if it was not set, I will assume this is a development environment
                // and write logs to file in working directory
                AmProps.devEnv = true;
                logger.setFilename("./alertmonitor.log");
            }
        }
        return logger;
    }

    public static SimpleLogger getAlertLog() {
        if (alertLog == null) {
            if (AmProps.devEnv) {
                // write file in local working directory
                alertLog = new SimpleLogger("./alerts.log");
            } else {
                // in production environment (aka when running inside container)
                alertLog = new SimpleLogger("/opt/alertmonitor/log/alerts.log");
            }
            alertLog.setVerbose(false);
        }
        return alertLog;
    }


}
