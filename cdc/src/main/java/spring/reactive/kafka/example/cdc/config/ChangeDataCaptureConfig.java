package spring.reactive.kafka.example.cdc.config;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.davidmoten.rx.jdbc.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.KafkaTemplate;
import spring.reactive.kafka.example.cdc.CdcService;
import spring.reactive.kafka.example.cdc.persistence.OffsetRepository;

@Configuration
@Import(ZookeeperConfig.class)
@EnableConfigurationProperties(KafkaDemoProperties.class)
@ComponentScan("spring.reactive.kafka.example.cdc")
public class ChangeDataCaptureConfig {

  @Autowired
  Logger logger;

  KafkaDemoProperties properties;

  @Autowired
  public void setProperties(KafkaDemoProperties properties) {
    this.properties = properties;
  }

  @Autowired
  private KafkaTemplate<Long, String> kafkaTemplate;

  @Bean
  @Scope("prototype")
  Logger logger(InjectionPoint ip) {
    return LoggerFactory.getLogger(ip.getMember().getDeclaringClass());
  }

  @Bean
  CdcService createCdcService(KafkaTemplate<Long, String> kafkaTemplate,
      OffsetRepository repository, DatabaseInfo info) {
    return new CdcService(kafkaTemplate, repository, properties, info);
  }

  @Bean
  LeaderSelector leaderSelector(CuratorFramework client, CdcService service) {
    LeaderSelector selector = new LeaderSelector(client, properties.getZookeeper().getLockPath(),
        new DemoLeaderSelectorListener(service));
    selector.start();
    return selector;
  }

  @Bean
  DatabaseInfo createBinaryLogClient() {
    return new DatabaseInfo(properties.getDatabase().getUrl());
  }

  @Bean
  Database database() {
    Database database = Database.nonBlocking()
        .maxPoolSize(properties.getDatabase().getMaxPoolConnections())
        .url(properties.getDatabase().getUrl())
        .healthCheck(properties.getDatabase().getType())
        .build();
    setupDatabase(database);
    return database;
  }

  private void setupDatabase(Database database) {
    database.update("create table if not exists "
        + properties.getCdc().getOffsetTableName() + " (\n"
        + "  table_name varchar(50) primary key,\n"
        + "  offset bigint\n"
        + ")")
        .counts()
        .blockingSubscribe(created -> logger.info("Table created:" + created));
  }

  @Getter
  @Setter
  public static class DatabaseInfo {

    private String host;
    private int port;
    private String scheme;
    private String user;
    private String password;
    private String url;

    public DatabaseInfo(String url) {
      setUrl(url);
      parser();
    }

    private void parser() {
      String uriString = getUrl().substring(5);
      try {
        URI uri = new URI(uriString);
        setHost(uri.getHost());
        setPort(uri.getPort());
        setScheme(uri.getPath().substring(1));
        Map<String, String> params = getQueryMap(uri.getQuery());
        setUser(params.get("user"));
        setPassword(params.get("password"));
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }


    private Map<String, String> getQueryMap(String query) {
      return Arrays.stream(query.split("&")).
          collect(Collectors.toMap(param -> param.substring(0, param.indexOf((int) '=')),
              param -> param.substring(param.indexOf((int) '=') + 1)));
    }
  }
}
