package spring.reactive.kafka.example.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import spring.reactive.kafka.example.cdc.config.KafkaDemoProperties;

@SpringBootApplication
public class EventProducerApplication {

  public static void main(String[] args) {
    SpringApplication.run(EventProducerApplication.class, args);
  }
}
