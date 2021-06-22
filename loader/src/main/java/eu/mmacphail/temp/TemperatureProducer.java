package eu.mmacphail.temp;

import eu.mmacphail.data.Temperature;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TemperatureProducer {

    private static final Logger log = LoggerFactory.getLogger(TemperatureProducer.class);

    public static void main(String[] args) throws InterruptedException {
        List<String> stations = IntStream.rangeClosed(1, 5).mapToObj(i -> "station-" + i).collect(Collectors.toList());

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "docker-desktop:31654");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry.cluster.local");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        Random rng = new Random();

        try(KafkaProducer<String, Temperature> producer = new KafkaProducer<>(props)) {
            while(true) {
                String station = stations.get(rng.nextInt(5));
                Temperature temperature = new Temperature(station, rng.nextInt());
                ProducerRecord<String, Temperature> record = new ProducerRecord<>("temperature", station, temperature);
                producer.send(record, (md, e) -> {
                   if(e != null) {
                       e.printStackTrace();
                       log.error(e.getMessage());
                   } else {
                        log.info("temp {}° for station {}", temperature.getTemperature(), station);
                   }
                });
                Thread.sleep(2000);
            }
        }
    }

}
