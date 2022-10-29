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

import java.io.File;

public class AmProps {

    public static String ALERTMONITOR_RUNTIME_ID = "0000-0000-0000-0000";
    public static long START_UP_TIME = 0;
    public static String VERSION = "n/a";
    public static String githubVersion = "n/a";
    public static boolean IS_CONTAINERIZED = false;
    public static String LOCAL_IP_ADDRESS;

    public static boolean devEnv = false;

    /** Environment variables */
    public static int ALERTMONITOR_DATA_RETENTION_DAYS = 7;
    public static int ALERTMONITOR_PSYNC_INTERVAL_SEC = 300;
    public static String ALERTMONITOR_PROMETHEUS_SERVER = "http://localhost:9090";
    public static int ALERTMONITOR_PROMETHEUS_CLIENT_POOL_SIZE = 1;
    public static int ALERTMONITOR_HTTP_CLIENT_READ_TIMEOUT_SEC = 120;
    public static String ALERTMONITOR_DATE_FORMAT = "yyyy/MM/dd H:mm:ss";
    public static boolean ALERTMONITOR_KAFKA_ENABLED = false;
    public static String ALERTMONITOR_KAFKA_SERVER = "localhost:9092";
    public static String ALERTMONITOR_KAFKA_TOPIC = "alertmonitor_alerts";
    public static String ALERTMONITOR_PROMETHEUS_ID_LABELS = "cluster, region, monitor";
    public static boolean ALERTMONITOR_MONGODB_ENABLED = false;
    public static String ALERTMONITOR_MONGODB_CONNECTION_STRING = "mongodb://admin:mongodbpassword@promvm:27017/test?w=majority&authSource=admin";

    public static void loadProps() {

        // read configuration from environment variables
        ALERTMONITOR_DATA_RETENTION_DAYS = Integer.parseInt(System.getenv().getOrDefault("ALERTMONITOR_DATA_RETENTION_DAYS", "30").trim());
        ALERTMONITOR_PSYNC_INTERVAL_SEC = Integer.parseInt(System.getenv().getOrDefault("ALERTMONITOR_PSYNC_INTERVAL_SEC", "60").trim());
        if (ALERTMONITOR_PROMETHEUS_SERVER.endsWith("/")) ALERTMONITOR_PROMETHEUS_SERVER = ALERTMONITOR_PROMETHEUS_SERVER.substring(0, ALERTMONITOR_PROMETHEUS_SERVER.length()-1);
        ALERTMONITOR_PROMETHEUS_SERVER = System.getenv().getOrDefault("ALERTMONITOR_PROMETHEUS_SERVER", "http://localhost:9090").trim();
        if (ALERTMONITOR_PROMETHEUS_SERVER.endsWith("/")) ALERTMONITOR_PROMETHEUS_SERVER = ALERTMONITOR_PROMETHEUS_SERVER.substring(0, ALERTMONITOR_PROMETHEUS_SERVER.length()-1);
        if (!ALERTMONITOR_PROMETHEUS_SERVER.startsWith("http")) ALERTMONITOR_PROMETHEUS_SERVER = "http://" + ALERTMONITOR_PROMETHEUS_SERVER;
        ALERTMONITOR_PROMETHEUS_CLIENT_POOL_SIZE = Integer.parseInt(System.getenv().getOrDefault("ALERTMONITOR_PROMETHEUS_CLIENT_POOL_SIZE", "1").trim());
        ALERTMONITOR_HTTP_CLIENT_READ_TIMEOUT_SEC = Integer.parseInt(System.getenv().getOrDefault("ALERTMONITOR_HTTP_CLIENT_READ_TIMEOUT_SEC", "120").trim());
        ALERTMONITOR_DATE_FORMAT = System.getenv().getOrDefault("ALERTMONITOR_DATE_FORMAT", "yyyy/MM/dd H:mm:ss").trim();
        ALERTMONITOR_KAFKA_ENABLED = Boolean.parseBoolean(System.getenv().getOrDefault("ALERTMONITOR_KAFKA_ENABLED", "false").trim());
        ALERTMONITOR_KAFKA_SERVER = System.getenv().getOrDefault("ALERTMONITOR_KAFKA_SERVER", "localhost:9092").trim();
        ALERTMONITOR_KAFKA_TOPIC = System.getenv().getOrDefault("ALERTMONITOR_KAFKA_TOPIC", "alertmonitor_events").trim();
        ALERTMONITOR_PROMETHEUS_ID_LABELS = System.getenv().getOrDefault("ALERTMONITOR_PROMETHEUS_ID_LABELS", "cluster, region, monitor").trim();
        ALERTMONITOR_MONGODB_ENABLED = Boolean.parseBoolean(System.getenv().getOrDefault("ALERTMONITOR_MONGODB_ENABLED", "false").trim());
//        AmProps.ALERTMONITOR_MONGODB_CONNECTION_STRING = System.getenv().getOrDefault("ALERTMONITOR_MONGODB_CONNECTION_STRING", "mongodb://admin:mongodbpassword@promvm:27017/test?w=majority&authSource=admin").trim();
        ALERTMONITOR_MONGODB_CONNECTION_STRING = System.getenv().getOrDefault("ALERTMONITOR_MONGODB_CONNECTION_STRING", "mongodb://admin:mongodbpassword@promvm:27017/?authSource=admin").trim();

        // set development environment, override default configuration
        if (new File("/Users/matjaz").exists()) {
            ALERTMONITOR_PROMETHEUS_SERVER = "https://elasticvm/prometheus";
            ALERTMONITOR_MONGODB_ENABLED = false;
            ALERTMONITOR_MONGODB_CONNECTION_STRING = "mongodb://admin:mongodbpassword@elasticvm:27017/?authSource=admin";
        }

    }

}
