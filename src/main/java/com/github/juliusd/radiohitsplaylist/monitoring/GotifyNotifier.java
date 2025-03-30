package com.github.juliusd.radiohitsplaylist.monitoring;

import com.github.juliusd.radiohitsplaylist.config.NotifierConfiguration;

public class GotifyNotifier implements Notifier {

  private final GotifyClient gotifyClient;
  private final NotifierConfiguration config;

  public GotifyNotifier(GotifyClient gotifyClient, NotifierConfiguration config) {
    this.gotifyClient = gotifyClient;
    this.config = config;
  }

  @Override
  public void runFinishedSuccessfully() {
    if (config.notifyOnSuccess()) {
      GotfiyMessage message = new GotfiyMessage("Finished", "run finished successfully", 1);
      gotifyClient.sendMessage(config.gotifyApiToken(), message);
    }
  }
}
