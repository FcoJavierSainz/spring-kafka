package spring.reactive.kafka.example.cdc.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.davidmoten.rx.jdbc.pool.DatabaseType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("kafka-demo")
@Getter
@Setter
public class KafkaDemoProperties {

  @Valid
  private Zookeeper zookeeper;

  @Valid
  private Database database;

  @Valid
  private Cdc cdc;

  @Getter
  @Setter
  public static class Database {

    @NotNull
    private String url;
    private int maxPoolConnections = 5;
    @NotNull
    private DatabaseType type;
  }

  @Getter
  @Setter
  public static class Zookeeper {

    @NotNull
    private String connectionString;
    @NotNull
    private String lockPath;
  }

  @Getter
  @Setter
  public static class Cdc {

    @NotNull
    private String tableName;

    @NotNull
    private String offsetTableName = "offset";
  }

}
