package com.github.juliusd.radiohitsplaylist.monitoring;

import static com.github.juliusd.radiohitsplaylist.Logger.log;

import java.time.Duration;
import java.time.LocalDateTime;

public class LoggingNotifier implements Notifier {

  private final Statistic statistic = new Statistic();

  @Override
  public void runStarted() {
    statistic.runStarted();
    log("Run started at " + LocalDateTime.now().toString());
    log("Version: " + LoggingNotifier.class.getPackage().getImplementationVersion());
  }

  @Override
  public void recordPlaylistShuffled(String playlistName) {
    statistic.recordPlaylistShuffled(playlistName);
    log("Playlist shuffled: " + playlistName);
  }

  @Override
  public void recordPlaylistRefresh(String streamName, int amountOfTracks) {
    statistic.recordPlaylistRefresh(streamName, amountOfTracks);
    log("Playlist refreshed - Stream: " + streamName + ", Tracks: " + amountOfTracks);
  }

  @Override
  public void recordSoundgraphExecuted(String name, int amountOfTracks) {
    statistic.recordSoundgraphExecuted(name, amountOfTracks);
    log("Soundgraph executed - Name: " + name + ", Tracks: " + amountOfTracks);
  }

  @Override
  public void recordInitialCacheSize(long cacheSize) {
    statistic.recordInitialCacheSize(cacheSize);
    log("Initial cache size: " + cacheSize);
  }

  @Override
  public void recordFinalCacheSize(long cacheSize) {
    statistic.recordFinalCacheSize(cacheSize);

    long newTracksAdded = statistic.getNewTracksAdded();
    long cacheHits = statistic.getCacheHits();
    long cacheMisses = statistic.getCacheMisses();

    StringBuilder cacheMessage = new StringBuilder();
    cacheMessage.append("Track cache: ").append(cacheSize).append(" total tracks");
    cacheMessage.append(" (+").append(newTracksAdded).append(" new)");
    if (cacheHits > 0) {
      cacheMessage.append(", ").append(cacheHits).append(" cache hits");
    }
    if (cacheMisses > 0) {
      cacheMessage.append(", ").append(cacheMisses).append(" cache misses");
    }

    log(cacheMessage.toString());
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
  public void runFinishedSuccessfully() {
    String durationText = getDurationText();
    log("Run finished successfully after " + durationText);
  }

  @Override
  public void runFailed(Throwable throwable) {
    log("Run failed: " + throwable.getMessage());
  }

  private String getDurationText() {
    if (statistic.getStartTime() != null) {
      LocalDateTime endTime = LocalDateTime.now();
      Duration duration = Duration.between(statistic.getStartTime(), endTime);
      return formatDuration(duration);
    }
    return "???";
  }

  private static String formatDuration(Duration duration) {
    long minutes = duration.toMinutes();
    long seconds = duration.minusMinutes(minutes).getSeconds();

    if (minutes > 0) {
      return String.format("%d min %d sec", minutes, seconds);
    } else {
      return String.format("%d sec", seconds);
    }
  }
}
