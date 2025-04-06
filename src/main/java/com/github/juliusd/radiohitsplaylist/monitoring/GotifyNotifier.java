package com.github.juliusd.radiohitsplaylist.monitoring;

import com.github.juliusd.radiohitsplaylist.Statistic;
import com.github.juliusd.radiohitsplaylist.config.NotifierConfiguration;

import java.util.List;

public class GotifyNotifier implements Notifier {

  private final GotifyClient gotifyClient;
  private final NotifierConfiguration config;

  public GotifyNotifier(GotifyClient gotifyClient, NotifierConfiguration config) {
    this.gotifyClient = gotifyClient;
    this.config = config;
  }

  @Override
  public void runFinishedSuccessfully(Statistic statistic) {
    if (config.notifyOnSuccess()) {
      var messageText = NotificationTextBuilder.createMessageText(statistic);

      var message = new GotfiyMessage("Finished", messageText, 1);
      gotifyClient.sendMessage(config.gotifyApiToken(), message);
    }
  }

  @Override
  public void runFailed(Throwable throwable) {
    if (config.notifyOnFailure()) {
      var message = new GotfiyMessage("Failed", "run failed with " + throwable.getMessage(), 3);
      gotifyClient.sendMessage(config.gotifyApiToken(), message);
    }
  }
}
