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
  public void runStarted() {
    statistic.runStarted();
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
  public void recordSoundgraphExecuted(String name, int amountOfTracks) {
    statistic.recordSoundgraphExecuted(name, amountOfTracks);
  }

  @Override
  public void recordInitialCacheSize(long cacheSize) {
    statistic.recordInitialCacheSize(cacheSize);
  }

  @Override
  public void recordFinalCacheSize(long cacheSize) {
    statistic.recordFinalCacheSize(cacheSize);
  }

  @Override
  public void recordCacheHit() {
    statistic.recordCacheHit();
  }

  @Override
  public void recordCacheMiss() {
    statistic.recordCacheMiss();
  }

  @Override
  public void runFinished() {
    if (statistic.hasFailures()) {
      if (config.notifyOnFailure()) {
        var messageText = NotificationTextBuilder.createPartialFailureMessageText(statistic);
        var message = new GotfiyMessage("Partial Failure", messageText, 1);
        gotifyClient.sendMessage(config.gotifyApiToken(), message);
      }
    } else {
      if (config.notifyOnSuccess()) {
        var messageText = NotificationTextBuilder.createMessageText(statistic);
        var message = new GotfiyMessage("Success", messageText, 1);
        gotifyClient.sendMessage(config.gotifyApiToken(), message);
      }
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

  @Override
  public void runFailed(String taskGroupName, Throwable throwable) {
    statistic.recordTaskGroupFailure(taskGroupName, throwable);
    if (config.notifyOnFailure()) {
      var messageText = NotificationTextBuilder.createFailedMessageText(taskGroupName, throwable);

      var message = new GotfiyMessage("Failed: " + taskGroupName, messageText, 3);
      gotifyClient.sendMessage(config.gotifyApiToken(), message);
    }
  }
}
