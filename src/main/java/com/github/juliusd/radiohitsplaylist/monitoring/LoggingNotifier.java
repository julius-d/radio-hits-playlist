package com.github.juliusd.radiohitsplaylist.monitoring;

import static com.github.juliusd.radiohitsplaylist.Logger.log;

import java.time.LocalDateTime;

public class LoggingNotifier implements Notifier {
  @Override
  public void runStarted() {
    log("Run started at " + LocalDateTime.now().toString());
    log("Version: " + LoggingNotifier.class.getPackage().getImplementationVersion());
  }

  @Override
  public void recordPlaylistShuffled(String playlistName) {
    log("Playlist shuffled: " + playlistName);
  }

  @Override
  public void recordPlaylistRefresh(String streamName, int amountOfTracks) {
    log("Playlist refreshed - Stream: " + streamName + ", Tracks: " + amountOfTracks);
  }

  @Override
  public void recordSoundgraphExecuted(String name, int amountOfTracks) {
    log("Soundgraph executed - Name: " + name + ", Tracks: " + amountOfTracks);
  }

  @Override
  public void recordInitialCacheSize(long cacheSize) {
    log("Initial cache size: " + cacheSize);
  }

  @Override
  public void recordFinalCacheSize(long cacheSize) {
    log("Final cache size: " + cacheSize);
  }

  @Override
  public void recordCacheHit() {
    log("Cache hit");
  }

  @Override
  public void runFinishedSuccessfully() {
    log("Run finished successfully");
  }

  @Override
  public void runFailed(Throwable throwable) {
    log("Run failed: " + throwable.getMessage());
  }
}
