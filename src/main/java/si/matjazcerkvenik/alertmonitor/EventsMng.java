package si.matjazcerkvenik.alertmonitor;

import javax.faces.bean.ManagedBean;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
public class EventsMng {

    public static List<Event> eventList = new ArrayList<Event>();

    public static List<Event> getEventList() {
        return eventList;
    }
}
