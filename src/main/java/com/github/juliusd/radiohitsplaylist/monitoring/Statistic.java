package com.github.juliusd.radiohitsplaylist.monitoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Statistic {

  private final List<String> shuffledPlaylists = new ArrayList<>();
  private final List<PlaylistRefreshResult> refreshedPlaylists = new ArrayList<>();

  public void recordPlaylistShuffled(String playlistName) {
    shuffledPlaylists.add(playlistName);
  }

  public void recordPlaylistRefresh(String streamName, int amountOfTracks) {
    refreshedPlaylists.add(new PlaylistRefreshResult(streamName, amountOfTracks));
  }

  public List<String> getShuffledPlaylists() {
    return Collections.unmodifiableList(shuffledPlaylists);
  }

  public List<PlaylistRefreshResult> getRefreshedPlaylists() {
    return Collections.unmodifiableList(refreshedPlaylists);
  }

  public record PlaylistRefreshResult(
    String streamName, int amountOfTracks
  ) {
  }
}
