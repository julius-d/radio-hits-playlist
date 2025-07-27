package com.github.juliusd.radiohitsplaylist.monitoring;

import java.util.List;

public class CompositeNotifier implements Notifier {
  private final List<Notifier> notifiers;

  public CompositeNotifier(List<Notifier> notifiers) {
    this.notifiers = notifiers;
  }

  @Override
  public void runStarted() {
    notifiers.forEach(Notifier::runStarted);
  }

  @Override
  public void recordPlaylistShuffled(String playlistName) {
    notifiers.forEach(notifier -> notifier.recordPlaylistShuffled(playlistName));
  }

  @Override
  public void recordPlaylistRefresh(String streamName, int amountOfTracks) {
    notifiers.forEach(notifier -> notifier.recordPlaylistRefresh(streamName, amountOfTracks));
  }

  @Override
  public void recordSoundgraphExecuted(String name, int amountOfTracks) {
    notifiers.forEach(notifier -> notifier.recordSoundgraphExecuted(name, amountOfTracks));
  }

  @Override
  public void recordInitialCacheSize(long cacheSize) {
    notifiers.forEach(notifier -> notifier.recordInitialCacheSize(cacheSize));
  }

  @Override
  public void recordFinalCacheSize(long cacheSize) {
    notifiers.forEach(notifier -> notifier.recordFinalCacheSize(cacheSize));
  }

  @Override
  public void recordCacheHit() {
    notifiers.forEach(Notifier::recordCacheHit);
  }

  @Override
  public void recordCacheMiss() {
    notifiers.forEach(Notifier::recordCacheMiss);
  }

  @Override
  public void runFinished() {
    notifiers.forEach(Notifier::runFinished);
  }

  @Override
  public void runFailed(Throwable throwable) {
    notifiers.forEach(notifier -> notifier.runFailed(throwable));
  }

  @Override
  public void runFailed(String taskGroupName, Throwable throwable) {
    notifiers.forEach(notifier -> notifier.runFailed(taskGroupName, throwable));
  }
}
