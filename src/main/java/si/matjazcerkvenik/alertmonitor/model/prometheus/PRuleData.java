package si.matjazcerkvenik.alertmonitor.model.prometheus;

import java.util.List;

public class PRuleData {

    private List<PRuleGroup> groups;

    public List<PRuleGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<PRuleGroup> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "PRuleData{" +
                "groups=" + groups +
                '}';
    }

}
