package com.github.juliusd.radiohitsplaylist.monitoring;

public class NoOpNotifier implements Notifier {
  @Override
  public void runStarted() {}

  @Override
  public void recordPlaylistShuffled(String playlistName) {}

  @Override
  public void recordPlaylistRefresh(String streamName, int amountOfTracks) {}

  @Override
  public void recordSoundgraphExecuted(String name, int amountOfTracks) {}

  @Override
  public void recordInitialCacheSize(long cacheSize) {}

  @Override
  public void recordFinalCacheSize(long cacheSize) {}

  @Override
  public void recordCacheHit() {}

  @Override
  public void recordCacheMiss() {}

  @Override
  public void runFinished() {}

  @Override
  public void runFailed(Throwable throwable) {}

  @Override
  public void runFailed(String taskGroupName, Throwable throwable) {}
}
