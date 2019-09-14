package si.matjazcerkvenik.alertmonitor.util;

import si.matjazcerkvenik.alertmonitor.model.DAO;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class OnStartListener implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        DAO.startUpTime = System.currentTimeMillis();

        // read version file
        InputStream inputStream = servletContextEvent.getServletContext().getResourceAsStream("/WEB-INF/version.txt");
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
            DAO.version = textBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        DAO.getLogger().info("***********************************");
//        DAO.getLogger().info("*                                 *");
//        DAO.getLogger().info("*       Alertmonitor started      *");
//        DAO.getLogger().info("*                                 *");
//        DAO.getLogger().info("***********************************");
//
//        DAO.getLogger().info("VERSION=" + DAO.version);

        // read environment variables
        Map<String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
//            DAO.getLogger().info(entry.getKey() + "=" + entry.getValue());
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // no implementation
//        DAO.getLogger().info("#");
//        DAO.getLogger().info("# Stopping Alertmonitor");
//        DAO.getLogger().info("#\n\n\n");
    }
}
