package si.matjazcerkvenik.alertmonitor;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
public class EventsMng {

    public static List<Event> eventList = new ArrayList<Event>();

    public static List<Event> getEventList() {
        return eventList;
    }
}
