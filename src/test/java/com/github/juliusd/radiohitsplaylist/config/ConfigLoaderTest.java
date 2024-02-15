package com.github.juliusd.radiohitsplaylist.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        """);


    Configuration configuration = new ConfigLoader().loadConfig(path.toString());
    assertEquals(configuration, new Configuration(
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
      )
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
    assertEquals(Collections.emptyList(), configuration.reCreateFamilyRadioPlaylistTasks());
    assertEquals(Collections.emptyList(), configuration.reCreateFamilyRadioPlaylistTasks());
    assertEquals(Collections.emptyList(), configuration.shuffleTasks());
  }

  private Path givenConfig(String config) throws IOException {
    Path path = Files.createFile(tempDirectory.resolve("test.yaml"));
    Files.write(path, config.getBytes());
    return path.toAbsolutePath();
  }
}
