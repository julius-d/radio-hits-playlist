package com.github.juliusd.radiohitsplaylist.spotify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class RefreshTokenWriter {

  private static final Pattern REFRESH_TOKEN_LINE =
      Pattern.compile("^(\\s{2}refreshToken:\\s+)\\S+", Pattern.MULTILINE);

  public void updateRefreshToken(String configFilePath, String newRefreshToken) throws IOException {
    Path path = Path.of(configFilePath);
    String content = Files.readString(path);
    var matcher = REFRESH_TOKEN_LINE.matcher(content);
    if (!matcher.find()) {
      throw new IllegalStateException(
          "Could not find refreshToken entry in config file: " + configFilePath);
    }
    String updated = matcher.replaceFirst("$1" + newRefreshToken);
    Files.writeString(path, updated);
  }
}
