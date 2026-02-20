package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TextNormalizerTest {

  @Test
  void shouldReturnNullForNullInput() {
    assertThat(TextNormalizer.normalizeIfAllCaps(null)).isNull();
  }

  @Test
  void shouldReturnEmptyStringForEmptyInput() {
    assertThat(TextNormalizer.normalizeIfAllCaps("")).isEmpty();
  }

  @Test
  void shouldNotModifyMixedCaseText() {
    String mixedCase = "Already Mixed Case";
    assertThat(TextNormalizer.normalizeIfAllCaps(mixedCase)).isEqualTo(mixedCase);
  }

  @Test
  void shouldConvertAllCapsToTitleCase() {
    assertThat(TextNormalizer.normalizeIfAllCaps("HELLO WORLD")).isEqualTo("Hello World");
  }

  @Test
  void shouldHandleApostrophes() {
    assertThat(TextNormalizer.normalizeIfAllCaps("DON'T STOP ME NOW"))
        .isEqualTo("Don't Stop Me Now");
  }

  @Test
  void shouldHandleFeatKeyword() {
    assertThat(TextNormalizer.normalizeIfAllCaps("SONG TITLE FEAT. ARTIST"))
        .isEqualTo("Song Title feat. Artist");
  }

  @Test
  void shouldHandleAmpersand() {
    assertThat(TextNormalizer.normalizeIfAllCaps("ROCK & ROLL")).isEqualTo("Rock & Roll");
  }

  @Test
  void shouldHandleMultipleApostrophes() {
    assertThat(TextNormalizer.normalizeIfAllCaps("IT'S A BEAUTIFUL DAY"))
        .isEqualTo("It's A Beautiful Day");
  }

  @Test
  void shouldHandleSingleWord() {
    assertThat(TextNormalizer.normalizeIfAllCaps("HELLO")).isEqualTo("Hello");
  }

  @Test
  void shouldHandleSpecialCharactersOnly() {
    String specialChars = "& - !";
    assertThat(TextNormalizer.normalizeIfAllCaps(specialChars)).isEqualTo(specialChars);
  }

  @Test
  void shouldHandleNumbersInText() {
    assertThat(TextNormalizer.normalizeIfAllCaps("HIGHWAY 61 REVISITED"))
        .isEqualTo("Highway 61 Revisited");
  }

  @Test
  void shouldNotModifyLowerCase() {
    String lowerCase = "already lowercase";
    assertThat(TextNormalizer.normalizeIfAllCaps(lowerCase)).isEqualTo(lowerCase);
  }

  @Test
  void shouldHandleComplexArtistName() {
    assertThat(TextNormalizer.normalizeIfAllCaps("THE ROLLING STONES"))
        .isEqualTo("The Rolling Stones");
  }
}
