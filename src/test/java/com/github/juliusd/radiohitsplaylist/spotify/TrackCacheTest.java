package com.github.juliusd.radiohitsplaylist.spotify;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.radiohitsplaylist.Track;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TrackCacheTest {

  @TempDir Path tempDir;

  private TrackCache trackCache;
  private Path tempDbPath;

  @BeforeEach
  void setUp() {
    tempDbPath = tempDir.resolve("test_track_cache.db");
    trackCache = new TrackCache(tempDbPath.toString());
  }

  @Test
  void findTrack_returnsEmptyWhenTrackNotInCache() {
    var track = new Track("Test Song", "Test Artist");

    var result = trackCache.findTrack(track);

    assertThat(result).isEmpty();
  }

  @Test
  void storeTrack_andFindTrack_returnsStoredUri() {
    var track = new Track("Test Song", "Test Artist");
    var spotifyUri = URI.create("spotify:track:1234567890abcdef");

    trackCache.storeTrack(track, spotifyUri);
    var result = trackCache.findTrack(track);

    assertThat(result).contains(spotifyUri);
  }

  @Test
  void findTrack_requiresExactMatch() {
    var originalTrack = new Track("Test Song", "Test Artist");
    var spotifyUri = URI.create("spotify:track:1234567890abcdef");
    trackCache.storeTrack(originalTrack, spotifyUri);

    // Different artist
    var differentArtist = new Track("Test Song", "Different Artist");
    assertThat(trackCache.findTrack(differentArtist)).isEmpty();

    // Different title
    var differentTitle = new Track("Different Song", "Test Artist");
    assertThat(trackCache.findTrack(differentTitle)).isEmpty();

    // Case sensitivity
    var differentCase = new Track("test song", "test artist");
    assertThat(trackCache.findTrack(differentCase)).isEmpty();

    // Exact match should still work
    assertThat(trackCache.findTrack(originalTrack)).isPresent();
  }

  @Test
  void storeTrack_replacesExistingEntry() {
    var track = new Track("Test Song", "Test Artist");
    var firstUri = URI.create("spotify:track:1111111111111111");
    var secondUri = URI.create("spotify:track:2222222222222222");

    // Store first URI
    trackCache.storeTrack(track, firstUri);
    assertThat(trackCache.findTrack(track)).contains(firstUri);

    // Store second URI for same track (should replace)
    trackCache.storeTrack(track, secondUri);
    assertThat(trackCache.findTrack(track)).contains(secondUri);

    // Cache size should still be 1
    assertThat(trackCache.getCacheSize()).isEqualTo(1);
  }

  @Test
  void getCacheSize_returnsCorrectCount() {
    assertThat(trackCache.getCacheSize()).isEqualTo(0);

    var track1 = new Track("Song 1", "Artist 1");
    var track2 = new Track("Song 2", "Artist 2");
    var uri1 = URI.create("spotify:track:1111111111111111");
    var uri2 = URI.create("spotify:track:2222222222222222");

    trackCache.storeTrack(track1, uri1);
    assertThat(trackCache.getCacheSize()).isEqualTo(1);

    trackCache.storeTrack(track2, uri2);
    assertThat(trackCache.getCacheSize()).isEqualTo(2);
  }

  @Test
  void clearCache_removesAllEntries() {
    var track1 = new Track("Song 1", "Artist 1");
    var track2 = new Track("Song 2", "Artist 2");
    var uri1 = URI.create("spotify:track:1111111111111111");
    var uri2 = URI.create("spotify:track:2222222222222222");

    trackCache.storeTrack(track1, uri1);
    trackCache.storeTrack(track2, uri2);
    assertThat(trackCache.getCacheSize()).isEqualTo(2);

    trackCache.clearCache();

    assertThat(trackCache.getCacheSize()).isEqualTo(0);
    assertThat(trackCache.findTrack(track1)).isEmpty();
    assertThat(trackCache.findTrack(track2)).isEmpty();
  }

  @Test
  void cache_persistsAcrossInstances() {
    var track = new Track("Test Song", "Test Artist");
    var spotifyUri = URI.create("spotify:track:1234567890abcdef");

    // Store in first instance
    trackCache.storeTrack(track, spotifyUri);
    assertThat(trackCache.findTrack(track)).contains(spotifyUri);

    // Create new instance with same database file
    var newCache = new TrackCache(tempDbPath.toString());
    assertThat(newCache.findTrack(track)).contains(spotifyUri);
    assertThat(newCache.getCacheSize()).isEqualTo(1);
  }

  @Test
  void cache_handlesSpecialCharacters() {
    var track = new Track("Song with \"quotes\" & symbols!", "Artist with 'apostrophe' & äöüèéê");
    var spotifyUri = URI.create("spotify:track:special123456789");

    trackCache.storeTrack(track, spotifyUri);
    var result = trackCache.findTrack(track);

    assertThat(result).contains(spotifyUri);
  }

  @Test
  void cache_handlesUnicodeCharacters() {
    var track = new Track("Müsik mit Ümlauts", "Künstler with 中文");
    var spotifyUri = URI.create("spotify:track:unicode123456789");

    trackCache.storeTrack(track, spotifyUri);
    Optional<URI> result = trackCache.findTrack(track);

    assertThat(result).contains(spotifyUri);
  }
}
