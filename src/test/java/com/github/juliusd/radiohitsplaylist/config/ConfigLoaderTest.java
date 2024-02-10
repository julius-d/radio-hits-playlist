package com.github.juliusd.radiohitsplaylist.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
         clientSecret: myClientSecret
       """;

    Files.write(path, str.getBytes());


    Configuration configuration = new ConfigLoader().loadConfig(path.toAbsolutePath().toString());
    Assertions.assertEquals(configuration, new Configuration(new SpotifyConfiguration("myRefreshToken", "myClientSecret")));
  }
}
