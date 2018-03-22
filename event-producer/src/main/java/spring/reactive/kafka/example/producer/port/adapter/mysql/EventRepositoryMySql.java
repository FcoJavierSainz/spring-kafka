package spring.reactive.kafka.example.producer.port.adapter.mysql;

import javax.annotation.PostConstruct;
import org.davidmoten.rx.jdbc.Database;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import spring.reactive.kafka.example.producer.domain.Event;
import spring.reactive.kafka.example.producer.domain.EventRepository;

@Repository
public class EventRepositoryMySql implements EventRepository {

  @Autowired
  Database database;

  @Autowired
  Logger logger;

  @Override
  public Mono<Event> saveEvent(Event event) {
    return RxJava2Adapter
        .flowableToFlux(
            database.update("insert into event (description, created_on) values (?, ?)")
                .parameters(event.getDescription(), event.getWhen())
                .returnGeneratedKeys()
                .getAs(Integer.class)
        ).map(id -> {
          event.setId(id);
          return event;
        }).next();
  }

  @PostConstruct
  private void setupDatabase() {
    database.update("create table if not exists event (\n"
        + "  id bigint auto_increment primary key,\n"
        + "  description varchar(255),\n"
        + "  created_on timestamp\n"
        + ")")
        .counts()
        .blockingSubscribe(created -> logger.info("Table created:" + created));
  }
}
