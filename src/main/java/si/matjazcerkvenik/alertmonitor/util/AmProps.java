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

public class AmProps {

    public static String ALERTMONITOR_RUNTIME_ID = "0000-0000-0000-0000";
    public static long startUpTime = 0;
    public static String version = "n/a";
    public static boolean isContainerized = false;
    public static String localIpAddress;

    public static boolean devEnv = false;

    /** Environment variables */
    public static int ALERTMONITOR_DATA_RETENTION_DAYS = 7;
    public static int ALERTMONITOR_PSYNC_INTERVAL_SEC = 300;
    public static String ALERTMONITOR_PROMETHEUS_SERVER = "http://localhost:9090";
    public static int ALERTMONITOR_HTTP_CLIENT_READ_TIMEOUT_SEC = 120;
    public static String ALERTMONITOR_DATE_FORMAT = "yyyy/MM/dd H:mm:ss";
    public static boolean ALERTMONITOR_KAFKA_ENABLED = false;
    public static String ALERTMONITOR_KAFKA_SERVER = "localhost:9092";
    public static String ALERTMONITOR_KAFKA_TOPIC = "alertmonitor_alerts";
    public static String ALERTMONITOR_PROMETHEUS_ID_LABELS = "cluster, region, monitor";
    public static boolean ALERTMONITOR_MONGODB_ENABLED = false;
    public static String ALERTMONITOR_MONGODB_CONNECTION_STRING = "mongodb://admin:mongodbpassword@promvm:27017/test?w=majority&authSource=admin";

}
