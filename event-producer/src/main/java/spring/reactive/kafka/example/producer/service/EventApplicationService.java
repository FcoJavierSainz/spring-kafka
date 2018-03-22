package spring.reactive.kafka.example.producer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import spring.reactive.kafka.example.producer.domain.Event;
import spring.reactive.kafka.example.producer.domain.EventRepository;

@Service
public class EventApplicationService {

  @Autowired
  EventRepository repository;

  public Mono<Event> createEvent(CreateEventCommand command) {
    Event event = Event.fromCommand(command);
    return repository.saveEvent(event);
  }
}
