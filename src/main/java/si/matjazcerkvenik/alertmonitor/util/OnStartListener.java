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
import java.util.Map;

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

        DAO.getLogger().info("*********************************************");
        DAO.getLogger().info("*                                           *");
        DAO.getLogger().info("*            Alertmonitor started           *");
        DAO.getLogger().info("*                                           *");
        DAO.getLogger().info("*********************************************\n");
        DAO.getLogger().info("ALERTMONITOR_VERSION=" + DAO.version);
        DAO.getLogger().info("ALERTMONITOR_IP_ADDR=" + DAO.getInstance().getLocalIpAddress());

        // read all environment variables
        Map<String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
            DAO.getLogger().info(entry.getKey() + "=" + entry.getValue());
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
