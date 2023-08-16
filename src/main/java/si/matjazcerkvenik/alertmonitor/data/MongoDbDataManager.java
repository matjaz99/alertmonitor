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
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DWarning;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;
import si.matjazcerkvenik.alertmonitor.web.WebhookMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDbDataManager implements IDataManager {

    private static SimpleLogger logger = LogFactory.getLogger();

    private MongoClient mongoClient;

    public MongoDbDataManager() {
//        mongoClient = MongoClients.create(AmProps.ALERTMONITOR_MONGODB_CONNECTION_STRING);

        int timeoutSeconds = 5;

        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(AmProps.ALERTMONITOR_MONGODB_CONNECT_TIMEOUT_SEC, SECONDS);
                    builder.readTimeout(AmProps.ALERTMONITOR_MONGODB_READ_TIMEOUT_SEC, SECONDS);
                })
                .applyToClusterSettings( builder -> builder.serverSelectionTimeout(AmProps.ALERTMONITOR_MONGODB_CONNECT_TIMEOUT_SEC, SECONDS))
                .applyConnectionString(new ConnectionString(AmProps.ALERTMONITOR_MONGODB_CONNECTION_STRING))
                .codecRegistry(codecRegistry)
                .build();

        mongoClient = MongoClients.create(settings);

        logger.info("MongoDbDataManager initialized");
    }

    @Override
    public void addWebhookMessage(WebhookMessage message) {

        logger.info("MongoDbDataManager: addWebhookMessage");

        try {
            MongoDatabase db = mongoClient.getDatabase(AmProps.ALERTMONITOR_MONGODB_DB_NAME);
            MongoCollection<WebhookMessage> collection = db.getCollection("webhook", WebhookMessage.class);

            // insert one doc
            collection.insertOne(message);

            DAO.getInstance().removeWarningFromAllProviders("mongo");
            AmMetrics.alertmonitor_db_inserts_total.labels("webhook").inc();

        } catch (Exception e) {
            logger.error("MongoDbDataManager: addWebhookMessage: Exception: " + e.getMessage());
            DAO.getInstance().addWarningToAllProviders("mongo", "No connection to DB", DWarning.DWARNING_SEVERITY_CRITICAL);
            AmMetrics.alertmonitor_db_failures_total.labels().inc();
        }

    }

    @Override
    public List<WebhookMessage> getWebhookMessages() {
        logger.info("MongoDbDataManager: getWebhookMessages");
        try {
            MongoDatabase db = mongoClient.getDatabase(AmProps.ALERTMONITOR_MONGODB_DB_NAME);
            MongoCollection<WebhookMessage> collection = db.getCollection("webhook", WebhookMessage.class);

            List<WebhookMessage> docsResultList = collection.find(Filters.eq("runtimeId", AmProps.RUNTIME_ID))
                    .sort(Sorts.descending("id"))
                    .limit(100)
                    .into(new ArrayList<>());

            logger.info("MongoDbDataManager: docsResultList size=" + docsResultList.size());

            DAO.getInstance().removeWarningFromAllProviders("mongo");
            AmMetrics.alertmonitor_db_queries_total.labels("webhook").inc();

            return docsResultList;

        } catch (Exception e) {
            logger.error("MongoDbDataManager: getWebhookMessages: Exception: ", e);
            DAO.getInstance().addWarningToAllProviders("mongo", "No connection to DB", DWarning.DWARNING_SEVERITY_CRITICAL);
            AmMetrics.alertmonitor_db_failures_total.labels().inc();
        }
        return null;
    }

    @Override
    public void addToJournal(List<DEvent> events) {

        if (events.size() == 0) return;

        logger.info("MongoDbDataManager: add to journal (" + events.size() + ")");

        try {
            MongoDatabase db = mongoClient.getDatabase(AmProps.ALERTMONITOR_MONGODB_DB_NAME);
            MongoCollection<DEvent> collection = db.getCollection("journal", DEvent.class);

            collection.insertMany(events, new InsertManyOptions().ordered(false));

            DAO.getInstance().removeWarningFromAllProviders("mongo");
            AmMetrics.alertmonitor_db_inserts_total.labels("journal").inc();

        } catch (Exception e) {
            logger.error("MongoDbDataManager: addToJournal(): Exception: " + e.getMessage());
            DAO.getInstance().addWarningToAllProviders("mongo", "No connection to DB", DWarning.DWARNING_SEVERITY_CRITICAL);
            AmMetrics.alertmonitor_db_failures_total.labels().inc();
        }

    }

    @Override
    public List<DEvent> getJournal() {
        logger.info("MongoDbDataManager: getJournal");

        try {
            MongoDatabase db = mongoClient.getDatabase(AmProps.ALERTMONITOR_MONGODB_DB_NAME);
            MongoCollection<DEvent> collection = db.getCollection("journal", DEvent.class);

            List<DEvent> docsResultList = collection.find()
                    .sort(Sorts.descending("timestamp"))
                    .limit(5000)
                    .into(new ArrayList<>());
            // TODO add provider as filter

            logger.info("MongoDbDataManager: docsResultList size=" + docsResultList.size());

            DAO.getInstance().removeWarningFromAllProviders("mongo");
            AmMetrics.alertmonitor_db_queries_total.labels("journal").inc();

            return docsResultList;

        } catch (Exception e) {
            logger.error("MongoDbDataManager: getJournal: Exception: " + e.getMessage());
            DAO.getInstance().addWarningToAllProviders("mongo", "No connection to DB", DWarning.DWARNING_SEVERITY_CRITICAL);
            AmMetrics.alertmonitor_db_failures_total.labels().inc();
        }
        return null;
    }

    @Override
    public long getJournalSize() {
        logger.info("MongoDbDataManager: getJournalSize");

        try {
            MongoDatabase db = mongoClient.getDatabase(AmProps.ALERTMONITOR_MONGODB_DB_NAME);
            MongoCollection<Document> collection = db.getCollection("journal");

            DAO.getInstance().removeWarningFromAllProviders("mongo");
            AmMetrics.alertmonitor_db_queries_total.labels("journal").inc();

            return collection.countDocuments();

        } catch (Exception e) {
            logger.error("MongoDbDataManager: getJournalSize: Exception: " + e.getMessage());
            DAO.getInstance().addWarningToAllProviders("mongo", "No connection to DB", DWarning.DWARNING_SEVERITY_CRITICAL);
            AmMetrics.alertmonitor_db_failures_total.labels().inc();
        }
        return -1;
    }

    @Override
    public int getNumberOfAlertsInLastHour() {
        logger.info("MongoDbDataManager: getNumberOfAlertsInLastHour");

        try {
            MongoDatabase db = mongoClient.getDatabase(AmProps.ALERTMONITOR_MONGODB_DB_NAME);
            MongoCollection<Document> collection = db.getCollection("journal");

            Bson filter = Filters.gte("timestamp", System.currentTimeMillis() - 3600 * 1000);

            DAO.getInstance().removeWarningFromAllProviders("mongo");
            AmMetrics.alertmonitor_db_queries_total.labels("journal").inc();

            return (int) collection.countDocuments(filter);

        } catch (Exception e) {
            logger.error("MongoDbDataManager: getNumberOfAlertsInLastHour: Exception: " + e.getMessage());
            DAO.getInstance().addWarningToAllProviders("mongo", "No connection to DB", DWarning.DWARNING_SEVERITY_CRITICAL);
            AmMetrics.alertmonitor_db_failures_total.labels().inc();
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
    public DEvent getEvent(String uid) {
        logger.info("MongoDbDataManager: getEvent id=" + uid);

        try {
            MongoDatabase db = mongoClient.getDatabase(AmProps.ALERTMONITOR_MONGODB_DB_NAME);
            MongoCollection<DEvent> collection = db.getCollection("journal", DEvent.class);

            DEvent event = collection.find(Filters.eq("uid", uid)).first();
            DAO.getInstance().removeWarningFromAllProviders("mongo");
            AmMetrics.alertmonitor_db_queries_total.labels("journal").inc();
            return event;

        } catch (Exception e) {
            logger.error("MongoDbDataManager: getEvent: Exception: " + e.getMessage());
            DAO.getInstance().addWarningToAllProviders("mongo", "No connection to DB", DWarning.DWARNING_SEVERITY_CRITICAL);
            AmMetrics.alertmonitor_db_failures_total.labels().inc();
        }
        return null;
    }

    @Override
    public void cleanDB() {

        logger.info("MongoDbDataManager: cleanDB: started");

        try {

            long daysInMillis = Integer.toUnsignedLong(AmProps.ALERTMONITOR_DATA_RETENTION_DAYS) * 24 * 3600 * 1000;
            long diff = (System.currentTimeMillis() - daysInMillis);
            Bson filter = Filters.lte("timestamp", diff);

            MongoDatabase db = mongoClient.getDatabase(AmProps.ALERTMONITOR_MONGODB_DB_NAME);
            MongoCollection<Document> collection = db.getCollection("webhook");
            DeleteResult resultDeleteMany = collection.deleteMany(filter);
            logger.info("MongoDbDataManager: cleanDB [webhook]: result" + resultDeleteMany);
            AmMetrics.alertmonitor_db_deletes_total.labels("webhook").inc();

            MongoCollection<Document> collection2 = db.getCollection("journal");
            DeleteResult resultDeleteMany2 = collection2.deleteMany(filter);
            logger.info("MongoDbDataManager: cleanDB [journal]: result" + resultDeleteMany2);
            AmMetrics.alertmonitor_db_deletes_total.labels("journal").inc();

            DAO.getInstance().removeWarningFromAllProviders("mongo");

        } catch (Exception e) {
            logger.error("MongoDbDataManager: cleanDB: Exception: " + e.getMessage());
            DAO.getInstance().addWarningToAllProviders("mongo", "No connection to DB", DWarning.DWARNING_SEVERITY_CRITICAL);
            AmMetrics.alertmonitor_db_failures_total.labels().inc();
        }
    }

    @Override
    public void handleAlarmClearing(DEvent clearEvent) {
        try {
            MongoDatabase db = mongoClient.getDatabase(AmProps.ALERTMONITOR_MONGODB_DB_NAME);
            MongoCollection<Document> collection = db.getCollection("journal");

            Bson filter = Filters.and(
                    Filters.eq("correlationId", clearEvent.getCorrelationId()),
                    Filters.eq("provider", clearEvent.getProvider()),
                    Filters.eq("clearTimestamp", 0));
            Bson updateOperation1 = Updates.set("clearTimestamp", clearEvent.getClearTimestamp());
            Bson updateOperation2 = Updates.set("clearUid", clearEvent.getUid());
            Bson updates = Updates.combine(updateOperation1, updateOperation2);
            UpdateResult updateResult = collection.updateMany(filter, updates);
            logger.info("MongoDbDataManager: handleAlarmClearing: result" + updateResult);
            AmMetrics.alertmonitor_db_updates_total.labels("journal").inc();

            DAO.getInstance().removeWarningFromAllProviders("mongo");

        } catch (Exception e) {
            logger.error("MongoDbDataManager: handleAlarmClearing: Exception: " + e.getMessage());
            DAO.getInstance().addWarningToAllProviders("mongo", "No connection to DB", DWarning.DWARNING_SEVERITY_CRITICAL);
            AmMetrics.alertmonitor_db_failures_total.labels().inc();
        }
    }

    public void close() {
        mongoClient.close();
    }
}
