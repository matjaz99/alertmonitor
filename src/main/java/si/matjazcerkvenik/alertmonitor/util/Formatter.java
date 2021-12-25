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
import si.matjazcerkvenik.alertmonitor.model.DNotification;

public class Formatter {

    public static String toJson(DNotification notification) {
        return new Gson().toJson(notification);
    }

    public static String convertToDHMSFormat(int secUpTotal) {
        int secUpRemain = secUpTotal % 60;
        int minUpTotal = secUpTotal / 60;
        int minUpRemain = minUpTotal % 60;
        int hourUpTotal = minUpTotal / 60;
        int hourUpRemain = hourUpTotal % 60;
        int dayUpTotal = hourUpTotal / 24;
        int dayUpRemain = hourUpTotal % 24;

        String resp = minUpRemain + "m " + secUpRemain + "s";

        if (dayUpTotal == 0) {
            if (hourUpRemain > 0) {
                resp = hourUpTotal + "h " + resp;
            }
        }

        if (dayUpTotal > 0) {
            resp = dayUpTotal + "d " + dayUpRemain + "h " + resp;
        }

        return resp;
    }

}
