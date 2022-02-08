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
package si.matjazcerkvenik.alertmonitor.util;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import si.matjazcerkvenik.alertmonitor.data.DAO;

import java.util.Properties;

public class KafkaClient {

    private static int kafkaClientsCount = 0;
    private int clientId;
    private Properties props = new Properties();
    private Producer<String, String> producer;
    private long msgCounter = 0L;

    private static KafkaClient kafkaClient;

    public KafkaClient() {

        clientId = kafkaClientsCount++;

//        props.put("bootstrap.servers", "centosvm:9092");
        props.put("bootstrap.servers", AmProps.ALERTMONITOR_KAFKA_SERVER);
//        props.put("acks", "all");   // all replicas acknowledge reception
        props.put("acks", "0");   // do not wait confirmation
        props.put("retries", 0);   // only relevant if acks=all or acks=1
//        props.put("request.timeout.ms", 3000);  // only relevant if acks=all or acks=1
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<String, String>(props);

    }

    public static KafkaClient getInstance() {
        if (kafkaClient == null) kafkaClient = new KafkaClient();
        return kafkaClient;
    }

    public void publish(String topic, String json) {

        producer.send(new ProducerRecord<String, String>(topic, Long.toString(msgCounter++), json));
        LogFactory.getLogger().info("kafka publish to topic: " + topic + ": [" + props.get("bootstrap.servers") + "]: " + json);

    }

    public void resetClient() {
        producer.close();
        kafkaClient = null;
    }
}
