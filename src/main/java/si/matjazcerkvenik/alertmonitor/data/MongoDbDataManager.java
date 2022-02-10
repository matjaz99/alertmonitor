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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AmAlertMessage;
import si.matjazcerkvenik.alertmonitor.webhook.WebhookMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MongoDbDataManager implements IDataManager {

    public static String connectionString = "mongodb://admin:mongodbpassword@promvm:27017/test?w=majority&authSource=admin";

    @Override
    public void addWebhookMessage(WebhookMessage message) {

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase db = mongoClient.getDatabase("alertmonitor");
            MongoCollection<Document> collection = db.getCollection("webhook_messages");

            Document doc = Document.parse(new Gson().toJson(message));
//            Document doc = new Document("_id", new ObjectId());
//            doc.append("messageId", message.getId())
//                    .append("timestamp", message.getTimestamp())
//                    .append("contentLength", message.getContentLength())
//                    .append("contentType", message.getContentType())
//                    .append("method", message.getMethod())
//                    .append("pathInfo", message.getPathInfo())
//                    .append("protocol", message.getProtocol())
//                    .append("remoteHost", message.getRemoteHost())
//                    .append("remotePort", message.getRemotePort())
//                    .append("requestUri", message.getRequestUri())
//                    .append("headerMap", message.getHeaderMap())
//                    .append("parameterMap", message.getParameterMap())
//                    .append("body", message.getBody());

            // insert one doc
            collection.insertOne(doc);

        }

    }

    @Override
    public List<WebhookMessage> getWebhookMessages() {
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase sampleTrainingDB = mongoClient.getDatabase("alertmonitor");
            MongoCollection<Document> gradesCollection = sampleTrainingDB.getCollection("webhook_messages");

            List<Document> docs = gradesCollection.find()
                    .sort(Sorts.descending("id"))
                    .limit(100)
                    .into(new ArrayList<>());

            List<WebhookMessage> webhookMessageList = new ArrayList<>();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            for (Document student : docs) {
                // document: {"_id": {"$oid": "62044887878b4423baf8d9c7"}, "id": 46, "timestamp": {"$numberLong": "1644447879359"}, "contentLength": 1952, "contentType": "application/json", "method": "POST", "protocol": "HTTP/1.1", "remoteHost": "192.168.0.123", "remotePort": 36312, "requestUri": "/alertmonitor/webhook", "body": "{\"receiver\":\"alertmonitor\",\"status\":\"firing\",\"alerts\":[{\"status\":\"firing\",\"labels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"info\":\"SSH on gitlab.iskratel.si:22 has been down for more than 10 minutes.\",\"instance\":\"gitlab.iskratel.si:22\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"annotations\":{\"description\":\"SSH on gitlab.iskratel.si:22 has been down for more than 10 minutes.\",\"summary\":\"SSH on gitlab.iskratel.si:22 is down\"},\"startsAt\":\"2022-02-09T18:54:10.322Z\",\"endsAt\":\"0001-01-01T00:00:00Z\",\"generatorURL\":\"http://promvm.home.net/prometheus/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-ssh%22%7D+%3D%3D+0\\u0026g0.tab=1\",\"fingerprint\":\"6a7e625056f7fa79\"},{\"status\":\"firing\",\"labels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"info\":\"SSH on prom.devops.iskratel.cloud:22 has been down for more than 10 minutes.\",\"instance\":\"prom.devops.iskratel.cloud:22\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"annotations\":{\"description\":\"SSH on prom.devops.iskratel.cloud:22 has been down for more than 10 minutes.\",\"summary\":\"SSH on prom.devops.iskratel.cloud:22 is down\"},\"startsAt\":\"2022-02-09T18:54:25.322Z\",\"endsAt\":\"0001-01-01T00:00:00Z\",\"generatorURL\":\"http://promvm.home.net/prometheus/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-ssh%22%7D+%3D%3D+0\\u0026g0.tab=1\",\"fingerprint\":\"22e2e031457e143b\"}],\"groupLabels\":{\"alertname\":\"SSH Not Responding\"},\"commonLabels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"commonAnnotations\":{},\"externalURL\":\"http://promvm.home.net:9093\",\"version\":\"4\",\"groupKey\":\"{}/{severity=~\\\"^(critical|major|minor|warning|informational|indeterminate)$\\\"}:{alertname=\\\"SSH Not Responding\\\"}\",\"truncatedAlerts\":0}", "parameterMap": {}, "headerMap": {"content-length": "1952", "host": "192.168.0.16:8080", "content-type": "application/json", "user-agent": "Alertmanager/0.23.0"}}
//                System.out.println("document: " + student.toJson());
//                WebhookMessage am = gson.fromJson(student.toJson(), WebhookMessage.class);
//                System.out.println("converted back: " + am.toString());
                WebhookMessage m = new WebhookMessage();
                m.setId(student.getInteger("id"));
                m.setTimestamp(student.getLong("timestamp"));
                m.setContentLength(student.getInteger("contentLength"));
                m.setContentType(student.getString("contentType"));
                m.setMethod(student.getString("method"));
//                m.setPathInfo(req.getPathInfo());
//                m.setProtocol(req.getProtocol());
//                m.setRemoteHost(req.getRemoteHost());
//                m.setRemotePort(req.getRemotePort());
//                m.setRequestUri(req.getRequestURI());
//                m.setBody(body);
//                m.setHeaderMap(generateHeaderMap(req));
//                m.setParameterMap(generateParamMap(req));
                System.out.println(m.toString());
            }

            return webhookMessageList;

        }
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
