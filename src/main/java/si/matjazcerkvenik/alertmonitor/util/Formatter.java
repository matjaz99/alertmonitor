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
