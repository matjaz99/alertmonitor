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
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;

import java.util.List;

public interface IDataManager {

    public void addWebhookMessage(WebhookMessage message);

    public List<WebhookMessage> getWebhookMessages();

    public void addToJournal(List<DEvent> event);

    public List<DEvent> getJournal();

    public long getJournalSize();

    public DEvent getEvent(String id);

    public int getNumberOfAlertsInLastHour();

    public String getAlertsPerSecondInLastHour();

    public void cleanDB();

    public void handleAlarmClearing(DEvent clearEvent);

    public void close();

}
