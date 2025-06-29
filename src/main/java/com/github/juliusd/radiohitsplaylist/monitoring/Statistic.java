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
  private long initialCacheSize = 0;
  private long finalCacheSize = 0;
  private long cacheHits = 0;

  public void recordPlaylistShuffled(String playlistName) {
    shuffledPlaylists.add(playlistName);
  }

  public void recordPlaylistRefresh(String streamName, int amountOfTracks) {
    refreshedPlaylists.add(new PlaylistRefreshResult(streamName, amountOfTracks));
  }

  public void recordSoundgraphExecuted(String name, int amountOfTracks) {
    soundgraphResults.add(new SoundgraphResult(name, amountOfTracks));
  }

  public void recordInitialCacheSize(long cacheSize) {
    this.initialCacheSize = cacheSize;
  }

  public void recordFinalCacheSize(long cacheSize) {
    this.finalCacheSize = cacheSize;
  }

  public void recordCacheHit() {
    this.cacheHits++;
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

  public long getInitialCacheSize() {
    return initialCacheSize;
  }

  public long getFinalCacheSize() {
    return finalCacheSize;
  }

  public long getNewTracksAdded() {
    return finalCacheSize - initialCacheSize;
  }

  public long getCacheHits() {
    return cacheHits;
  }

  public void runStarted() {
    startTime = LocalDateTime.now();
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public record PlaylistRefreshResult(String streamName, int amountOfTracks) {}

  public record SoundgraphResult(String name, int amountOfTracks) {}
}
