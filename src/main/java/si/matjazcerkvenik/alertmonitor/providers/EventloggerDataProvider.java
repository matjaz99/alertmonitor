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

import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DTarget;
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
}
