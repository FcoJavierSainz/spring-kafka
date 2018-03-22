package spring.reactive.kafka.example.cdc.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperConfig {

  @Autowired
  KafkaDemoProperties properties;

  @Bean
  CuratorFramework getCuratorFramework() {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.
        builder().retryPolicy(retryPolicy)
        .connectString(properties.getZookeeper().getConnectionString())
        .build();
    client.start();
    return client;
  }

}
