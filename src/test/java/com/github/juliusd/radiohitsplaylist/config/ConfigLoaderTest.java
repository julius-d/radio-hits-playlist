package com.github.juliusd.radiohitsplaylist.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigLoaderTest {

  @TempDir
  private Path tempDirectory;

  @Test
  void load() throws IOException {
    Path path = Files.createFile(tempDirectory.resolve("test.yaml"));
    //language=yaml
    String str =
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
        """;

    Files.write(path, str.getBytes());


    Configuration configuration = new ConfigLoader().loadConfig(path.toAbsolutePath().toString());
    assertEquals(configuration, new Configuration(
      new SpotifyConfiguration("myRefreshToken", "myClientId", "myClientSecret"),
      List.of(
        new ShuffleTaskConfiguration("myPlaylistId0001"),
        new ShuffleTaskConfiguration("myPlaylistId0002"),
        new ShuffleTaskConfiguration("myPlaylistId0003")
      )));
  }
}
