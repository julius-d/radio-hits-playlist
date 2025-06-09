package com.github.juliusd.radiohitsplaylist.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.juliusd.radiohitsplaylist.soundgraph.AlbumType;
import org.junit.jupiter.api.Test;

class SoundgraphConfigTest {

  @Test
  void shouldParseYamlConfig() throws Exception {
    // given
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    String yamlConfig =
        """
            name: "Test Configuration"
            targetPlaylistId: "target_playlist_id"
            descriptionPrefix: "Test Description"
            pipe:
              steps:
                - type: combine
                  sources:
                    - steps:
                        - type: loadPlaylist
                          playlistId: source_playlist_1
                          name: "My Playlist"
                        - type: shuffle
                        - type: limit
                          value: 100
                    - steps:
                        - type: loadAlbum
                          albumId: source_album_1
                          name: "My Album"
                        - type: shuffle
                        - type: limit
                          value: 50
                - type: shuffle
                - type: limit
                  value: 150
            """;

    // when
    SoundgraphConfig config = mapper.readValue(yamlConfig, SoundgraphConfig.class);

    // then
    assertThat(config.name()).isEqualTo("Test Configuration");
    assertThat(config.targetPlaylistId()).isEqualTo("target_playlist_id");
    assertThat(config.descriptionPrefix()).isEqualTo("Test Description");
    assertThat(config.pipe().steps()).hasSize(3);

    // Verify first step (combine)
    SoundgraphConfig.Step firstStep = config.pipe().steps().get(0);
    assertThat(firstStep).isInstanceOf(SoundgraphConfig.CombineStep.class);
    SoundgraphConfig.CombineStep combineStep = (SoundgraphConfig.CombineStep) firstStep;
    assertThat(combineStep.sources()).hasSize(2);

    // Verify first source (playlist)
    SoundgraphConfig.Pipe firstSource = combineStep.sources().get(0);
    assertThat(firstSource.steps()).hasSize(3);
    assertThat(firstSource.steps().get(0)).isInstanceOf(SoundgraphConfig.LoadPlaylistStep.class);
    assertThat(((SoundgraphConfig.LoadPlaylistStep) firstSource.steps().get(0)).playlistId())
        .isEqualTo("source_playlist_1");
    assertThat(((SoundgraphConfig.LoadPlaylistStep) firstSource.steps().get(0)).name())
        .isEqualTo("My Playlist");
    assertThat(firstSource.steps().get(1)).isInstanceOf(SoundgraphConfig.ShuffleStep.class);
    assertThat(firstSource.steps().get(2)).isInstanceOf(SoundgraphConfig.LimitStep.class);
    assertThat(((SoundgraphConfig.LimitStep) firstSource.steps().get(2)).value()).isEqualTo(100);

    // Verify second source (album)
    SoundgraphConfig.Pipe secondSource = combineStep.sources().get(1);
    assertThat(secondSource.steps()).hasSize(3);
    assertThat(secondSource.steps().get(0)).isInstanceOf(SoundgraphConfig.LoadAlbumStep.class);
    assertThat(((SoundgraphConfig.LoadAlbumStep) secondSource.steps().get(0)).albumId())
        .isEqualTo("source_album_1");
    assertThat(((SoundgraphConfig.LoadAlbumStep) secondSource.steps().get(0)).name())
        .isEqualTo("My Album");
    assertThat(secondSource.steps().get(1)).isInstanceOf(SoundgraphConfig.ShuffleStep.class);
    assertThat(secondSource.steps().get(2)).isInstanceOf(SoundgraphConfig.LimitStep.class);
    assertThat(((SoundgraphConfig.LimitStep) secondSource.steps().get(2)).value()).isEqualTo(50);

    // Verify second step (shuffle)
    SoundgraphConfig.Step secondStep = config.pipe().steps().get(1);
    assertThat(secondStep).isInstanceOf(SoundgraphConfig.ShuffleStep.class);

    // Verify third step (limit)
    SoundgraphConfig.Step thirdStep = config.pipe().steps().get(2);
    assertThat(thirdStep).isInstanceOf(SoundgraphConfig.LimitStep.class);
    assertThat(((SoundgraphConfig.LimitStep) thirdStep).value()).isEqualTo(150);
  }

  @Test
  void shouldParseArtistTopTracksConfig() throws Exception {
    // given
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    String yamlConfig =
        """
            name: "Artist Top Tracks Test"
            targetPlaylistId: "target_playlist_id"
            pipe:
              steps:
                - type: "loadArtistTopTracks"
                  artistId: "artist_id_1"
                  name: "Test Artist"
                - type: "shuffle"
                - type: "limit"
                  value: 10
            """;

    // when
    SoundgraphConfig config = mapper.readValue(yamlConfig, SoundgraphConfig.class);

    // then
    assertThat(config.name()).isEqualTo("Artist Top Tracks Test");
    assertThat(config.targetPlaylistId()).isEqualTo("target_playlist_id");
    assertThat(config.pipe().steps()).hasSize(3);

    // Verify first step (loadArtistTopTracks)
    SoundgraphConfig.Step firstStep = config.pipe().steps().get(0);
    assertThat(firstStep).isInstanceOf(SoundgraphConfig.LoadArtistTopTracksStep.class);
    SoundgraphConfig.LoadArtistTopTracksStep artistStep =
        (SoundgraphConfig.LoadArtistTopTracksStep) firstStep;
    assertThat(artistStep.artistId()).isEqualTo("artist_id_1");
    assertThat(artistStep.name()).isEqualTo("Test Artist");

    // Verify second step (shuffle)
    SoundgraphConfig.Step secondStep = config.pipe().steps().get(1);
    assertThat(secondStep).isInstanceOf(SoundgraphConfig.ShuffleStep.class);

    // Verify third step (limit)
    SoundgraphConfig.Step thirdStep = config.pipe().steps().get(2);
    assertThat(thirdStep).isInstanceOf(SoundgraphConfig.LimitStep.class);
    assertThat(((SoundgraphConfig.LimitStep) thirdStep).value()).isEqualTo(10);
  }

