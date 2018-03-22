package spring.reactive.kafka.example.producer.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import spring.reactive.kafka.example.producer.domain.Event;
import spring.reactive.kafka.example.producer.service.CreateEventCommand;
import spring.reactive.kafka.example.producer.service.EventApplicationService;

@RestController
@RequestMapping("/events")
public class EventsResource {

  @Autowired
  EventApplicationService service;

  @PostMapping
  public Mono<Event> createEvent(@RequestBody CreateEventCommand command) {
    return service.createEvent(command);
  }
}
