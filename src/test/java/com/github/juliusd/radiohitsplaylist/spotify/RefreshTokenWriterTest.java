package com.github.juliusd.radiohitsplaylist.spotify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RefreshTokenWriterTest {

  @TempDir Path tempDir;

  private final RefreshTokenWriter writer = new RefreshTokenWriter();

  @Test
  void updatesRefreshTokenValue() throws IOException {
    Path config = writeConfig("---\nspotify:\n  refreshToken: oldToken123\n  clientId: abc\n");

    writer.updateRefreshToken(config.toString(), "newToken456");

    String result = Files.readString(config);
    assertThat(result).contains("  refreshToken: newToken456");
    assertThat(result).doesNotContain("oldToken123");
    assertThat(result).contains("  clientId: abc");
  }

  @Test
  void preservesOtherYamlContent() throws IOException {
    String yaml =
        "---\n"
            + "# my config\n"
            + "spotify:\n"
            + "  refreshToken: oldToken\n"
            + "  clientId: myId\n"
            + "  clientSecret: mySecret\n"
            + "shuffleTasks:\n"
            + "  - playlistId: abc123\n";
    Path config = writeConfig(yaml);

    writer.updateRefreshToken(config.toString(), "brandNewToken");

    String result = Files.readString(config);
    assertThat(result).contains("# my config");
    assertThat(result).contains("  clientId: myId");
    assertThat(result).contains("  clientSecret: mySecret");
    assertThat(result).contains("  - playlistId: abc123");
    assertThat(result).contains("  refreshToken: brandNewToken");
  }

  @Test
  void throwsWhenNoRefreshTokenLine() throws IOException {
    Path config = writeConfig("---\nspotify:\n  clientId: abc\n");

    assertThatThrownBy(() -> writer.updateRefreshToken(config.toString(), "newToken"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("refreshToken");
  }

  @Test
  void handlesTokenWithHyphensAndUnderscores() throws IOException {
    Path config =
        writeConfig("---\nspotify:\n  refreshToken: AQD_old-Token==\n  clientId: abc\n");

    writer.updateRefreshToken(config.toString(), "AQD_new-Token_with-special==");

    String result = Files.readString(config);
    assertThat(result).contains("  refreshToken: AQD_new-Token_with-special==");
    assertThat(result).doesNotContain("AQD_old-Token==");
  }

  @Test
  void handlesTokenWithRegexMetacharacters() throws IOException {
    Path config = writeConfig("---\nspotify:\n  refreshToken: oldToken\n  clientId: abc\n");

    writer.updateRefreshToken(config.toString(), "AQD$1suffix\\end");

    String result = Files.readString(config);
    assertThat(result).contains("  refreshToken: AQD$1suffix\\end");
    assertThat(result).doesNotContain("oldToken");
  }

  private Path writeConfig(String content) throws IOException {
    Path file = tempDir.resolve("config.yaml");
    Files.writeString(file, content);
    return file;
  }
}
