package spring.reactive.kafka.example.producer.service;

import lombok.Data;

@Data
public class CreateEventCommand {

  private String description;
}
