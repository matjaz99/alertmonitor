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

import com.google.gson.Gson;
import si.matjazcerkvenik.alertmonitor.model.DEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Formatter {

    /**
     * Convert event to JSON formatted string
     * @param event
     * @return json string
     */
    public static String toJson(DEvent event) {
        return new Gson().toJson(event);
    }

    /**
     * Format timestamp from millis into readable form.
     * @param timestamp unix timestamp in millis
     * @return readable date
     */
    public static String getFormatedTimestamp(long timestamp, AmDateFormat format) {
        if (timestamp == 0) return "n/a";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(AmProps.ALERTMONITOR_DATE_FORMAT);
        switch (format){
            case DATE:
                sdf = new SimpleDateFormat("yyyy/MM/dd");
                break;
            case TIME:
                sdf = new SimpleDateFormat("H:mm:ss");
                break;
            case ISO8601:
                // TODO
                break;
        }
        return sdf.format(cal.getTime());
    }

    /**
     * This will return given number of seconds in format: __d __h __m __s.
     * @param seconds
     * @return formatted time
     */
    public static String convertToDHMSFormat(int seconds) {
        int secRemain = seconds % 60;
        int minTotal = seconds / 60;
        int minRemain = minTotal % 60;
        int hourTotal = minTotal / 60;
        int hourRemain = hourTotal % 60;
        int dayTotal = hourTotal / 24;
        int dayRemain = hourTotal % 24;

        String resp = minRemain + "m " + secRemain + "s";

        if (dayTotal == 0) {
            if (hourRemain > 0) {
                resp = hourTotal + "h " + resp;
            }
        }

        if (dayTotal > 0) {
            resp = dayTotal + "d " + dayRemain + "h " + resp;
        }

        return resp;
    }

    /**
     * Convert '1.667248865E9' to '1667248865'
     * @return non-scientific notated number
     */
    public static String convertScientificNotationToString(String scientificNumber) {
        return String.format("%.0f", Double.parseDouble(scientificNumber));
    }

    /**
     * Remove leading protocol (eg. http://) and trailing port (eg. :8080).
     * @param instance
     * @return hostname
     */
    public static String stripInstance(String instance) {

        if (instance == null) return instance;

        // remove protocol
        if (instance.contains("://")) {
            instance = instance.split("://")[1];
        }
        // remove port
        instance = instance.split(":")[0];

        // remove relative URL
        instance = instance.split("/")[0];

        // resolve to IP address
//        try {
//            InetAddress address = InetAddress.getByName(instance);
//            instance = address.getHostAddress();
//        } catch (UnknownHostException e) {
//            // nothing to do, leave as it is
//            DAO.getLogger().warn("Cannot resolve: " + instance);
//        }
        return instance;
    }

}
