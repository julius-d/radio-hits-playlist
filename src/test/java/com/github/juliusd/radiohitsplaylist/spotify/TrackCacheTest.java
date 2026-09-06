package com.github.juliusd.radiohitsplaylist.spotify;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.radiohitsplaylist.Track;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TrackCacheTest {

  @TempDir Path tempDir;

  private TrackCache trackCache;
  private Path tempDbPath;

  @BeforeEach
  void setUp() {
    tempDbPath = tempDir.resolve("test_track_cache_v2.db");
    trackCache = new TrackCache(tempDbPath.toString());
  }

  private static SpotifyTrack spotify(String title, String artist, String uri) {
    return new SpotifyTrack(title, List.of(artist), URI.create(uri), null);
  }

  private static SpotifyTrack spotify(String title, List<String> artists, String uri) {
    return new SpotifyTrack(title, artists, URI.create(uri), null);
  }

  @Test
  void findTrack_returnsEmptyWhenNotCached() {
    assertThat(trackCache.findTrack(new Track("Test Song", "Test Artist"))).isEmpty();
  }

  @Test
  void storeAndFind_exactMatch() {
    var spotifyUri = URI.create("spotify:track:1234567890abcdef");
    trackCache.storeTrack(spotify("Test Song", "Test Artist", "spotify:track:1234567890abcdef"));

    assertThat(trackCache.findTrack(new Track("Test Song", "Test Artist"))).contains(spotifyUri);
  }

  @Test
  void findTrack_matchesCaseInsensitive() {
    trackCache.storeTrack(spotify("New Religion", "Bebe Rexha", "spotify:track:abc"));

    // Radio sends title in ALL CAPS
    assertThat(trackCache.findTrack(new Track("NEW RELIGION", "Bebe Rexha")))
        .contains(URI.create("spotify:track:abc"));
  }

  @Test
  void findTrack_matchesArtistSeparatorVariants() {
    trackCache.storeTrack(
        spotify("Stay", List.of("Leony", "Calum Scott"), "spotify:track:def"));

    assertThat(trackCache.findTrack(new Track("Stay", "LEONY & CALUM SCOTT")))
        .contains(URI.create("spotify:track:def"));
    assertThat(trackCache.findTrack(new Track("Stay", "Leony, Calum Scott")))
        .contains(URI.create("spotify:track:def"));
    assertThat(trackCache.findTrack(new Track("Stay", "Leony x Calum Scott")))
        .contains(URI.create("spotify:track:def"));
  }

  @Test
  void findTrack_matchesFeatVariants() {
    trackCache.storeTrack(
        spotify("Better Days", List.of("Glockenbach", "Declan J Donovan"), "spotify:track:ghi"));

    assertThat(trackCache.findTrack(new Track("Better Days", "Glockenbach feat. Declan J Donovan")))
        .contains(URI.create("spotify:track:ghi"));
    assertThat(trackCache.findTrack(new Track("Better Days", "Glockenbach feat Declan J Donovan")))
        .contains(URI.create("spotify:track:ghi"));
    assertThat(
            trackCache.findTrack(
                new Track("Better Days", "Glockenbach featuring Declan J Donovan")))
        .contains(URI.create("spotify:track:ghi"));
  }

  @Test
  void findTrack_matchesBandNameWithAmpersand() {
    trackCache.storeTrack(
        spotify("The Sound of Silence", List.of("Simon & Garfunkel"), "spotify:track:jkl"));

    assertThat(
            trackCache.findTrack(new Track("The Sound of Silence", "Simon & Garfunkel")))
        .contains(URI.create("spotify:track:jkl"));
  }

  @Test
  void findTrack_doesNotMatchDifferentSong() {
    trackCache.storeTrack(spotify("New Religion", "Bebe Rexha", "spotify:track:abc"));

    assertThat(trackCache.findTrack(new Track("Old Religion", "Bebe Rexha"))).isEmpty();
    assertThat(trackCache.findTrack(new Track("New Religion", "Other Artist"))).isEmpty();
  }

  @Test
  void storeTrack_replacesExistingEntry() {
    trackCache.storeTrack(spotify("Song", "Artist", "spotify:track:1111111111111111"));
    trackCache.storeTrack(spotify("Song", "Artist", "spotify:track:2222222222222222"));

    assertThat(trackCache.findTrack(new Track("Song", "Artist")))
        .contains(URI.create("spotify:track:2222222222222222"));
    assertThat(trackCache.getCacheSize()).isEqualTo(1);
  }

  @Test
  void getCacheSize_returnsCorrectCount() {
    assertThat(trackCache.getCacheSize()).isEqualTo(0);

    trackCache.storeTrack(spotify("Song 1", "Artist 1", "spotify:track:1111111111111111"));
    assertThat(trackCache.getCacheSize()).isEqualTo(1);

    trackCache.storeTrack(spotify("Song 2", "Artist 2", "spotify:track:2222222222222222"));
    assertThat(trackCache.getCacheSize()).isEqualTo(2);
  }

  @Test
  void clearCache_removesAllEntries() {
    trackCache.storeTrack(spotify("Song 1", "Artist 1", "spotify:track:1111111111111111"));
    trackCache.storeTrack(spotify("Song 2", "Artist 2", "spotify:track:2222222222222222"));

    trackCache.clearCache();

    assertThat(trackCache.getCacheSize()).isEqualTo(0);
    assertThat(trackCache.findTrack(new Track("Song 1", "Artist 1"))).isEmpty();
  }

  @Test
  void cache_persistsAcrossInstances() {
    var uri = URI.create("spotify:track:1234567890abcdef");
    trackCache.storeTrack(spotify("Test Song", "Test Artist", "spotify:track:1234567890abcdef"));

    var newCache = new TrackCache(tempDbPath.toString());
    assertThat(newCache.findTrack(new Track("Test Song", "Test Artist"))).contains(uri);
    assertThat(newCache.getCacheSize()).isEqualTo(1);
  }

  @Test
  void cache_handlesSpecialCharacters() {
    var uri = URI.create("spotify:track:special123456789");
    trackCache.storeTrack(
        spotify(
            "Song with \"quotes\" & symbols!",
            "Artist with 'apostrophe'",
            "spotify:track:special123456789"));

    assertThat(
            trackCache.findTrack(
                new Track("Song with \"quotes\" & symbols!", "Artist with 'apostrophe'")))
        .contains(uri);
  }
}
