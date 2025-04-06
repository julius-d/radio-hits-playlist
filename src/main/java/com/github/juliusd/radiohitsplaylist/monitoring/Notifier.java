package com.github.juliusd.radiohitsplaylist.monitoring;

public interface Notifier {
  void runStarted();

  void recordPlaylistShuffled(String playlistName);

  void recordPlaylistRefresh(String streamName, int amountOfTracks);

  void runFinishedSuccessfully();

  void runFailed(Throwable throwable);
}
