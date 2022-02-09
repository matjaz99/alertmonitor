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

    public static long startUpTime = 0;
    public static String version = "n/a";
    public static boolean isContainerized = false;

    /** Environment variables */
    public static int JOURNAL_TABLE_SIZE = 5000;
    public static int ALERTMONITOR_PSYNC_INTERVAL_SEC = 300;
    public static String ALERTMONITOR_PROMETHEUS_SERVER = "http://localhost:9090";
    public static String DATE_FORMAT = "yyyy/MM/dd H:mm:ss";
    public static boolean ALERTMONITOR_KAFKA_ENABLED = false;
    public static String ALERTMONITOR_KAFKA_SERVER = "kafkahost:9092";
    public static String ALERTMONITOR_KAFKA_TOPIC = "alertmonitor_alerts";
    public static String ALERTMONITOR_PROMETHEUS_ID_LABELS = "cluster, region, monitor";

}
