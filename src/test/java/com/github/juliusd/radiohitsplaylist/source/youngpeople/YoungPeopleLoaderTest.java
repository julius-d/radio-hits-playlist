package com.github.juliusd.radiohitsplaylist.source.youngpeople;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.juliusd.radiohitsplaylist.Track;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YoungPeopleLoaderTest {

  @Mock private YoungPeopleClient youngPeopleClient;

  private YoungPeopleLoader youngPeopleLoader;

  @BeforeEach
  void setUp() {
    youngPeopleLoader = new YoungPeopleLoader(youngPeopleClient);
  }

  @Test
  void shouldParseTracksFromHtml() {
    String mockHtml =
        """
        <html>
        <body>
        <table class="playlist_aktueller_tag">
          <tbody>
            <tr class="track">
              <th colspan="2" class="column_1">Zeit</th>
              <th colspan="2" class="column_2">Künstler</th>
              <th colspan="2" class="column_3">Titel</th>
            </tr>
            <tr class="play_track">
              <td class="play_time">17:00</td>
              <td class="current_track"></td>
              <td class="trackpicture"></td>
              <td class="trackinterpret">Ion Miles; SIRA; BHZ</td>
              <td class="tracktitle">2007</td>
              <td class="trackbemerkung"></td>
            </tr>
            <tr class="play_track">
              <td class="play_time">17:05</td>
              <td class="current_track"></td>
              <td class="trackpicture"></td>
              <td class="trackinterpret">Armin van Buuren; JAI RYU</td>
              <td class="tracktitle">Let It Be for Love</td>
              <td class="trackbemerkung"></td>
            </tr>
            <tr class="play_track">
              <td class="play_time">17:10</td>
              <td class="current_track"></td>
              <td class="trackpicture"></td>
              <td class="trackinterpret">Royel Otis</td>
              <td class="tracktitle">Car</td>
              <td class="trackbemerkung"></td>
            </tr>
          </tbody>
        </table>
        </body>
        </html>
        """;

    when(youngPeopleClient.getPlaylistHtml("beste_musik/beste_musik_am_freitag"))
        .thenReturn(mockHtml);

    List<Track> tracks = youngPeopleLoader.load("beste_musik/beste_musik_am_freitag");

    assertThat(tracks)
        .hasSize(3)
        .containsExactly(
            new Track("2007", "Ion Miles; SIRA; BHZ"),
            new Track("Let It Be for Love", "Armin van Buuren; JAI RYU"),
            new Track("Car", "Royel Otis"));
  }

  @Test
  void shouldRemoveDuplicateTracks() {
    String mockHtml =
        """
        <html>
        <body>
        <table class="playlist_aktueller_tag">
          <tbody>
            <tr class="play_track">
              <td class="play_time">17:00</td>
              <td class="current_track"></td>
              <td class="trackpicture"></td>
              <td class="trackinterpret">Same Artist</td>
              <td class="tracktitle">Same Track</td>
              <td class="trackbemerkung"></td>
            </tr>
            <tr class="play_track">
              <td class="play_time">17:05</td>
              <td class="current_track"></td>
              <td class="trackpicture"></td>
              <td class="trackinterpret">Different Artist</td>
              <td class="tracktitle">Different Track</td>
              <td class="trackbemerkung"></td>
            </tr>
            <tr class="play_track">
              <td class="play_time">17:10</td>
              <td class="current_track"></td>
              <td class="trackpicture"></td>
              <td class="trackinterpret">Same Artist</td>
              <td class="tracktitle">Same Track</td>
              <td class="trackbemerkung"></td>
            </tr>
          </tbody>
        </table>
        </body>
        </html>
        """;

    when(youngPeopleClient.getPlaylistHtml("beste_musik/beste_musik_am_freitag"))
        .thenReturn(mockHtml);

    List<Track> tracks = youngPeopleLoader.load("beste_musik/beste_musik_am_freitag");

    assertThat(tracks)
        .hasSize(2)
        .containsExactly(
            new Track("Same Track", "Same Artist"),
            new Track("Different Track", "Different Artist"));
  }

  @Test
  void shouldIgnoreHeaderRowsAndOnlyProcessPlayTrackRows() {
    String mockHtml =
        """
        <html>
        <body>
        <table class="playlist_aktueller_tag">
          <tbody>
            <tr class="track">
              <th colspan="2" class="column_1">Zeit</th>
              <th colspan="2" class="column_2">Künstler</th>
              <th colspan="2" class="column_3">Titel</th>
            </tr>
            <tr class="play_track">
              <td class="play_time">17:00</td>
              <td class="current_track"></td>
              <td class="trackpicture"></td>
              <td class="trackinterpret">Valid Artist</td>
              <td class="tracktitle">Valid Track</td>
              <td class="trackbemerkung"></td>
            </tr>
            <tr class="some_other_row">
              <td class="trackinterpret">Should Be Ignored</td>
              <td class="tracktitle">Should Be Ignored</td>
            </tr>
          </tbody>
        </table>
        </body>
        </html>
        """;

    when(youngPeopleClient.getPlaylistHtml("beste_musik/beste_musik_am_freitag"))
        .thenReturn(mockHtml);

    List<Track> tracks = youngPeopleLoader.load("beste_musik/beste_musik_am_freitag");

    assertThat(tracks).hasSize(1).containsExactly(new Track("Valid Track", "Valid Artist"));
  }
}
