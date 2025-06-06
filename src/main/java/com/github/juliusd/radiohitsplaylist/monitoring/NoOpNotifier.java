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
  public void runFinishedSuccessfully() {}

  @Override
  public void runFailed(Throwable throwable) {}
}
