package si.matjazcerkvenik.alertmonitor.model.prometheus;

import java.util.List;

public class PRuleData {

    private List<PRuleGroups> groups;

    public List<PRuleGroups> getGroups() {
        return groups;
    }

    public void setGroups(List<PRuleGroups> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "PRuleData{" +
                "groups=" + groups +
                '}';
    }

}
