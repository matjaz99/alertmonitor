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
package si.matjazcerkvenik.alertmonitor.data;

import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.util.List;

public abstract class DAOInterface {

    public static boolean MONGO_ENABLED = false;

    public abstract void addWebhookMessage(WebhookMessage message);

    public abstract List<WebhookMessage> getWebhookMessages();

    public abstract void addToJournal(DEvent event);

    public abstract List<DEvent> getJournal();

    public abstract DEvent getEvent(String id);
}
