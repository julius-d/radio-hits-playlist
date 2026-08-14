package com.github.juliusd.radiohitsplaylist.spotify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
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
    String updated = matcher.replaceFirst("$1" + Matcher.quoteReplacement(newRefreshToken));
    Path tmp = Files.createTempFile(path.getParent(), ".config-", ".yaml.tmp");
    try {
      Files.writeString(tmp, updated);
      Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException e) {
      Files.deleteIfExists(tmp);
      throw e;
    }
  }
}
