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
package si.matjazcerkvenik.alertmonitor.model.alertmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DSeverity;
import si.matjazcerkvenik.alertmonitor.model.config.ProviderConfig;
import si.matjazcerkvenik.alertmonitor.model.config.YamlConfig;
import si.matjazcerkvenik.alertmonitor.util.*;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

import java.util.*;

public class AlertmanagerProcessor {

    public static AmAlertMessage processWebhookMessage(WebhookMessage wm) throws Exception {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        AmAlertMessage am = gson.fromJson(wm.getBody(), AmAlertMessage.class);
        LogFactory.getLogger().debug(am.toString());
        LogFactory.getLogger().info("AlertmanagerProcessor: alerts received: " + am.getAlerts().size());
        return am;

    }

    public static List<DEvent> convertToDevent(WebhookMessage m, AmAlertMessage am) {

        List<DEvent> eventList = new ArrayList<DEvent>();

        for (Iterator<AmAlert> it = am.getAlerts().iterator(); it.hasNext();) {
            AmAlert a = it.next();

            DEvent e = new DEvent();
            e.setTimestamp(System.currentTimeMillis());
//            e.setFirstTimestamp(e.getTimestamp());
            e.setSource(m.getRemoteHost());
            e.setAlertname(a.getLabels().getOrDefault(DEvent.LBL_ALERTNAME, "-unknown-"));
            e.setUserAgent(m.getHeaderMap().getOrDefault("user-agent", "-"));
            e.setInfo(a.getLabels().getOrDefault(DEvent.LBL_INFO, "-"));
            e.setInstance(a.getLabels().getOrDefault(DEvent.LBL_INSTANCE, "-"));
            e.setHostname(Formatter.stripInstance(e.getInstance()));
            e.setNodename(a.getLabels().getOrDefault(DEvent.LBL_NODENAME, e.getInstance()));
            e.setJob(a.getLabels().getOrDefault(DEvent.LBL_JOB, "-"));
            e.setTags(a.getLabels().getOrDefault(DEvent.LBL_TAGS, ""));
            e.setSeverity(a.getLabels().getOrDefault(DEvent.LBL_SEVERITY, "indeterminate"));
            e.setPriority(a.getLabels().getOrDefault(DEvent.LBL_PRIORITY, "low"));
            e.setGroup(a.getLabels().getOrDefault(DEvent.LBL_GROUP, "unknown"));
            e.setEventType(a.getLabels().getOrDefault(DEvent.LBL_EVENTTYPE, "5"));
            e.setProbableCause(a.getLabels().getOrDefault(DEvent.LBL_PROBABLECAUSE, "1024"));
            e.setCurrentValue(a.getAnnotations().getOrDefault(DEvent.LBL_CURRENTVALUE, "-"));
            e.setUrl(a.getLabels().getOrDefault(DEvent.LBL_URL, ""));
            if (a.getLabels().containsKey(DEvent.LBL_DESCRIPTION)) {
                e.setDescription(a.getLabels().getOrDefault(DEvent.LBL_DESCRIPTION, "-"));
            } else {
                e.setDescription(a.getAnnotations().getOrDefault(DEvent.LBL_DESCRIPTION, "-"));
            }
            e.setStatus(a.getStatus());
            e.setGeneratorUrl(a.getGeneratorURL());

            // set prometheusId
            String[] lblArray = AmProps.ALERTMONITOR_PROMETHEUS_ID_LABELS.split(",");
            String s = "{";
            for (int i = 0; i < lblArray.length; i++) {
                s += lblArray[i].trim() + "=\"" + a.getLabels().getOrDefault(lblArray[i].trim(), "-") + "\", ";
            }
            s = s.substring(0, s.length()-2) + "}";
            e.setPrometheusId(s);

            // set provider name
            if (AmProps.yamlConfig != null) {
                for (ProviderConfig pc : AmProps.yamlConfig.getProviders()) {
                    if (pc.getUri().equalsIgnoreCase(m.getRequestUri())) {
                        e.setProvider(pc.getName());
                        break;
                    }
                }
            } else {
                e.setProvider(AmProps.ALERTMONITOR_DATAPROVIDERS_DEFAULT_PROVIDER_NAME);
            }

            // set all other labels
            e.setOtherLabels(a.getLabels());

            // set severity=clear for all events that have status=resolved, but not for those with severity=informational
            if (a.getStatus().equalsIgnoreCase("resolved")) {
                e.setSeverity(DSeverity.CLEAR);
            }

            // add other labels directly into tags
            // eg: severity (but not clear), priority
            if (!e.getSeverity().equals(DSeverity.CLEAR)) {
                e.setTags(e.getTags() + "," + e.getSeverity());
            }
            e.setTags(e.getTags() + "," + e.getPriority());

            // environment variable substitution
            e.setNodename(substitute(e.getNodename()));
            e.setInfo(substitute(e.getInfo()));
            e.setDescription(substitute(e.getDescription()));
            e.setTags(substitute(e.getTags()));
            e.setUrl(substitute(e.getUrl()));

            // set unique ID of event
            e.generateUID();

            // set correlation ID
            e.generateCID();

            eventList.add(e);

            LogFactory.getLogger().debug("AlertmanagerProcessor: " + e.toString());

        }

        return eventList;

    }



    /**
     * This method does environment variable substitution
     */
    public static String substitute(String s) {

        if (s == null || !s.contains("${")) {
            return s;
        }

        Map<String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
            s = s.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return s;

    }

}
