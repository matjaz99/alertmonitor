package si.matjazcerkvenik.alertmonitor.util;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import si.matjazcerkvenik.alertmonitor.model.DAO;

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
        props.put("bootstrap.servers", DAO.ALERTMONITOR_KAFKA_SERVER);
        props.put("acks", "all");
        props.put("retries", 0);
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

        producer.send(new ProducerRecord<String, String>(topic, Long.toString(msgCounter), json));
        msgCounter++;

    }
}
