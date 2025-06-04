package com.github.juliusd.radiohitsplaylist.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigLoaderTest {

  @TempDir
  private Path tempDirectory;

  @Test
  void loadsFullConfig() throws IOException {
    Path path = givenConfig(
      //language=yaml
      """
      ---
      spotify:
        refreshToken: myRefreshToken
        clientId: myClientId
        clientSecret: myClientSecret
      shuffleTasks:
        - playlistId: myPlaylistId0001
        - playlistId: myPlaylistId0002
        - playlistId: myPlaylistId0003
      reCreateFamilyRadioPlaylistTasks:
        - playlistId: targetPlaylistId4
          streamName: myStream1
          descriptionPrefix: my prefix
        - playlistId: targetPlaylistId5
          streamName: myStream2
          descriptionPrefix: my other prefix
      reCreateBerlinHitRadioPlaylistTasks:
        - playlistId: targetPlaylistId6
          streamName: myHitStream1
          descriptionPrefix: my prefix2
        - playlistId: targetPlaylistId7
          streamName: myHitStream2
          descriptionPrefix: my other prefix2
      bundesmuxUrl: https://example.org/b
      reCreateBundesmuxPlaylistTasks:
        - playlistId: targetPlaylistId8
          streamName: myBundesStream1
          descriptionPrefix: my prefix7
        - playlistId: targetPlaylistId9
          streamName: myBundesStream2
          descriptionPrefix: my other prefix7
      soundgraphTasks:
        - targetPlaylist: targetPlaylistId10
          pipe:
            steps:
              - type: combine
                sources:
                  - steps:
                      - type: loadPlaylist
                        playlistId: source_playlist_1
                      - type: shuffle
                      - type: limit
                        value: 100
                  - steps:
                      - type: loadAlbum
                        albumId: source_album_1
                      - type: shuffle
                      - type: limit
                        value: 50
              - type: shuffle
              - type: limit
                value: 150
      gotify:
       notifyOnSuccess: false
       notifyOnFailure: true
       gotifyUrl: https://example.org/gotify
       gotifyApiToken: myApiToken
      """);

    Configuration configuration = new ConfigLoader().loadConfig(path.toString());
    
    // Create expected SoundgraphConfig
    var soundgraphConfig = new SoundgraphConfig(
        "targetPlaylistId10",
        new SoundgraphConfig.Pipe(List.of(
            new SoundgraphConfig.CombineStep(
                List.of(
                    new SoundgraphConfig.Pipe(List.of(
                        new SoundgraphConfig.LoadPlaylistStep("source_playlist_1"),
                        new SoundgraphConfig.ShuffleStep(),
                        new SoundgraphConfig.LimitStep(100)
                    )),
                    new SoundgraphConfig.Pipe(List.of(
                        new SoundgraphConfig.LoadAlbumStep("source_album_1"),
                        new SoundgraphConfig.ShuffleStep(),
                        new SoundgraphConfig.LimitStep(50)
                    ))
                )
            ),
            new SoundgraphConfig.ShuffleStep(),
            new SoundgraphConfig.LimitStep(150)
        ))
    );

    assertThat(configuration).isEqualTo(new Configuration(
            new SpotifyConfiguration("myRefreshToken", "myClientId", "myClientSecret"),
            List.of(
                    new ShuffleTaskConfiguration("myPlaylistId0001"),
                    new ShuffleTaskConfiguration("myPlaylistId0002"),
                    new ShuffleTaskConfiguration("myPlaylistId0003")
            ),
            List.of(
                    new ReCreateFamilyRadioPlaylistTaskConfiguration("myStream1", "targetPlaylistId4", "my prefix"),
                    new ReCreateFamilyRadioPlaylistTaskConfiguration("myStream2", "targetPlaylistId5", "my other prefix")
            ),
            List.of(
                    new ReCreateBerlinHitRadioPlaylistTaskConfiguration("myHitStream1", "targetPlaylistId6", "my prefix2"),
                    new ReCreateBerlinHitRadioPlaylistTaskConfiguration("myHitStream2", "targetPlaylistId7", "my other prefix2")
            ),
            "https://example.org/b",
            List.of(
                    new ReCreateBundesmuxPlaylistTaskConfiguration("myBundesStream1", "targetPlaylistId8", "my prefix7"),
                    new ReCreateBundesmuxPlaylistTaskConfiguration("myBundesStream2", "targetPlaylistId9", "my other prefix7")
            ),
            List.of(soundgraphConfig),
            new NotifierConfiguration(false, true, "https://example.org/gotify", "myApiToken")
    ));
  }

  @Test
  void notConfigureListsAreEmpty() throws IOException {
    Path path = givenConfig(
      //language=yaml
      """
      ---
      spotify:
        refreshToken: myRefreshToken
        clientId: myClientId
        clientSecret: myClientSecret
      """);

    Configuration configuration = new ConfigLoader().loadConfig(path.toString());
    assertThat(configuration.reCreateFamilyRadioPlaylistTasks()).isEqualTo(Collections.emptyList());
    assertThat(configuration.reCreateFamilyRadioPlaylistTasks()).isEqualTo(Collections.emptyList());
    assertThat(configuration.reCreateBundesmuxPlaylistTasks()).isEqualTo(Collections.emptyList());
    assertThat(configuration.shuffleTasks()).isEqualTo(Collections.emptyList());
    assertThat(configuration.soundgraphTasks()).isEqualTo(Collections.emptyList());
  }

  @Test
  void notifierConfigCanBeEmpty() throws IOException {
    Path path = givenConfig(
      //language=yaml
      """
      ---
      spotify:
        refreshToken: myRefreshToken
        clientId: myClientId
        clientSecret: myClientSecret
      """);

    Configuration configuration = new ConfigLoader().loadConfig(path.toString());
    assertThat(configuration.gotify()).isNull();
  }

  private Path givenConfig(String config) throws IOException {
    Path path = Files.createFile(tempDirectory.resolve("test.yaml"));
    Files.write(path, config.getBytes());
    return path.toAbsolutePath();
  }
}
