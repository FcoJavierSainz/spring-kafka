package spring.reactive.kafka.example.cdc.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spring.reactive.kafka.example.cdc.CdcService;

public class DemoLeaderSelectorListener implements LeaderSelectorListener {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private CdcService cdc;

  public DemoLeaderSelectorListener(CdcService cdc) {
    this.cdc = cdc;
  }

  @Override
  public void takeLeadership(CuratorFramework client) throws Exception {
    startCdc();
  }

  private void startCdc() {
    cdc.startCapturing();
  }

  @Override
  public void stateChanged(CuratorFramework client, ConnectionState newState) {

    switch (newState) {
      case SUSPENDED:
        stopCdc();
        break;

      case RECONNECTED:
        startCdc();
        break;

      case LOST:
        stopCdc();
        break;
    }
  }

  private void stopCdc() {
    cdc.stopCapturing();
  }
}
