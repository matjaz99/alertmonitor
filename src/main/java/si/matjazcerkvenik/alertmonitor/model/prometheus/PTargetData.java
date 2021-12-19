package si.matjazcerkvenik.alertmonitor.model.prometheus;

import java.util.List;

public class PTargetData {

    private List<PTarget> activeTargets;

    public List<PTarget> getActiveTargets() {
        return activeTargets;
    }

    public void setActiveTargets(List<PTarget> activeTargets) {
        this.activeTargets = activeTargets;
    }

    @Override
    public String toString() {
        return "PTargetData{" +
                "activeTargets=" + activeTargets +
                '}';
    }

}
