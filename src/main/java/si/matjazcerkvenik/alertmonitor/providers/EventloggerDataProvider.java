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
package si.matjazcerkvenik.alertmonitor.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DTarget;
import si.matjazcerkvenik.alertmonitor.model.DWarning;
import si.matjazcerkvenik.alertmonitor.model.eventlogger.ElEvent;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

import java.util.ArrayList;
import java.util.List;

public class EventloggerDataProvider extends AbstractDataProvider {
	
	private static final long serialVersionUID = 344648519048L;

    @Override
    public void init() {
        logger.info(providerConfig.toString());
        addWarning("notSupportedProvider", "Provider not supported", DWarning.DWARNING_SEVERITY_WARNING);
    }

    @Override
    public void processIncomingEvent(WebhookMessage m) {
        DAO.getInstance().getDataManager().addWebhookMessage(m);
        webhookRequestsReceivedCount++;
        AmMetrics.alertmonitor_webhook_requests_received_total.labels(providerConfig.getName(), m.getRemoteHost(), m.getMethod().toUpperCase()).inc();

        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Object[] obj = gson.fromJson(m.getBody(), Object[].class);
//            ElEvent am = gson.fromJson(m.getBody(), ElEvent.class);
//            ElMessage em = gson.fromJson(m.getBody(), ElMessage.class);

            List<DEvent> list = new ArrayList<>();

            for (int i = 0; i < obj.length; i++) {

                Gson gson2 = new Gson();
                String json = gson2.toJson(obj[i]);
                ElEvent el = gson.fromJson(json, ElEvent.class);

                DEvent e = new DEvent();
                e.setTimestamp(System.currentTimeMillis());
                e.setFirstTimestamp(e.getTimestamp());
                e.setSource(m.getRemoteHost());
                e.setAlertname(el.getEventName());
                e.setUserAgent(m.getHeaderMap().getOrDefault("user-agent", "-"));
                e.setInfo(el.getAddInfo());
                e.setInstance(el.getSourceHost());
                e.setHostname(Formatter.stripInstance(e.getInstance()));
                e.setNodename(el.getSourceHost());
                e.setJob("eventlogger");
                e.setTags("eventlogger, log");
                e.setSeverity(el.getSeverityString().toLowerCase());
                e.setPriority("low");
                e.setGroup("unknown");
                e.setEventType("5");
                e.setProbableCause("1024");
                e.setCurrentValue("-");
                e.setUrl("");
                e.setDescription(el.getMessage());
                e.generateUID();
                e.generateCID();

                System.out.println("GOT EVENT: " + e.toString());
                list.add(e);
            }

            synchronizeAlerts(list, false);
            lastEventTimestamp = System.currentTimeMillis();

        } catch (Exception e) {
            LogFactory.getLogger().error("EventloggerDataProvider: processIncomingEvent(): unable to process incoming message: \n" + m.toString());
            LogFactory.getLogger().error("EventloggerDataProvider: processIncomingEvent(): error: " + e.getMessage());
        }
    }

    @Override
    public String reloadPrometheusAction() {
        // TODO
        return null;
    }

    @Override
    public DEvent getEvent(String id) {
        // TODO
        return super.getEvent(id);
    }

    @Override
    public List<DTarget> getTargets() {
        // TODO
        return null;
    }

    @Override
    public List<DTarget> getSmartTargets() {
        // TODO
        return null;
    }

    @Override
    public DTarget getSingleTarget(String id) {
        // TODO
        return null;
    }

    @Override
    public void restartSyncTimer() {
        // TODO
    }
    
    @Override
    public void stopSyncTimer() {
    	// TODO
    	
    }
}
