package com.github.juliusd.radiohitsplaylist.spotify;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.radiohitsplaylist.Track;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlaylistUpdaterTest {

  private static SpotifyTrack spotify(String title, String... artists) {
    return new SpotifyTrack(title, List.of(artists), URI.create("spotify:track:abc"), null);
  }

  @Test
  void isNormalizedMatch_exactMatch() {
    assertThat(
            PlaylistUpdater.isNormalizedMatch(
                new Track("New Religion", "Bebe Rexha"),
                spotify("New Religion", "Bebe Rexha")))
        .isTrue();
  }

  @Test
  void isNormalizedMatch_caseDifference() {
    assertThat(
            PlaylistUpdater.isNormalizedMatch(
                new Track("NEW RELIGION", "BEBE REXHA"),
                spotify("New Religion", "Bebe Rexha")))
        .isTrue();
  }

  @Test
  void isNormalizedMatch_separatorDifference() {
    assertThat(
            PlaylistUpdater.isNormalizedMatch(
                new Track("Stay", "LEONY & CALUM SCOTT"),
                spotify("Stay", "Leony", "Calum Scott")))
        .isTrue();
  }

  @Test
  void isNormalizedMatch_featuringInRadioArtist() {
    assertThat(
            PlaylistUpdater.isNormalizedMatch(
                new Track("Better Days", "Glockenbach featuring Declan J Donovan"),
                spotify("Better Days", "Glockenbach", "Declan J Donovan")))
        .isTrue();
  }

  @Test
  void isNormalizedMatch_bandNameWithAmpersand() {
    assertThat(
            PlaylistUpdater.isNormalizedMatch(
                new Track("The Sound of Silence", "Simon & Garfunkel"),
                spotify("The Sound of Silence", "Simon & Garfunkel")))
        .isTrue();
  }

  @Test
  void isNormalizedMatch_differentSong() {
    assertThat(
            PlaylistUpdater.isNormalizedMatch(
                new Track("Different Title", "Bebe Rexha"),
                spotify("New Religion", "Bebe Rexha")))
        .isFalse();
  }

  @Test
  void isNormalizedMatch_extraSpotifyArtistNotInRadio() {
    assertThat(
            PlaylistUpdater.isNormalizedMatch(
                new Track("New Religion", "Bebe Rexha"),
                spotify("New Religion", "Bebe Rexha", "Faithless")))
        .isFalse();
  }
}
