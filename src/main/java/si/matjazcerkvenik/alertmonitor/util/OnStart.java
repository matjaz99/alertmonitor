package si.matjazcerkvenik.alertmonitor.util;

import si.matjazcerkvenik.alertmonitor.model.DAO;
import si.matjazcerkvenik.alertmonitor.model.DTag;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class OnStart implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        DAO.startUpTime = System.currentTimeMillis();
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

        DAO.getInstance().addTag(new DTag("critical", "red"));
        DAO.getInstance().addTag(new DTag("left", "violet"));
        DAO.getInstance().addTag(new DTag("right", "green"));
        DAO.getInstance().addTag(new DTag("normal", "orange"));
        DAO.getInstance().addTag(new DTag("banana", "yellow"));

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // no implementation
    }
}
