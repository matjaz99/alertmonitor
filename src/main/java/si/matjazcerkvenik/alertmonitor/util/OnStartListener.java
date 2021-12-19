package si.matjazcerkvenik.alertmonitor.util;

import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.PSyncTask;
import si.matjazcerkvenik.alertmonitor.model.TaskManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Timer;
import java.util.stream.Stream;

public class OnStartListener implements ServletContextListener {

    private Timer pSyncTimer = null;
    private PSyncTask pSyncTask = null;


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        DAO.startUpTime = System.currentTimeMillis();

        // read version file
        InputStream inputStream = servletContextEvent.getServletContext().getResourceAsStream("/WEB-INF/version.txt");
        try {
            DataInputStream dis = new DataInputStream(inputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            DAO.version = br.readLine();
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DAO.getLogger().info("\n");
        DAO.getLogger().info("************************************************");
        DAO.getLogger().info("*                                              *");
        DAO.getLogger().info("*             Alertmonitor started             *");
        DAO.getLogger().info("*                                              *");
        DAO.getLogger().info("************************************************");
        DAO.getLogger().info("");
        DAO.getLogger().info("ALERTMONITOR_VERSION=" + DAO.version);
        DAO.getLogger().info("ALERTMONITOR_IPADDR=" + DAO.getInstance().getLocalIpAddress());

        // read all environment variables
        Map<String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
            DAO.getLogger().info(entry.getKey() + "=" + entry.getValue());
        }

        // read configuration from environment variables
        DAO.JOURNAL_TABLE_SIZE = Integer.parseInt(System.getenv().getOrDefault("ALERTMONITOR_JOURNAL_SIZE", "20000").trim());
        DAO.ALERTMONITOR_PSYNC_INTERVAL_SEC = Integer.parseInt(System.getenv().getOrDefault("ALERTMONITOR_PSYNC_INTERVAL_SEC", "60").trim());
        DAO.ALERTMONITOR_PROMETHEUS_SERVER = System.getenv().getOrDefault("ALERTMONITOR_PSYNC_ENDPOINT", "http://centosvm:9090").trim(); // To be deleted
        if (DAO.ALERTMONITOR_PROMETHEUS_SERVER.endsWith("/")) DAO.ALERTMONITOR_PROMETHEUS_SERVER = DAO.ALERTMONITOR_PROMETHEUS_SERVER.substring(0, DAO.ALERTMONITOR_PROMETHEUS_SERVER.length()-1);
        DAO.ALERTMONITOR_PROMETHEUS_SERVER = System.getenv().getOrDefault("ALERTMONITOR_PROMETHEUS_SERVER", "http://localhost:9090").trim();
        if (DAO.ALERTMONITOR_PROMETHEUS_SERVER.endsWith("/")) DAO.ALERTMONITOR_PROMETHEUS_SERVER = DAO.ALERTMONITOR_PROMETHEUS_SERVER.substring(0, DAO.ALERTMONITOR_PROMETHEUS_SERVER.length()-1);
        DAO.DATE_FORMAT = System.getenv().getOrDefault("ALERTMONITOR_DATE_FORMAT", "yyyy/MM/dd H:mm:ss").trim();
        DAO.ALERTMONITOR_KAFKA_ENABLED = Boolean.parseBoolean(System.getenv().getOrDefault("ALERTMONITOR_KAFKA_ENABLED", "false").trim());
        DAO.ALERTMONITOR_KAFKA_SERVER = System.getenv().getOrDefault("ALERTMONITOR_KAFKA_SERVER", "localhost:9092").trim();
        DAO.ALERTMONITOR_KAFKA_TOPIC = System.getenv().getOrDefault("ALERTMONITOR_KAFKA_TOPIC", "alertmonitor_notifications").trim();

        // runtime memory info
        int mb = 1024 * 1024;
        Runtime instance = Runtime.getRuntime();
        DAO.getLogger().info("***** Heap utilization statistics [MB] *****");
        DAO.getLogger().info("Total Memory: " + instance.totalMemory() / mb); // available memory
        DAO.getLogger().info("Free Memory: " + instance.freeMemory() / mb); // free memory
        DAO.getLogger().info("Used Memory: "
                + (instance.totalMemory() - instance.freeMemory()) / mb); // used memory
        DAO.getLogger().info("Max Memory: " + instance.maxMemory() / mb); // Maximum available memory

        // is running inside docker
        try (Stream< String > stream =
                 Files.lines(Paths.get("/proc/1/cgroup"))) {
            DAO.getLogger().info("Running in container: " + stream.anyMatch(line -> line.contains("/docker")));
            DAO.isContainerized = true;
        } catch (IOException e) {
            DAO.getLogger().info("Running in container: false");
            DAO.isContainerized = false;
        }

        AmMetrics.alertmonitor_build_info.labels("Alertmonitor", DAO.version, System.getProperty("os.name")).set(DAO.startUpTime);

        // start resync timer
        TaskManager.getInstance().restartPsyncTimer();

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        TaskManager.getInstance().stopPsyncTimer();

        DAO.getLogger().info("#");
        DAO.getLogger().info("# Stopping Alertmonitor");
        DAO.getLogger().info("#\n\n\n");
        DAO.getLogger().close();
    }


}
