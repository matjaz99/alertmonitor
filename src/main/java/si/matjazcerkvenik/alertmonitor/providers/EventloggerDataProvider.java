package si.matjazcerkvenik.alertmonitor.providers;

import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.Target;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

import java.util.List;

public class EventloggerDataProvider extends AbstractDataProvider {

    @Override
    public void processIncomingEvent(WebhookMessage m) {
        // TODO
    }

    @Override
    public DEvent getEvent(String id) {
        // TODO
        return super.getEvent(id);
    }

    @Override
    public List<Target> getTargets() {
        // TODO
        return null;
    }

    @Override
    public List<Target> getSmartTargets() {
        // TODO
        return null;
    }

    @Override
    public Target getSingleTarget(String id) {
        // TODO
        return null;
    }

    @Override
    public void restartSyncTimer() {
        // TODO
    }
}
