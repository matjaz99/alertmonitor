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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MongoDbDAO extends DAOInterface {

    public static String connectionString = "mongodb://admin:mongodbpassword@promvm:27017/test?w=majority&authSource=admin";

    public void something() {
        DAOInterface.MONGO_ENABLED = true;
    }

    @Override
    public void addWebhookMessage(WebhookMessage message) {

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase sampleTrainingDB = mongoClient.getDatabase("alertmonitor");
            MongoCollection<Document> gradesCollection = sampleTrainingDB.getCollection("webhook");

            Document doc = new Document("_id", new ObjectId());
            doc.append("messageId", message.getId())
                    .append("timestamp", message.getTimestamp())
                    .append("contentLength", message.getContentLength())
                    .append("contentType", message.getContentType())
                    .append("method", message.getMethod())
                    .append("pathInfo", message.getPathInfo())
                    .append("protocol", message.getProtocol())
                    .append("remoteHost", message.getRemoteHost())
                    .append("remotePort", message.getRemotePort())
                    .append("requestUri", message.getRequestUri())
                    .append("headerMap", message.getHeaderMap())
                    .append("parameterMap", message.getParameterMap())
                    .append("body", message.getBody());

            // insert one doc
            gradesCollection.insertOne(doc);

        }

    }

    @Override
    public List<WebhookMessage> getWebhookMessages() {
        return null;
    }

    @Override
    public void addToJournal(DEvent event) {

    }

    @Override
    public List<DEvent> getJournal() {
        return null;
    }

    @Override
    public DEvent getEvent(String id) {
        return null;
    }
}
