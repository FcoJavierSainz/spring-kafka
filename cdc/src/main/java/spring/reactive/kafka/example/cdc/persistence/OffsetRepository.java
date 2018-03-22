package spring.reactive.kafka.example.cdc.persistence;

import org.davidmoten.rx.jdbc.Database;
import org.davidmoten.rx.jdbc.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import spring.reactive.kafka.example.cdc.config.KafkaDemoProperties;

@Repository
public class OffsetRepository {

  @Autowired
  private Database database;

  @Autowired
  KafkaDemoProperties properties;

  public Mono<Long> getOffset(String table) {
    return RxJava2Adapter
        .flowableToFlux(
            database.select("select offset from " + properties.getCdc().getOffsetTableName()
                + " where table_name = ?")
                .parameter(table)
                .getAs(Long.class)

        ).next();
  }

  public Mono<Integer> storeOffset(String table, long offset) {
    return RxJava2Adapter
        .flowableToFlux(
            database.update("insert into " + properties.getCdc().getOffsetTableName()
                + " (table_name, offset) values(?, ?)"
                + "on duplicate key update offset = ?")
                .parameters(table, offset, offset)
                .counts()

        ).next();
  }
}
