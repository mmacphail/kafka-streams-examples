package eu.mmacphail.temp;

import eu.mmacphail.data.Temperature;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class MaxTempWindowedStreamsApp {

  public static void main(String[] args) {

    System.out.println(">>> Starting the streams-app Application");

    final Properties settings = new Properties();
    settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "max-temp-streams-wdw");
    settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "docker-desktop:31654");
    settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

    final Topology topology = getTopology();
    // you can paste the topology into this site for a vizualization: https://zz85.github.io/kafka-streams-viz/
    System.out.println(topology.describe());
    final KafkaStreams streams = new KafkaStreams(topology, settings);
    final CountDownLatch latch = new CountDownLatch(1);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("<<< Stopping the streams-app Application");
      streams.close();
      latch.countDown();
    }));

    try {
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }
    System.exit(0);
  }

  private static Topology getTopology() {
    final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", "http://schema-registry.cluster.local");
    final Serde<Temperature> temperatureValueSerde = new SpecificAvroSerde<>();
    temperatureValueSerde.configure(serdeConfig, false);

    final StreamsBuilder builder = new StreamsBuilder();

    final KStream<String, Temperature> temperatures = builder.stream("temperature",
            Consumed.with(Serdes.String(), temperatureValueSerde));

    KTable<Windowed<String>, Temperature> maxTemps = temperatures.groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofSeconds(1L)))
            .aggregate(
                    () -> new Temperature("unknown-station", Integer.MIN_VALUE),
                    MaxTempWindowedStreamsApp::maxTemp,
                    Materialized.with(Serdes.String(), temperatureValueSerde)
            );


    maxTemps.toStream()
            .map((key, value) -> KeyValue.pair(key.key(), value))
            .to(
        "temperature-max-wdw",
        Produced.with(Serdes.String(), temperatureValueSerde));
    final Topology topology = builder.build();
    return topology;
  }

  private static Temperature maxTemp(String key, Temperature t1, Temperature t2) {
    if(t1.getTemperature() >= t2.getTemperature()) {
      return t1;
    } else {
      return t2;
    }
  }

}
