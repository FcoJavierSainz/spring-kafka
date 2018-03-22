package spring.reactive.kafka.example.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.annotation.ServiceActivator;

@SpringBootApplication
@EnableBinding(Sink.class)
public class EventConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(EventConsumerApplication.class, args);
  }

  private static Logger logger = LoggerFactory.getLogger(EventConsumerApplication.class);

  @ServiceActivator(inputChannel = Sink.INPUT)
  public void loggerSink(Object payload) {
    logger.info("Received: " + payload);
  }

}
