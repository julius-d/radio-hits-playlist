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
            new SoundgraphSong(
                URI.create("spotify:track:track1"), false, "Track 1", List.of("Artist 1")),
            new SoundgraphSong(
                URI.create("spotify:track:track2"), false, "Track 2", List.of("Artist 2")),
            new SoundgraphSong(
                URI.create("spotify:track:track3"), false, "Track 3", List.of("Artist 3")),
            new SoundgraphSong(
                URI.create("spotify:track:track4"), false, "Track 4", List.of("Artist 4")),
            new SoundgraphSong(
                URI.create("spotify:track:track5"), false, "Track 5", List.of("Artist 5")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> limitedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.LimitStep(3)))));

    // then
    assertThat(limitedTracks)
        .hasSize(3)
        .extracting(SoundgraphSong::uri)
        .containsExactly(
            URI.create("spotify:track:track1"),
            URI.create("spotify:track:track2"),
            URI.create("spotify:track:track3"));
  }

  @Test
  void shouldHandleLimitGreaterThanInputSize() throws Exception {
    // given
    List<SoundgraphSong> inputTracks =
        List.of(
            new SoundgraphSong(
                URI.create("spotify:track:track1"), false, "Track 1", List.of("Artist 1")),
            new SoundgraphSong(
                URI.create("spotify:track:track2"), false, "Track 2", List.of("Artist 2")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> limitedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.LimitStep(5)))));

    // then
    assertThat(limitedTracks)
        .hasSize(2)
        .extracting(SoundgraphSong::uri)
        .containsExactly(URI.create("spotify:track:track1"), URI.create("spotify:track:track2"));
  }

  @Test
  void shouldFilterOutExplicitTracks() throws Exception {
    // given
    List<SoundgraphSong> inputTracks =
        List.of(
            new SoundgraphSong(
                URI.create("spotify:track:track1"),
                true,
                "Explicit Track 1",
                List.of("Artist 1")), // explicit
            new SoundgraphSong(
                URI.create("spotify:track:track2"),
                false,
                "Clean Track 2",
                List.of("Artist 2")), // non-explicit
            new SoundgraphSong(
                URI.create("spotify:track:track3"),
                true,
                "Explicit Track 3",
                List.of("Artist 3")), // explicit
            new SoundgraphSong(
                URI.create("spotify:track:track4"),
                false,
                "Clean Track 4",
                List.of("Artist 4"))); // non-explicit

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> filteredTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.FilterOutExplicitStep()))));

    // then
    assertThat(filteredTracks)
        .hasSize(2)
        .extracting(SoundgraphSong::uri)
        .containsExactly(URI.create("spotify:track:track2"), URI.create("spotify:track:track4"));
  }

  @Test
  void shouldCombineTracksFromMultipleSources() throws Exception {
    // given
    List<SoundgraphSong> source1Tracks =
        List.of(
            new SoundgraphSong(
                URI.create("spotify:track:source1_track1"),
                false,
                "Source 1 Track 1",
                List.of("Artist 1")),
            new SoundgraphSong(
                URI.create("spotify:track:source1_track2"),
                false,
                "Source 1 Track 2",
                List.of("Artist 2")));

    List<SoundgraphSong> source2Tracks =
        List.of(
            new SoundgraphSong(
                URI.create("spotify:track:source2_track1"),
                false,
                "Source 2 Track 1",
                List.of("Artist 3")),
            new SoundgraphSong(
                URI.create("spotify:track:source2_track2"),
                false,
                "Source 2 Track 2",
                List.of("Artist 4")),
            new SoundgraphSong(
                URI.create("spotify:track:source2_track3"),
                false,
                "Source 2 Track 3",
                List.of("Artist 5")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_1")).thenReturn(source1Tracks);
    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_2")).thenReturn(source2Tracks);

    // when
    List<SoundgraphSong> combinedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
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
            URI.create("spotify:track:source1_track1"),
            URI.create("spotify:track:source2_track1"),
            URI.create("spotify:track:source1_track2"),
            URI.create("spotify:track:source2_track2"),
            URI.create("spotify:track:source2_track3"));
  }

  @Test
  void shouldShuffleTracks() throws Exception {
    // given
    List<SoundgraphSong> inputTracks =
        List.of(
            new SoundgraphSong(
                URI.create("spotify:track:track1"), false, "Track 1", List.of("Artist 1")),
            new SoundgraphSong(
                URI.create("spotify:track:track2"), false, "Track 2", List.of("Artist 2")),
            new SoundgraphSong(
                URI.create("spotify:track:track3"), false, "Track 3", List.of("Artist 3")),
            new SoundgraphSong(
                URI.create("spotify:track:track4"), false, "Track 4", List.of("Artist 4")),
            new SoundgraphSong(
                URI.create("spotify:track:track5"), false, "Track 5", List.of("Artist 5")));

    when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id")).thenReturn(inputTracks);

    // when
    List<SoundgraphSong> shuffledTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadPlaylistStep(
                            "source_playlist_id", "Test Playlist"),
                        new SoundgraphConfig.ShuffleStep()))));

    // then
    assertThat(shuffledTracks)
        .hasSize(5)
        .extracting(SoundgraphSong::uri)
        .containsExactlyInAnyOrder(
            URI.create("spotify:track:track1"),
            URI.create("spotify:track:track2"),
            URI.create("spotify:track:track3"),
            URI.create("spotify:track:track4"),
            URI.create("spotify:track:track5"));
  }

  @Test
  void shouldRemoveDuplicateTracksFromAlbum() throws Exception {
    // given
    List<SoundgraphSong> albumTracks =
        List.of(
            new SoundgraphSong(
                URI.create("spotify:track:track1"),
                false,
                "Album Track 1",
                List.of("Album Artist")),
            new SoundgraphSong(
                URI.create("spotify:track:track1"),
                false,
                "Album Track 1",
                List.of("Album Artist")), // duplicate
            new SoundgraphSong(
                URI.create("spotify:track:track2"),
                false,
                "Album Track 2",
                List.of("Album Artist")),
            new SoundgraphSong(
                URI.create("spotify:track:track3"),
                false,
                "Album Track 3",
                List.of("Album Artist")),
            new SoundgraphSong(
                URI.create("spotify:track:track3"),
                false,
                "Album Track 3",
                List.of("Album Artist"))); // duplicate

    when(soundgraphSpotifyWrapper.getAlbumTracks("source_album_id")).thenReturn(albumTracks);

    // when
    List<SoundgraphSong> dedupedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadAlbumStep("source_album_id", "Test Album"),
                        new SoundgraphConfig.DedupStep()))));

    // then
    assertThat(dedupedTracks)
        .hasSize(3)
        .extracting(SoundgraphSong::uri)
        .containsExactly(
            URI.create("spotify:track:track1"),
            URI.create("spotify:track:track2"),
            URI.create("spotify:track:track3"));
  }

  @Test
  void shouldLoadArtistTopTracks() throws Exception {
    // given
    List<SoundgraphSong> artistTracks =
        List.of(
            new SoundgraphSong(
                URI.create("spotify:track:artist_track1"),
                false,
                "Top Track 1",
                List.of("Test Artist")),
            new SoundgraphSong(
                URI.create("spotify:track:artist_track2"),
                true,
                "Top Track 2",
                List.of("Test Artist")),
            new SoundgraphSong(
                URI.create("spotify:track:artist_track3"),
                false,
                "Top Track 3",
                List.of("Test Artist")));

    when(soundgraphSpotifyWrapper.getArtistTopTracks("artist_id")).thenReturn(artistTracks);

    // when
    List<SoundgraphSong> loadedTracks =
        soundgraphService.processSoundgraphConfig(
            new SoundgraphConfig(
                "Test Configuration",
                "target_playlist_id",
                new SoundgraphConfig.Pipe(
                    List.of(
                        new SoundgraphConfig.LoadArtistTopTracksStep(
                            "artist_id", "Test Artist")))));

    // then
    assertThat(loadedTracks)
        .hasSize(3)
        .extracting(SoundgraphSong::uri)
        .containsExactly(
            URI.create("spotify:track:artist_track1"),
            URI.create("spotify:track:artist_track2"),
            URI.create("spotify:track:artist_track3"));
  }

  @Test
  void shouldFilterArtistsFromDenylist() throws Exception {
    // given
    List<SoundgraphSong> mainPlaylistTracks =
        List.of(
            new SoundgraphSong(
                URI.create("spotify:track:track1"), false, "Track 1", List.of("Artist 1")),
            new SoundgraphSong(
                URI.create("spotify:track:track2"), false, "Track 2", List.of("Artist 2")),
            new SoundgraphSong(
                URI.create("spotify:track:track3"), false, "Track 3", List.of("Artist 3")),
            new SoundgraphSong(
                URI.create("spotify:track:track4"),
                false,
                "Track 4",
                List.of("Artist 1", "Artist 4")), // multi-artist with denied artist
            new SoundgraphSong(
                URI.create("spotify:track:track5"), false, "Track 5", List.of("Artist 5")));

    List<SoundgraphSong> denylistTracks =
        List.of(
            new SoundgraphSong(
                URI.create("spotify:track:denied1"), false, "Denied Track 1", List.of("Artist 1")),
            new SoundgraphSong(
                URI.create("spotify:track:denied2"), false, "Denied Track 2", List.of("Artist 3")));

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
        .containsExactly(URI.create("spotify:track:track2"), URI.create("spotify:track:track5"));
  }

  @Test
  void shouldHandleEmptyDenylist() throws Exception {
    // given
    List<SoundgraphSong> mainPlaylistTracks =
        List.of(
            new SoundgraphSong(
                URI.create("spotify:track:track1"), false, "Track 1", List.of("Artist 1")),
            new SoundgraphSong(
                URI.create("spotify:track:track2"), false, "Track 2", List.of("Artist 2")));

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
        .containsExactly(URI.create("spotify:track:track1"), URI.create("spotify:track:track2"));
  }
}
