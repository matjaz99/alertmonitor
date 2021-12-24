package si.matjazcerkvenik.alertmonitor.model.prometheus;

import java.util.List;

public class PRuleGroup {

    private String name;
    private String file;
    private List<PRule> rules;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public List<PRule> getRules() {
        return rules;
    }

    public void setRules(List<PRule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "PRuleGroups{" +
                "name='" + name + '\'' +
                ", file='" + file + '\'' +
                ", rules=" + rules +
                '}';
    }

}
