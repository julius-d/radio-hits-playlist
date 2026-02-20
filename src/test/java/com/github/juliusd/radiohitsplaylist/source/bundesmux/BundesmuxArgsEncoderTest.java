package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BundesmuxArgsEncoderTest {

  @Test
  void shouldBuildCorrectArgsStructure() throws Exception {
    String channelIdentifier = "test-channel";
    Instant start = Instant.parse("2024-01-15T23:00:00.000Z");
    int page = 1;
    int limit = 50;

    String args = BundesmuxArgsEncoder.buildArgs(channelIdentifier, start, page, limit);
    String decoded = URLDecoder.decode(args, StandardCharsets.UTF_8);

    assertThat(decoded).contains("\"t\":9");
    assertThat(decoded).contains("\"i\":0");
    assertThat(decoded).contains("\"l\":1");
    assertThat(decoded).contains("\"t\":10");
    assertThat(decoded).contains("\"i\":1");
  }

  @Test
  void shouldIncludeAllParameters() throws Exception {
    String channelIdentifier = "my-channel";
    Instant start = Instant.parse("2024-02-20T12:30:45.000Z");
    String startISO = start.toString();
    int page = 2;
    int limit = 100;

    String args = BundesmuxArgsEncoder.buildArgs(channelIdentifier, start, page, limit);
    String decoded = URLDecoder.decode(args, StandardCharsets.UTF_8);

    assertThat(decoded).contains("channelIdentifier");
    assertThat(decoded).contains("start");
    assertThat(decoded).contains("page");
    assertThat(decoded).contains("limit");
    assertThat(decoded).contains(channelIdentifier);
    assertThat(decoded).contains(startISO);
    assertThat(decoded).contains(String.valueOf(page));
    assertThat(decoded).contains(String.valueOf(limit));
  }

  @Test
  void shouldReturnUrlEncodedString() {
    Instant start = Instant.parse("2024-01-01T00:00:00.000Z");
    String args = BundesmuxArgsEncoder.buildArgs("channel", start, 1, 50);

    // URL-encoded strings should not contain unencoded special characters like {, }, [, ]
    assertThat(args).doesNotContain("{");
    assertThat(args).doesNotContain("}");
    assertThat(args).doesNotContain("[");
    assertThat(args).doesNotContain("]");
    assertThat(args).contains("%");
  }

  @Test
  void shouldHandleSpecialCharactersInChannel() throws Exception {
    String channelIdentifier = "channel-with-special_chars";
    Instant start = Instant.parse("2024-01-01T00:00:00.000Z");
    String args = BundesmuxArgsEncoder.buildArgs(channelIdentifier, start, 1, 50);
    String decoded = URLDecoder.decode(args, StandardCharsets.UTF_8);

    assertThat(decoded).contains(channelIdentifier);
  }

  @Test
  void shouldCreateValidJsonStructure() throws Exception {
    Instant start = Instant.parse("2024-01-01T00:00:00.000Z");
    String args = BundesmuxArgsEncoder.buildArgs("test", start, 1, 50);
    String decoded = URLDecoder.decode(args, StandardCharsets.UTF_8);

    // Should be valid JSON with proper structure
    assertThat(decoded).startsWith("{");
    assertThat(decoded).endsWith("}");
    assertThat(decoded).contains("\"k\":");
    assertThat(decoded).contains("\"v\":");
    assertThat(decoded).contains("\"a\":");
    assertThat(decoded).contains("\"p\":");
  }
}
