package spring.reactive.kafka.example.producer.domain;

import java.time.Instant;
import lombok.Data;
import spring.reactive.kafka.example.producer.service.CreateEventCommand;

@Data
public class Event {

  private int id;
  private String description;
  private Instant when;

  public static Event fromCommand(CreateEventCommand command) {
    Event e = new Event();
    e.setDescription(command.getDescription());
    e.setWhen(Instant.now());
    return e;
  }
}
