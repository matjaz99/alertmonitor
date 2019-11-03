package si.matjazcerkvenik.alertmonitor.util;

import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.simplelogger.LEVEL;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

public class OnStartListener implements ServletContextListener {


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
        DAO.getLogger().info("*********************************************");
        DAO.getLogger().info("*                                           *");
        DAO.getLogger().info("*            Alertmonitor started           *");
        DAO.getLogger().info("*                                           *");
        DAO.getLogger().info("*********************************************");
        DAO.getLogger().info("");
        DAO.getLogger().info("ALERTMONITOR_VERSION=" + DAO.version);
        DAO.getLogger().info("ALERTMONITOR_IPADDR=" + DAO.getInstance().getLocalIpAddress());

        // read all environment variables
        Map<String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
            DAO.getLogger().info(entry.getKey() + "=" + entry.getValue());
        }

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

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // no implementation
        DAO.getLogger().info("#");
        DAO.getLogger().info("# Stopping Alertmonitor");
        DAO.getLogger().info("#\n\n\n");
        DAO.getLogger().close();
    }


}
