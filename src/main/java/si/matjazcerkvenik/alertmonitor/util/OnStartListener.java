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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class OnStartListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        AmProps.startUpTime = System.currentTimeMillis();

        // read version file
        InputStream inputStream = servletContextEvent.getServletContext().getResourceAsStream("/WEB-INF/version.txt");
        try {
            DataInputStream dis = new DataInputStream(inputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            AmProps.version = br.readLine().trim();
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            AmProps.localIpAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            AmProps.localIpAddress = "UnknownHost";
        }

        LogFactory.getLogger().info("\n");
        LogFactory.getLogger().info("************************************************");
        LogFactory.getLogger().info("*                                              *");
        LogFactory.getLogger().info("*             Alertmonitor started             *");
        LogFactory.getLogger().info("*                                              *");
        LogFactory.getLogger().info("************************************************");
        LogFactory.getLogger().info("");
        LogFactory.getLogger().info("ALERTMONITOR_VERSION=" + AmProps.version);
        LogFactory.getLogger().info("ALERTMONITOR_IPADDR=" + AmProps.localIpAddress);

        AmProps.ALERTMONITOR_RUNTIME_ID = UUID.randomUUID().toString();
        LogFactory.getLogger().info("RUNTIME_ID=" + AmProps.ALERTMONITOR_RUNTIME_ID);

        // Don't call DAO before reading env vars!!!

        // read all environment variables
        LogFactory.getLogger().info("***** Environment variables *****");
        Map<String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
            LogFactory.getLogger().info(entry.getKey() + "=" + entry.getValue());
        }

        // read configuration from environment variables
        AmProps.ALERTMONITOR_DATA_RETENTION_DAYS = Integer.parseInt(System.getenv().getOrDefault("ALERTMONITOR_DATA_RETENTION_DAYS", "30").trim());
        AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC = Integer.parseInt(System.getenv().getOrDefault("ALERTMONITOR_PSYNC_INTERVAL_SEC", "60").trim());
        if (AmProps.ALERTMONITOR_PROMETHEUS_SERVER.endsWith("/")) AmProps.ALERTMONITOR_PROMETHEUS_SERVER = AmProps.ALERTMONITOR_PROMETHEUS_SERVER.substring(0, AmProps.ALERTMONITOR_PROMETHEUS_SERVER.length()-1);
        AmProps.ALERTMONITOR_PROMETHEUS_SERVER = System.getenv().getOrDefault("ALERTMONITOR_PROMETHEUS_SERVER", "http://localhost:9090").trim();
        if (AmProps.ALERTMONITOR_PROMETHEUS_SERVER.endsWith("/")) AmProps.ALERTMONITOR_PROMETHEUS_SERVER = AmProps.ALERTMONITOR_PROMETHEUS_SERVER.substring(0, AmProps.ALERTMONITOR_PROMETHEUS_SERVER.length()-1);
        if (!AmProps.ALERTMONITOR_PROMETHEUS_SERVER.startsWith("http")) AmProps.ALERTMONITOR_PROMETHEUS_SERVER = "http://" + AmProps.ALERTMONITOR_PROMETHEUS_SERVER;
        AmProps.ALERTMONITOR_HTTP_CLIENT_READ_TIMEOUT_SEC = Integer.parseInt(System.getenv().getOrDefault("ALERTMONITOR_HTTP_CLIENT_READ_TIMEOUT_SEC", "120").trim());
        AmProps.ALERTMONITOR_DATE_FORMAT = System.getenv().getOrDefault("ALERTMONITOR_DATE_FORMAT", "yyyy/MM/dd H:mm:ss").trim();
        AmProps.ALERTMONITOR_KAFKA_ENABLED = Boolean.parseBoolean(System.getenv().getOrDefault("ALERTMONITOR_KAFKA_ENABLED", "false").trim());
        AmProps.ALERTMONITOR_KAFKA_SERVER = System.getenv().getOrDefault("ALERTMONITOR_KAFKA_SERVER", "localhost:9092").trim();
        AmProps.ALERTMONITOR_KAFKA_TOPIC = System.getenv().getOrDefault("ALERTMONITOR_KAFKA_TOPIC", "alertmonitor_events").trim();
        AmProps.ALERTMONITOR_PROMETHEUS_ID_LABELS = System.getenv().getOrDefault("ALERTMONITOR_PROMETHEUS_ID_LABELS", "cluster, region, monitor").trim();
        AmProps.ALERTMONITOR_MONGODB_ENABLED = Boolean.parseBoolean(System.getenv().getOrDefault("ALERTMONITOR_MONGODB_ENABLED", "true").trim());
//        AmProps.ALERTMONITOR_MONGODB_CONNECTION_STRING = System.getenv().getOrDefault("ALERTMONITOR_MONGODB_CONNECTION_STRING", "mongodb://admin:mongodbpassword@promvm:27017/test?w=majority&authSource=admin").trim();
        AmProps.ALERTMONITOR_MONGODB_CONNECTION_STRING = System.getenv().getOrDefault("ALERTMONITOR_MONGODB_CONNECTION_STRING", "mongodb://admin:mongodbpassword@promvm:27017/?authSource=admin").trim();

        // runtime memory info
        int mb = 1024 * 1024;
        Runtime instance = Runtime.getRuntime();
        LogFactory.getLogger().info("***** Heap utilization statistics [MB] *****");
        LogFactory.getLogger().info("Total Memory: " + instance.totalMemory() / mb); // available memory
        LogFactory.getLogger().info("Free Memory: " + instance.freeMemory() / mb); // free memory
        LogFactory.getLogger().info("Used Memory: "
                + (instance.totalMemory() - instance.freeMemory()) / mb); // used memory
        LogFactory.getLogger().info("Max Memory: " + instance.maxMemory() / mb); // Maximum available memory

        // is running inside docker
        try (Stream< String > stream =
                 Files.lines(Paths.get("/proc/1/cgroup"))) {
            LogFactory.getLogger().info("Running in container: " + stream.anyMatch(line -> line.contains("/docker")));
            AmProps.isContainerized = true;
        } catch (IOException e) {
            LogFactory.getLogger().info("Running in container: false");
            AmProps.isContainerized = false;
        }

        AmMetrics.alertmonitor_build_info.labels("Alertmonitor", AmProps.ALERTMONITOR_RUNTIME_ID, AmProps.version, System.getProperty("os.name")).set(AmProps.startUpTime);

        // start periodic sync timer
        TaskManager.getInstance().restartPsyncTimer();

        AmProps.githubVersion = TaskManager.getInstance().getVersionFromGithub();

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        TaskManager.getInstance().stopPsyncTimer();

        LogFactory.getLogger().info("#");
        LogFactory.getLogger().info("# Stopping Alertmonitor");
        LogFactory.getLogger().info("#\n\n\n");
        LogFactory.getLogger().close();
    }


}
