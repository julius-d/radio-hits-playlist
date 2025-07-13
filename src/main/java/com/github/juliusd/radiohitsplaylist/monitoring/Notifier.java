package com.github.juliusd.radiohitsplaylist.monitoring;

public interface Notifier {
  void runStarted();

  void recordPlaylistShuffled(String playlistName);

  void recordPlaylistRefresh(String streamName, int amountOfTracks);

  void recordSoundgraphExecuted(String name, int amountOfTracks);

  void recordInitialCacheSize(long cacheSize);

  void recordFinalCacheSize(long cacheSize);

  void recordCacheHit();

  void recordCacheMiss();

  void runFinishedSuccessfully();

  void runFailed(Throwable throwable);
}