  @Test
  void shouldParseFilterArtistsFromConfig() throws Exception {
    // given
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    String yamlConfig =
        """
            name: "Filter Artists From Test"
            targetPlaylistId: "target_playlist_id"
            pipe:
              steps:
                - type: "loadPlaylist"
                  playlistId: "main_playlist_id"
                  name: "Main Playlist"
                - type: "filterArtistsFrom"
                  denylist:
                    steps:
                      - type: "loadPlaylist"
                        playlistId: "empty_denylist_id"
                        name: "Empty Denylist"
            """;

    // when
    SoundgraphConfig config = mapper.readValue(yamlConfig, SoundgraphConfig.class);

    // then
    assertThat(config.name()).isEqualTo("Filter Artists From Test");
    assertThat(config.targetPlaylistId()).isEqualTo("target_playlist_id");
    assertThat(config.pipe().steps()).hasSize(2);

    // Verify first step (loadPlaylist)
    SoundgraphConfig.Step firstStep = config.pipe().steps().get(0);
    assertThat(firstStep).isInstanceOf(SoundgraphConfig.LoadPlaylistStep.class);
    SoundgraphConfig.LoadPlaylistStep loadPlaylistStep =
        (SoundgraphConfig.LoadPlaylistStep) firstStep;
    assertThat(loadPlaylistStep.playlistId()).isEqualTo("main_playlist_id");
    assertThat(loadPlaylistStep.name()).isEqualTo("Main Playlist");

    // Verify second step (filterArtistsFrom)
    SoundgraphConfig.Step secondStep = config.pipe().steps().get(1);
    assertThat(secondStep).isInstanceOf(SoundgraphConfig.FilterArtistsFromStep.class);
    SoundgraphConfig.FilterArtistsFromStep filterArtistsFromStep =
        (SoundgraphConfig.FilterArtistsFromStep) secondStep;
    assertThat(filterArtistsFromStep.denylist().steps()).hasSize(1);
    assertThat(filterArtistsFromStep.denylist().steps().get(0))
        .isInstanceOf(SoundgraphConfig.LoadPlaylistStep.class);
    SoundgraphConfig.LoadPlaylistStep denylistStep =
        (SoundgraphConfig.LoadPlaylistStep) filterArtistsFromStep.denylist().steps().get(0);
    assertThat(denylistStep.playlistId()).isEqualTo("empty_denylist_id");
    assertThat(denylistStep.name()).isEqualTo("Empty Denylist");
  }

  @Test
  void shouldParseLoadArtistNewestAlbumConfigWithDefaultAlbumTypes() throws Exception {
    // given
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    String yamlConfig =
        """
            name: "Artist Newest Album Default Test"
            targetPlaylistId: "target_playlist_id"
            pipe:
              steps:
                - type: "loadArtistNewestAlbum"
                  artistId: "artist_id_1"
                  name: "Test Artist"
            """;

    // when
    SoundgraphConfig config = mapper.readValue(yamlConfig, SoundgraphConfig.class);

    // then
    assertThat(config.pipe().steps()).hasSize(1);

    // Verify step uses default album types
    SoundgraphConfig.Step step = config.pipe().steps().get(0);
    assertThat(step).isInstanceOf(SoundgraphConfig.LoadArtistNewestAlbumStep.class);
    SoundgraphConfig.LoadArtistNewestAlbumStep artistStep =
        (SoundgraphConfig.LoadArtistNewestAlbumStep) step;
    assertThat(artistStep.artistId()).isEqualTo("artist_id_1");
    assertThat(artistStep.name()).isEqualTo("Test Artist");
    assertThat(artistStep.albumTypes()).isNull();
  }

  @Test
  void shouldParseLoadArtistNewestAlbumConfig() throws Exception {
    // given
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    String yamlConfig =
        """
            name: "Artist Newest Album Valid Types Test"
            targetPlaylistId: "target_playlist_id"
            pipe:
              steps:
                - type: "loadArtistNewestAlbum"
                  artistId: "artist_id_1"
                  name: "Test Artist"
                  albumTypes: ["album", "single", "compilation", "appears_on"]
            """;

    // when
    SoundgraphConfig config = mapper.readValue(yamlConfig, SoundgraphConfig.class);

    // then
    assertThat(config.pipe().steps()).hasSize(1);

    // Verify step uses all valid album types
    SoundgraphConfig.Step step = config.pipe().steps().get(0);
    assertThat(step).isInstanceOf(SoundgraphConfig.LoadArtistNewestAlbumStep.class);
    SoundgraphConfig.LoadArtistNewestAlbumStep artistStep =
        (SoundgraphConfig.LoadArtistNewestAlbumStep) step;
    assertThat(artistStep.artistId()).isEqualTo("artist_id_1");
    assertThat(artistStep.name()).isEqualTo("Test Artist");
    assertThat(artistStep.albumTypes())
        .containsExactly(
            AlbumType.ALBUM, AlbumType.SINGLE, AlbumType.COMPILATION, AlbumType.APPEARS_ON);
  }
}
