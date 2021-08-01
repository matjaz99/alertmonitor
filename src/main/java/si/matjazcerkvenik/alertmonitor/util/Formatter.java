package si.matjazcerkvenik.alertmonitor.util;

import com.google.gson.Gson;
import si.matjazcerkvenik.alertmonitor.model.DNotification;

public class Formatter {

    public static String toJson(DNotification notification) {
        return new Gson().toJson(notification);
    }

}
