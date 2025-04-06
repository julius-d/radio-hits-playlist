package com.github.juliusd.radiohitsplaylist.monitoring;

import com.github.juliusd.radiohitsplaylist.config.NotifierConfiguration;

class GotifyNotifier implements Notifier {

  private final Statistic statistic = new Statistic();
  private final GotifyClient gotifyClient;
  private final NotifierConfiguration config;

  GotifyNotifier(GotifyClient gotifyClient, NotifierConfiguration config) {
    this.gotifyClient = gotifyClient;
    this.config = config;
  }

  @Override
  public void recordPlaylistShuffled(String playlistName) {
    statistic.recordPlaylistShuffled(playlistName);
  }

  @Override
  public void recordPlaylistRefresh(String streamName, int amountOfTracks) {
    statistic.recordPlaylistRefresh(streamName, amountOfTracks);
  }

  @Override
  public void runFinishedSuccessfully() {
    if (config.notifyOnSuccess()) {
      var messageText = NotificationTextBuilder.createMessageText(statistic);

      var message = new GotfiyMessage("Finished", messageText, 1);
      gotifyClient.sendMessage(config.gotifyApiToken(), message);
    }
  }

  @Override
  public void runFailed(Throwable throwable) {
    if (config.notifyOnFailure()) {
      var messageText = NotificationTextBuilder.createFailedMessageText(throwable);

      var message = new GotfiyMessage("Failed", messageText, 3);
      gotifyClient.sendMessage(config.gotifyApiToken(), message);
    }
  }

}
