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

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.config.ConfigReader;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class OnStartListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        AmProps.START_UP_TIME = System.currentTimeMillis();
        AmProps.RUNTIME_ID = UUID.randomUUID().toString();
        if (new File("/Users/matjaz").exists()) AmProps.DEV_ENV = true;

        LogFactory.getLogger().info("");
        LogFactory.getLogger().info("************************************************");
        LogFactory.getLogger().info("*                                              *");
        LogFactory.getLogger().info("*            Alertmonitor started              *");
        LogFactory.getLogger().info("*                                              *");
        LogFactory.getLogger().info("************************************************");
        LogFactory.getLogger().info("");

        // read version file
        InputStream inputStream = servletContextEvent.getServletContext().getResourceAsStream("/WEB-INF/version.txt");
        try {
            DataInputStream dis = new DataInputStream(inputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            AmProps.VERSION = br.readLine().trim();
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            AmProps.LOCAL_IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            AmProps.LOCAL_IP_ADDRESS = "unknown";
        }

        // is running inside docker
        try (Stream< String > stream =
                     Files.lines(Paths.get("/proc/1/cgroup"))) {
            LogFactory.getLogger().info("Running in container: " + stream.anyMatch(line -> line.contains("/docker")));
            AmProps.IS_CONTAINERIZED = true;
        } catch (IOException e) {
            LogFactory.getLogger().info("Running in container: false");
            AmProps.IS_CONTAINERIZED = false;
        }

        // print configuration
        LogFactory.getLogger().info("ALERTMONITOR_VERSION=" + AmProps.VERSION);
        LogFactory.getLogger().info("ALERTMONITOR_IPADDR=" + AmProps.LOCAL_IP_ADDRESS);
        LogFactory.getLogger().info("RUNTIME_ID=" + AmProps.RUNTIME_ID);

        // load configuration from env vars
        AmProps.loadProps();

        // read and print all environment variables
        LogFactory.getLogger().info("***** Environment variables *****");
        Map<String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
            LogFactory.getLogger().info(entry.getKey() + "=" + entry.getValue());
        }

        // load yaml config file
        AmProps.yamlConfig = ConfigReader.loadProvidersYamlConfig(AmProps.ALERTMONITOR_DATAPROVIDERS_CONFIG_FILE);

        // initialize DAO
        DAO.getInstance();

        AmMetrics.alertmonitor_build_info.labels("Alertmonitor", AmProps.RUNTIME_ID, AmProps.VERSION, System.getProperty("os.name")).set(AmProps.START_UP_TIME);

        AmProps.githubVersion = TaskManager.getInstance().getVersionFromGithub();

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        LogFactory.getLogger().info("#");
        LogFactory.getLogger().info("# Stopping Alertmonitor");
        LogFactory.getLogger().info("#\n\n\n");
        LogFactory.getLogger().close();
    }


}
