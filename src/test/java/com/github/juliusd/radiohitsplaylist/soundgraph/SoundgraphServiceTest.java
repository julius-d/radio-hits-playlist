package com.github.juliusd.radiohitsplaylist.soundgraph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.juliusd.radiohitsplaylist.config.SoundgraphConfig;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SoundgraphServiceTest {

  // Test track URI constants
  private static final URI TRACK_1_URI = URI.create("spotify:track:track1");
  private static final URI TRACK_2_URI = URI.create("spotify:track:track2");
  private static final URI TRACK_3_URI = URI.create("spotify:track:track3");
  private static final URI TRACK_4_URI = URI.create("spotify:track:track4");
  private static final URI TRACK_5_URI = URI.create("spotify:track:track5");

  // Source tracks for combination tests
  private static final URI SOURCE1_TRACK1_URI = URI.create("spotify:track:source1_track1");
  private static final URI SOURCE1_TRACK2_URI = URI.create("spotify:track:source1_track2");
  private static final URI SOURCE2_TRACK1_URI = URI.create("spotify:track:source2_track1");
  private static final URI SOURCE2_TRACK2_URI = URI.create("spotify:track:source2_track2");
  private static final URI SOURCE2_TRACK3_URI = URI.create("spotify:track:source2_track3");

  // Denied tracks for filtering tests
  private static final URI DENIED1_URI = URI.create("spotify:track:denied1");
  private static final URI DENIED2_URI = URI.create("spotify:track:denied2");

  private SoundgraphService soundgraphService;
  @Mock private SoundgraphSpotifyWrapper soundgraphSpotifyWrapper;

  @BeforeEach
  void setUp() {
    soundgraphService = new SoundgraphService(soundgraphSpotifyWrapper);
  }

  @Test
  void shouldProcessLimitStep() throws Exception {
    // given
    List<SoundgraphSong> inputTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Track 1", List.of("Artist 1")),
            new SoundgraphSong(TRACK_2_URI, false, "Track 2", List.of("Artist 2")),
            new SoundgraphSong(TRACK_3_URI, false, "Track 3", List.of("Artist 3")),
            new SoundgraphSong(TRACK_4_URI, false, "Track 4", List.of("Artist 4")),
            new SoundgraphSong(TRACK_5_URI, false, "Track 5", List.of("Artist 5")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> limitedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.LimitStep(3)))));

    // then
    assertThat(limitedTracks)
        .hasSize(3)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_1_URI, TRACK_2_URI, TRACK_3_URI);
  }

  @Test
  void shouldHandleLimitGreaterThanInputSize() throws Exception {
    // given
    List<SoundgraphSong> inputTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Track 1", List.of("Artist 1")),
            new SoundgraphSong(TRACK_2_URI, false, "Track 2", List.of("Artist 2")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> limitedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.LimitStep(5)))));

    // then
    assertThat(limitedTracks)
        .hasSize(2)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_1_URI, TRACK_2_URI);
  }

  @Test
  void shouldFilterOutExplicitTracks() throws Exception {
    // given
    List<SoundgraphSong> inputTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, true, "Explicit Track 1", List.of("Artist 1")),
            new SoundgraphSong(TRACK_2_URI, false, "Clean Track 2", List.of("Artist 2")),
            new SoundgraphSong(TRACK_3_URI, true, "Explicit Track 3", List.of("Artist 3")),
            new SoundgraphSong(TRACK_4_URI, false, "Clean Track 4", List.of("Artist 4")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> filteredTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.FilterOutExplicitStep()))));

    // then
    assertThat(filteredTracks)
        .hasSize(2)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_2_URI, TRACK_4_URI);
  }

  @Test
  void shouldCombineTracksFromMultipleSources() throws Exception {
    // given
    List<SoundgraphSong> source1Tracks =
        List.of(
            new SoundgraphSong(SOURCE1_TRACK1_URI, false, "Source 1 Track 1", List.of("Artist 1")),
            new SoundgraphSong(SOURCE1_TRACK2_URI, false, "Source 1 Track 2", List.of("Artist 2")));

    List<SoundgraphSong> source2Tracks =
        List.of(
            new SoundgraphSong(SOURCE2_TRACK1_URI, false, "Source 2 Track 1", List.of("Artist 3")),
            new SoundgraphSong(SOURCE2_TRACK2_URI, false, "Source 2 Track 2", List.of("Artist 4")),
            new SoundgraphSong(SOURCE2_TRACK3_URI, false, "Source 2 Track 3", List.of("Artist 5")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_1")).thenReturn(source1Tracks);
    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_2")).thenReturn(source2Tracks);

    // when
    List<SoundgraphSong> combinedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.CombineStep(
                            List.of(
                                new SoundgraphConfig.Pipe(
                                    List.of(
                                        new SoundgraphConfig.LoadPlaylistStep(
                                            "source_playlist_1", "Source Playlist 1"))),
                                new SoundgraphConfig.Pipe(
                                    List.of(
                                        new SoundgraphConfig.LoadPlaylistStep(
                                            "source_playlist_2", "Source Playlist 2")))))))));

    // then
    assertThat(combinedTracks)
        .hasSize(5)
        .extracting(SoundgraphSong::uri)
        .containsExactly(
            SOURCE1_TRACK1_URI,
            SOURCE2_TRACK1_URI,
            SOURCE1_TRACK2_URI,
            SOURCE2_TRACK2_URI,
            SOURCE2_TRACK3_URI);
  }

  @Test
  void shouldShuffleTracks() throws Exception {
    // given
    List<SoundgraphSong> inputTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Track 1", List.of("Artist 1")),
            new SoundgraphSong(TRACK_2_URI, false, "Track 2", List.of("Artist 2")),
            new SoundgraphSong(TRACK_3_URI, false, "Track 3", List.of("Artist 3")),
            new SoundgraphSong(TRACK_4_URI, false, "Track 4", List.of("Artist 4")),
            new SoundgraphSong(TRACK_5_URI, false, "Track 5", List.of("Artist 5")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> shuffledTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.ShuffleStep()))));

    // then
    assertThat(shuffledTracks)
        .hasSize(5)
        .extracting(SoundgraphSong::uri)
        .containsExactlyInAnyOrder(TRACK_1_URI, TRACK_2_URI, TRACK_3_URI, TRACK_4_URI, TRACK_5_URI);
  }

  @Test
  void shouldRemoveDuplicateTracksFromAlbum() throws Exception {
    // given
    List<SoundgraphSong> albumTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Album Track 1", List.of("Album Artist")),
            new SoundgraphSong(
                TRACK_1_URI, false, "Album Track 1", List.of("Album Artist")), // duplicate
            new SoundgraphSong(TRACK_2_URI, false, "Album Track 2", List.of("Album Artist")),
            new SoundgraphSong(TRACK_3_URI, false, "Album Track 3", List.of("Album Artist")),
            new SoundgraphSong(
                TRACK_3_URI, false, "Album Track 3", List.of("Album Artist"))); // duplicate

    when(soundgraphSpotifyWrapper.getAlbumTracks("source_album_id")).thenReturn(albumTracks);

    // when
    List<SoundgraphSong> dedupedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadAlbumStep("source_album_id", "Test Album"),
                        new SoundgraphConfig.DedupStep()))));

    // then
    assertThat(dedupedTracks)
        .hasSize(3)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_1_URI, TRACK_2_URI, TRACK_3_URI);
  }

  @Test
  void shouldLoadArtistTopTracks() throws Exception {
    // given
    List<SoundgraphSong> artistTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Top Track 1", List.of("Test Artist")),
            new SoundgraphSong(TRACK_2_URI, true, "Top Track 2", List.of("Test Artist")),
            new SoundgraphSong(TRACK_3_URI, false, "Top Track 3", List.of("Test Artist")));

    when(soundgraphSpotifyWrapper.getArtistTopTracks("artist_id")).thenReturn(artistTracks);

    // when
    List<SoundgraphSong> loadedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadArtistTopTracksStep(
                            "artist_id", "Test Artist")))));

    // then
    assertThat(loadedTracks)
        .hasSize(3)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_1_URI, TRACK_2_URI, TRACK_3_URI);
  }

  @Test
  void shouldLoadArtistNewestAlbumTracks() throws Exception {
    // given
    List<SoundgraphSong> albumTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Album Track 1", List.of("Test Artist")),
            new SoundgraphSong(TRACK_2_URI, true, "Album Track 2", List.of("Test Artist")),
            new SoundgraphSong(TRACK_3_URI, false, "Album Track 3", List.of("Test Artist")));

    when(soundgraphSpotifyWrapper.getArtistNewestAlbumTracks(
            "artist_id", List.of(AlbumType.ALBUM), List.of()))
        .thenReturn(albumTracks);

    // when
    List<SoundgraphSong> loadedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadArtistNewestAlbumStep(
                            "artist_id", "Test Artist", List.of(AlbumType.ALBUM), List.of())))));

    // then
    assertThat(loadedTracks)
        .hasSize(3)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_1_URI, TRACK_2_URI, TRACK_3_URI);
  }

  @Test
  void shouldFilterArtistsFromDenylist() throws Exception {
    // given
    List<SoundgraphSong> mainPlaylistTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Track 1", List.of("Artist 1")),
            new SoundgraphSong(TRACK_2_URI, false, "Track 2", List.of("Artist 2")),
            new SoundgraphSong(TRACK_3_URI, false, "Track 3", List.of("Artist 3")),
            new SoundgraphSong(
                TRACK_4_URI,
                false,
                "Track 4",
                List.of("Artist 1", "Artist 4")), // multi-artist with denied artist
            new SoundgraphSong(TRACK_5_URI, false, "Track 5", List.of("Artist 5")));

    List<SoundgraphSong> denylistTracks =
        List.of(
            new SoundgraphSong(DENIED1_URI, false, "Denied Track 1", List.of("Artist 1")),
            new SoundgraphSong(DENIED2_URI, false, "Denied Track 2", List.of("Artist 3")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("main_playlist_id"))
        .thenReturn(mainPlaylistTracks);
    when(soundgraphSpotifyWrapper.getPlaylistTracks("denylist_playlist_id"))
        .thenReturn(denylistTracks);

    // when
    List<SoundgraphSong> filteredTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep("main_playlist_id", "Main Playlist"),
                        new SoundgraphConfig.FilterArtistsFromStep(
                            new SoundgraphConfig.Pipe(
                                List.of(
                                    new SoundgraphConfig.LoadPlaylistStep(
                                        "denylist_playlist_id", "Denylist Playlist"))))))));

    // then
    assertThat(filteredTracks)
        .hasSize(2)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_2_URI, TRACK_5_URI);
  }

  @Test
  void shouldHandleEmptyDenylist() throws Exception {
    // given
    List<SoundgraphSong> mainPlaylistTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Track 1", List.of("Artist 1")),
            new SoundgraphSong(TRACK_2_URI, false, "Track 2", List.of("Artist 2")));

    List<SoundgraphSong> emptyDenylist = List.of();

    when(soundgraphSpotifyWrapper.getPlaylistTracks("main_playlist_id"))
        .thenReturn(mainPlaylistTracks);
    when(soundgraphSpotifyWrapper.getPlaylistTracks("empty_denylist_id")).thenReturn(emptyDenylist);

    // when
    List<SoundgraphSong> filteredTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep("main_playlist_id", "Main Playlist"),
                        new SoundgraphConfig.FilterArtistsFromStep(
                            new SoundgraphConfig.Pipe(
                                List.of(
                                    new SoundgraphConfig.LoadPlaylistStep(
                                        "empty_denylist_id", "Empty Denylist"))))))));

    // then
    assertThat(filteredTracks)
        .hasSize(2)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_1_URI, TRACK_2_URI);
  }

  @Test
  void shouldSeparateConsecutiveTracksFromSameArtist() throws Exception {
    // given - tracks with consecutive songs from same artists
    List<SoundgraphSong> inputTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Track 1", List.of("Artist A")),
            new SoundgraphSong(TRACK_2_URI, false, "Track 2", List.of("Artist A")),
            new SoundgraphSong(TRACK_3_URI, false, "Track 3", List.of("Artist B")),
            new SoundgraphSong(TRACK_4_URI, false, "Track 4", List.of("Artist C")),
            new SoundgraphSong(TRACK_5_URI, false, "Track 5", List.of("Artist A")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> separatedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.ArtistSeparationStep()))));

    // then - verify no consecutive tracks from same artist
    assertThat(separatedTracks).hasSize(5);
    for (int i = 0; i < separatedTracks.size() - 1; i++) {
      SoundgraphSong current = separatedTracks.get(i);
      SoundgraphSong next = separatedTracks.get(i + 1);

      // Check that no consecutive tracks share artists
      boolean sharesArtist =
          current.artists().stream().anyMatch(artist -> next.artists().contains(artist));
      assertThat(sharesArtist)
          .as(
              "Track %d (%s) and track %d (%s) should not share artists",
              i, current.artists(), i + 1, next.artists())
          .isFalse();
    }
  }

  @Test
  void shouldHandleMultiArtistTracksInSeparation() throws Exception {
    // given - tracks with multiple artists
    List<SoundgraphSong> inputTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Track 1", List.of("Artist A", "Artist B")),
            new SoundgraphSong(TRACK_2_URI, false, "Track 2", List.of("Artist A", "Artist C")),
            new SoundgraphSong(TRACK_3_URI, false, "Track 3", List.of("Artist D")),
            new SoundgraphSong(TRACK_4_URI, false, "Track 4", List.of("Artist E")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> separatedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.ArtistSeparationStep()))));

    // then
    noConsecutiveTracks(separatedTracks);
    assertThat(separatedTracks)
        .hasSize(4)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_1_URI, TRACK_3_URI, TRACK_2_URI, TRACK_4_URI);
  }

  @Test
  void shouldHandleSameArtistAtEndInSeparation() throws Exception {
    // given - tracks with multiple artists
    List<SoundgraphSong> inputTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Track 1", List.of("Artist A")),
            new SoundgraphSong(TRACK_2_URI, false, "Track 2", List.of("Artist B")),
            new SoundgraphSong(TRACK_3_URI, false, "Track 3", List.of("Artist C")),
            new SoundgraphSong(TRACK_4_URI, false, "Track 4", List.of("Artist C")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> separatedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.ArtistSeparationStep()))));

    // then
    noConsecutiveTracks(separatedTracks);
    assertThat(separatedTracks)
        .hasSize(4)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_1_URI, TRACK_3_URI, TRACK_2_URI, TRACK_4_URI);
  }

  private void noConsecutiveTracks(List<SoundgraphSong> separatedTracks) {
    for (int i = 0; i < separatedTracks.size() - 1; i++) {
      SoundgraphSong current = separatedTracks.get(i);
      SoundgraphSong next = separatedTracks.get(i + 1);

      boolean sharesArtist =
          current.artists().stream().anyMatch(artist -> next.artists().contains(artist));
      assertThat(sharesArtist).isFalse();
    }
  }

  @Test
  void shouldHandleEmptyListInArtistSeparation() throws Exception {
    // given
    List<SoundgraphSong> emptyTracks = List.of();
    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(emptyTracks);

    // when
    List<SoundgraphSong> separatedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.ArtistSeparationStep()))));

    // then
    assertThat(separatedTracks).isEmpty();
  }

  @Test
  void shouldHandleSingleTrackInArtistSeparation() throws Exception {
    // given
    List<SoundgraphSong> singleTrack =
        List.of(new SoundgraphSong(TRACK_1_URI, false, "Track 1", List.of("Artist A")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(singleTrack);

    // when
    List<SoundgraphSong> separatedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.ArtistSeparationStep()))));

    // then
    assertThat(separatedTracks).hasSize(1);
    assertThat(separatedTracks.get(0).uri()).isEqualTo(TRACK_1_URI);
  }

  @Test
  void shouldHandleAllTracksFromSameArtist() throws Exception {
    // given - all tracks from same artist (should just return as is)
    List<SoundgraphSong> sameArtistTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Track 1", List.of("Artist A")),
            new SoundgraphSong(TRACK_2_URI, false, "Track 2", List.of("Artist A")),
            new SoundgraphSong(TRACK_3_URI, false, "Track 3", List.of("Artist A")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id"))
        .thenReturn(sameArtistTracks);

    // when
    List<SoundgraphSong> separatedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.ArtistSeparationStep()))));

    // then - should maintain all tracks even if from same artist
    assertThat(separatedTracks)
        .hasSize(3)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_1_URI, TRACK_2_URI, TRACK_3_URI);
  }

  @Test
  void shouldNotChangeOrderWhenTracksAlreadyFromDifferentArtists() throws Exception {
    // given - tracks already from different artists
    List<SoundgraphSong> inputTracks =
        List.of(
            new SoundgraphSong(TRACK_1_URI, false, "Track 1", List.of("Artist A")),
            new SoundgraphSong(TRACK_2_URI, false, "Track 2", List.of("Artist B")),
            new SoundgraphSong(TRACK_3_URI, false, "Track 3", List.of("Artist C")),
            new SoundgraphSong(TRACK_4_URI, false, "Track 4", List.of("Artist D")),
            new SoundgraphSong(TRACK_5_URI, false, "Track 5", List.of("Artist E")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> separatedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                "Test Description",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.ArtistSeparationStep()))));

    // then - order should remain exactly the same
    assertThat(separatedTracks)
        .hasSize(5)
        .extracting(SoundgraphSong::uri)
        .containsExactly(TRACK_1_URI, TRACK_2_URI, TRACK_3_URI, TRACK_4_URI, TRACK_5_URI);
  }
}
