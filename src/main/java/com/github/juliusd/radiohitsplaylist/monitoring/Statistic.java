package com.github.juliusd.radiohitsplaylist.monitoring;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Statistic {

  private final List<String> shuffledPlaylists = new ArrayList<>();
  private final List<PlaylistRefreshResult> refreshedPlaylists = new ArrayList<>();
  private final List<SoundgraphResult> soundgraphResults = new ArrayList<>();
  private LocalDateTime startTime;

  public void recordPlaylistShuffled(String playlistName) {
    shuffledPlaylists.add(playlistName);
  }

  public void recordPlaylistRefresh(String streamName, int amountOfTracks) {
    refreshedPlaylists.add(new PlaylistRefreshResult(streamName, amountOfTracks));
  }

  public void recordSoundgraphExecuted(String playlistId, int amountOfTracks) {
    soundgraphResults.add(new SoundgraphResult(playlistId, amountOfTracks));
  }

  public List<String> getShuffledPlaylists() {
    return Collections.unmodifiableList(shuffledPlaylists);
  }

  public List<PlaylistRefreshResult> getRefreshedPlaylists() {
    return Collections.unmodifiableList(refreshedPlaylists);
  }

  public List<SoundgraphResult> getSoundgraphResults() {
    return Collections.unmodifiableList(soundgraphResults);
  }

  public void runStarted() {
    startTime = LocalDateTime.now();
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public record PlaylistRefreshResult(
    String streamName, int amountOfTracks
  ) {
  }

  public record SoundgraphResult(
    String playlistId, int amountOfTracks
  ) {
  }
}
