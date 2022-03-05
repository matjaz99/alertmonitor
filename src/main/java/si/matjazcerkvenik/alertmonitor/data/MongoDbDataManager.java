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
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MongoDbDataManager implements IDataManager {

    private static SimpleLogger logger = LogFactory.getLogger();
    public static String dbName = "alertmonitor";
    private MongoClient mongoClient;

    public MongoDbDataManager() {
//        mongoClient = MongoClients.create(AmProps.ALERTMONITOR_MONGODB_CONNECTION_STRING);

        int timeoutSeconds = 5;

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(timeoutSeconds * 1000, MILLISECONDS);
                    builder.readTimeout(timeoutSeconds * 1000, MILLISECONDS);
                })
                .applyToClusterSettings( builder -> builder.serverSelectionTimeout(timeoutSeconds * 1000, MILLISECONDS))
                .applyConnectionString(new ConnectionString(AmProps.ALERTMONITOR_MONGODB_CONNECTION_STRING))
                .build();

        mongoClient = MongoClients.create(settings);

        logger.info("MongoDbDataManager initialized");
    }

    @Override
    public void addWebhookMessage(WebhookMessage message) {

        logger.info("MongoDbDataManager: addWebhookMessage");

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("webhook_messages");

//            Document doc = Document.parse(new Gson().toJson(message));
            Document doc = new Document("_id", new ObjectId());
            doc.append("id", message.getId())
                    .append("runtimeId", message.getRuntimeId())
                    .append("timestamp", message.getTimestamp())
                    .append("contentLength", message.getContentLength())
                    .append("contentType", message.getContentType())
                    .append("method", message.getMethod())
                    .append("protocol", message.getProtocol())
                    .append("remoteHost", message.getRemoteHost())
                    .append("remotePort", message.getRemotePort())
                    .append("requestUri", message.getRequestUri())
                    .append("headerMap", message.getHeaderMap())
                    .append("parameterMap", message.getParameterMap())
                    .append("body", message.getBody());

            // insert one doc
            collection.insertOne(doc);

            DAO.getInstance().removeWarning("mongo");

        } catch (Exception e) {
            logger.error("MongoDbDataManager: addWebhookMessage: Exception: " + e.getMessage());
            DAO.getInstance().addWarning("mongo", "No connection to DB");
        }

    }

    @Override
    public List<WebhookMessage> getWebhookMessages() {
        logger.info("MongoDbDataManager: getWebhookMessages");
        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("webhook_messages");

            List<Document> docsResultList = collection.find(Filters.eq("runtimeId", AmProps.ALERTMONITOR_RUNTIME_ID))
                    .sort(Sorts.descending("id"))
                    .limit(100)
                    .into(new ArrayList<>());

            logger.info("MongoDbDataManager: docsResultList size=" + docsResultList.size());

            List<WebhookMessage> webhookMessageList = new ArrayList<>();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            for (Document doc : docsResultList) {
                // document: {"_id": {"$oid": "62044887878b4423baf8d9c7"}, "id": 46, "timestamp": {"$numberLong": "1644447879359"}, "contentLength": 1952, "contentType": "application/json", "method": "POST", "protocol": "HTTP/1.1", "remoteHost": "192.168.0.123", "remotePort": 36312, "requestUri": "/alertmonitor/webhook", "body": "{\"receiver\":\"alertmonitor\",\"status\":\"firing\",\"alerts\":[{\"status\":\"firing\",\"labels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"info\":\"SSH on gitlab.iskratel.si:22 has been down for more than 10 minutes.\",\"instance\":\"gitlab.iskratel.si:22\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"annotations\":{\"description\":\"SSH on gitlab.iskratel.si:22 has been down for more than 10 minutes.\",\"summary\":\"SSH on gitlab.iskratel.si:22 is down\"},\"startsAt\":\"2022-02-09T18:54:10.322Z\",\"endsAt\":\"0001-01-01T00:00:00Z\",\"generatorURL\":\"http://promvm.home.net/prometheus/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-ssh%22%7D+%3D%3D+0\\u0026g0.tab=1\",\"fingerprint\":\"6a7e625056f7fa79\"},{\"status\":\"firing\",\"labels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"info\":\"SSH on prom.devops.iskratel.cloud:22 has been down for more than 10 minutes.\",\"instance\":\"prom.devops.iskratel.cloud:22\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"annotations\":{\"description\":\"SSH on prom.devops.iskratel.cloud:22 has been down for more than 10 minutes.\",\"summary\":\"SSH on prom.devops.iskratel.cloud:22 is down\"},\"startsAt\":\"2022-02-09T18:54:25.322Z\",\"endsAt\":\"0001-01-01T00:00:00Z\",\"generatorURL\":\"http://promvm.home.net/prometheus/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-ssh%22%7D+%3D%3D+0\\u0026g0.tab=1\",\"fingerprint\":\"22e2e031457e143b\"}],\"groupLabels\":{\"alertname\":\"SSH Not Responding\"},\"commonLabels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"commonAnnotations\":{},\"externalURL\":\"http://promvm.home.net:9093\",\"version\":\"4\",\"groupKey\":\"{}/{severity=~\\\"^(critical|major|minor|warning|informational|indeterminate)$\\\"}:{alertname=\\\"SSH Not Responding\\\"}\",\"truncatedAlerts\":0}", "parameterMap": {}, "headerMap": {"content-length": "1952", "host": "192.168.0.16:8080", "content-type": "application/json", "user-agent": "Alertmanager/0.23.0"}}
//                System.out.println("document: " + doc.toJson());
//                WebhookMessage am = gson.fromJson(doc.toJson(), WebhookMessage.class);
//                System.out.println("converted back: " + am.toString());
                WebhookMessage m = new WebhookMessage();
                m.setId(((Number) doc.get("id")).longValue());
                m.setRuntimeId(doc.getString("runtimeId"));
                m.setTimestamp(((Number) doc.get("timestamp")).longValue());
                m.setContentLength(doc.getInteger("contentLength"));
                m.setContentType(doc.getString("contentType"));
                m.setMethod(doc.getString("method"));
                m.setProtocol(doc.getString("protocol"));
                m.setRemoteHost(doc.getString("remoteHost"));
                m.setRemotePort(doc.getInteger("remotePort"));
                m.setRequestUri(doc.getString("requestUri"));
                m.setBody(doc.getString("body"));
                m.setHeaderMapString(doc.getString("headerMapString"));
                m.setParameterMapString(doc.getString("parameterMapString"));

                // there are exceptions thrown if document.getString(xx) does not exist

                webhookMessageList.add(m);
            }

            DAO.getInstance().removeWarning("mongo");

            return webhookMessageList;

        } catch (Exception e) {
            logger.error("MongoDbDataManager: getWebhookMessages: Exception: ", e);
            DAO.getInstance().addWarning("mongo", "No connection to DB");
        }
        return null;
    }

    @Override
    public void addToJournal(List<DEvent> events) {

        if (events.size() == 0) return;

        logger.info("MongoDbDataManager: add to journal (" + events.size() + ")");

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("journal");

            List<Document> list = new ArrayList<>();

            for (DEvent e : events) {
                LogFactory.getLogger().debug("MongoDbDataManager: adding to journal uid=" + e.getUid());
                Document doc = Document.parse(new Gson().toJson(e));
                list.add(doc);
            }

            collection.insertMany(list, new InsertManyOptions().ordered(false));

            DAO.getInstance().removeWarning("mongo");

        } catch (Exception e) {
            logger.error("MongoDbDataManager: addToJournal(): Exception: " + e.getMessage());
            DAO.getInstance().addWarning("mongo", "No connection to DB");
        }

    }

    @Override
    public List<DEvent> getJournal() {
        logger.info("MongoDbDataManager: getJournal");

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("journal");

            List<Document> docsResultList = collection.find()
                    .sort(Sorts.descending("timestamp"))
                    .limit(1000)
                    .into(new ArrayList<>());

            logger.info("MongoDbDataManager: docsResultList size=" + docsResultList.size());

            List<DEvent> webhookMessageList = new ArrayList<>();

            for (Document doc : docsResultList) {
                DEvent e = convertToDEvent(doc);
                webhookMessageList.add(e);
            }

            DAO.getInstance().removeWarning("mongo");

            return webhookMessageList;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("MongoDbDataManager: getJournal: Exception: " + e.getMessage());
            DAO.getInstance().addWarning("mongo", "No connection to DB");
        }
        return null;
    }

    @Override
    public long getJournalSize() {
        logger.info("MongoDbDataManager: getJournalSize");

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("journal");

            DAO.getInstance().removeWarning("mongo");

            return collection.countDocuments();

        } catch (Exception e) {
            logger.error("MongoDbDataManager: getJournalSize: Exception: " + e.getMessage());
            DAO.getInstance().addWarning("mongo", "No connection to DB");
        }
        return -1;
    }

    @Override
    public int getNumberOfAlertsInLastHour() {
        logger.info("MongoDbDataManager: getNumberOfAlertsInLastHour");

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("journal");

            Bson filter = Filters.gte("timestamp", System.currentTimeMillis() - 3600 * 1000);

            return (int) collection.countDocuments(filter);

        } catch (Exception e) {
            logger.error("MongoDbDataManager: getNumberOfAlertsInLastHour: Exception: " + e.getMessage());
            DAO.getInstance().addWarning("mongo", "No connection to DB");
        }
        return -1;
    }

    @Override
    public String getAlertsPerSecondInLastHour() {
        int i = getNumberOfAlertsInLastHour();
        if (i < 0) return "n/a";
        double perSecond = i / 3600.0;
        DecimalFormat df2 = new DecimalFormat("#.###");
        return df2.format(perSecond);
    }

    @Override
    public DEvent getEvent(String id) {
        logger.info("MongoDbDataManager: getEvent id=" + id);

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("journal");

            Document doc = collection.find(Filters.eq("uid", id)).first();
            DEvent event = convertToDEvent(doc);

            DAO.getInstance().removeWarning("mongo");

            return event;

        } catch (Exception e) {
            logger.error("MongoDbDataManager: getEvent: Exception: " + e.getMessage());
            DAO.getInstance().addWarning("mongo", "No connection to DB");
        }
        return null;
    }

    private DEvent convertToDEvent(Document doc) {
        DEvent event = new DEvent();
        event.setUid(doc.getString("uid"));
        event.setCorrelationId(doc.getString("correlationId"));
        event.setTimestamp(((Number) doc.get("timestamp")).longValue());
        event.setFirstTimestamp(((Number) doc.get("firstTimestamp")).longValue());
        event.setLastTimestamp(((Number) doc.get("lastTimestamp")).longValue());
        event.setClearTimestamp(((Number) doc.get("clearTimestamp")).longValue());
        event.setClearUid(doc.getString("clearUid"));
        event.setSource(doc.getString("source"));
        event.setUserAgent(doc.getString("userAgent"));
        event.setAlertname(doc.getString("alertname"));
        event.setInfo(doc.getString("info"));
        event.setInstance(doc.getString("instance"));
        event.setHostname(doc.getString("hostname"));
        event.setNodename(doc.getString("nodename"));
        event.setJob(doc.getString("job"));
        event.setTags(doc.getString("tags"));
        event.setSeverity(doc.getString("severity"));
        event.setPriority(doc.getString("priority"));
        event.setGroup(doc.getString("group"));
        event.setEventType(doc.getString("eventType"));
        event.setProbableCause(doc.getString("probableCause"));
        event.setCurrentValue(doc.getString("currentValue"));
        event.setUrl(doc.getString("url"));
        event.setDescription(doc.getString("description"));
        event.setStatus(doc.getString("status"));
        event.setGeneratorUrl(doc.getString("generatorUrl"));
        event.setPrometheusId(doc.getString("prometheusId"));
        event.setOtherLabelsString(doc.getString("otherLabelsString"));
        return event;
    }

    @Override
    public void cleanDB() {

        logger.info("MongoDbDataManager: cleanDB: started");

        try {

            Bson filter = Filters.lte("timestamp",
                    System.currentTimeMillis() - AmProps.ALERTMONITOR_DATA_RETENTION_DAYS * 24 * 3600 * 1000);

            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("webhook_messages");
            DeleteResult resultDeleteMany = collection.deleteMany(filter);
            logger.info("MongoDbDataManager: cleanDB: result" + resultDeleteMany);

            MongoCollection<Document> collection2 = db.getCollection("journal");
            DeleteResult resultDeleteMany2 = collection2.deleteMany(filter);
            logger.info("MongoDbDataManager: cleanDB: result" + resultDeleteMany2);

            DAO.getInstance().removeWarning("mongo");

        } catch (Exception e) {
            logger.error("MongoDbDataManager: cleanDB: Exception: " + e.getMessage());
            DAO.getInstance().addWarning("mongo", "No connection to DB");
        }
    }

    @Override
    public void handleAlarmClearing(DEvent event) {
        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("journal");

            Bson filter = Filters.and(
                    Filters.eq("correlationId", event.getCorrelationId()),
                    Filters.eq("clearTimestamp", 0));
            Bson updateOperation1 = Updates.set("clearTimestamp", event.getClearTimestamp());
            Bson updateOperation2 = Updates.set("clearUid", event.getClearTimestamp());
            Bson updates = Updates.combine(updateOperation1, updateOperation2);
            UpdateResult updateResult = collection.updateMany(filter, updates);
            logger.info("MongoDbDataManager: handleAlarmClearing: result" + updateResult);

            DAO.getInstance().removeWarning("mongo");

        } catch (Exception e) {
            logger.error("MongoDbDataManager: handleAlarmClearing: Exception: " + e.getMessage());
            DAO.getInstance().addWarning("mongo", "No connection to DB");
        }
    }
}
