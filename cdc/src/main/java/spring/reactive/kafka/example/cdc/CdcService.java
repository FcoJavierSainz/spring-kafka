package spring.reactive.kafka.example.cdc;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.EventHeaderV4;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.NullEventDataDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.WriteRowsEventDataDeserializer;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import spring.reactive.kafka.example.cdc.config.ChangeDataCaptureConfig.DatabaseInfo;
import spring.reactive.kafka.example.cdc.config.KafkaDemoProperties;
import spring.reactive.kafka.example.cdc.persistence.OffsetRepository;


public class CdcService {

  Logger logger = LoggerFactory.getLogger(getClass());
  KafkaTemplate<Long, String> kafkaTemplate;
  OffsetRepository offsetRepository;
  KafkaDemoProperties properties;
  DatabaseInfo database;

  BinaryLogClient client;

  private HashMap<Long, TableMapEventData> tablesData = new HashMap<>(1);

  long offset;

  public CdcService(KafkaTemplate<Long, String> kafkaTemplate,
      OffsetRepository offsetRepository,
      KafkaDemoProperties properties,
      DatabaseInfo database) {
    this.kafkaTemplate = kafkaTemplate;
    this.offsetRepository = offsetRepository;
    this.properties = properties;
    this.database = database;
  }

  public void startCapturing() {
    if (client != null && client.isConnected()) {
      return;
    }

    String tableName = properties.getCdc().getTableName();
    offset = offsetRepository.getOffset(tableName).blockOptional().orElse(0L);
    client = new BinaryLogClient(database.getHost(), database.getPort(),
        database.getScheme(), database.getUser(), database.getPassword());
    client.setServerId(1);
    client.setKeepAliveInterval(5 * 1000);
    client.setBinlogPosition(offset);
    client.setEventDeserializer(createEventDeserializer());
    client.registerEventListener(event -> {
      switch (event.getHeader().getEventType()) {
        case TABLE_MAP: {
          TableMapEventData eventData = event.getData();
          if (eventData.getTable().equalsIgnoreCase(tableName)) {
            tablesData.put(eventData.getTableId(), eventData);
          }
          break;
        }
        case EXT_WRITE_ROWS: {
          offset = ((EventHeaderV4) event.getHeader()).getPosition();
          WriteRowsEventData eventData = event.getData();
          if (tablesData.containsKey(eventData.getTableId())) {
            sendMessages(eventData);
            updateOffset(offset);
          }
          break;
        }
      }
    });
    try {
      client.connect();
    } catch (IOException e) {
      logger.error("Error when connecting to the trail log", e);
    }
  }

  public void stopCapturing() {
    if (client != null) {
      try {
        client.disconnect();
      } catch (IOException e) {
        logger.error("Error when disconnecting to the trail log", e);
      }
    }
  }

  private EventDeserializer createEventDeserializer() {
    EventDeserializer eventDeserializer = new EventDeserializer();
    //ignore all events, except for Insert
    Arrays.stream(EventType.values()).forEach(eventType -> {
      if (eventType != EventType.EXT_WRITE_ROWS &&
          eventType != EventType.TABLE_MAP) {
        eventDeserializer.setEventDataDeserializer(eventType,
            new NullEventDataDeserializer());
      }
    });

    eventDeserializer.setEventDataDeserializer(EventType.EXT_WRITE_ROWS,
        new WriteRowsEventDataDeserializer(
            tablesData).setMayContainExtraInformation(true));
    return eventDeserializer;
  }


  private void updateOffset(long offset) {
    int result = offsetRepository.storeOffset(properties.getCdc().getTableName(), offset).block();
    logger.info("Update offset: " + result);
  }


  private void sendMessages(WriteRowsEventData eventData) {

    kafkaTemplate.executeInTransaction(operations -> {
      eventData.getRows().stream().forEach(data ->
          sendMessages(operations, (Long) data[0], (String) data[1])
      );
      return true;
    });
  }

  private void sendMessages(KafkaOperations<Long, String> tx, long id, String description) {
    tx.send("events", id, description);
  }
}
