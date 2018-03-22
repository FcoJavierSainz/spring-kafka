package spring.reactive.kafka.example.producer.domain;

import reactor.core.publisher.Mono;

public interface EventRepository {

  Mono<Event> saveEvent(Event event);

}
