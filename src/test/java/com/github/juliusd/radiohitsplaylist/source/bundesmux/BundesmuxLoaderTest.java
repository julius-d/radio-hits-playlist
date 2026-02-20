package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.juliusd.radiohitsplaylist.Track;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BundesmuxLoaderTest {

  private static final String TEST_STREAM_NAME = "test-stream";

  private BundesmuxClient mockClient;
  private Clock fixedClock;
  private BundesmuxLoader bundesmuxLoader;

  @BeforeEach
  void setUp() {
    mockClient = mock(BundesmuxClient.class);
    // Fixed clock at 2024-01-15T12:00:00Z, so yesterday is 2024-01-14
    // Loader will fetch from 21:00 to 06:00 of yesterday
    fixedClock = Clock.fixed(Instant.parse("2024-01-15T12:00:00Z"), ZoneId.of("UTC"));
    bundesmuxLoader = new BundesmuxLoader(mockClient, fixedClock);
  }

  @Test
  void shouldLoadTracksAndNormalizeUppercaseText() {
    // given
    String response =
        createSuccessResponse(
            entry("SONG TITLE ONE", "ARTIST ONE", "2024-01-14T22:00:00.000Z"),
            entry("Mixed Case Song", "Mixed Artist", "2024-01-14T21:00:00.000Z"));
    when(mockClient.getMetadataHistory(anyString())).thenReturn(response);

    // when
    List<Track> result = bundesmuxLoader.load(TEST_STREAM_NAME);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).title()).isEqualTo("Mixed Case Song");
    assertThat(result.get(0).artist()).isEqualTo("Mixed Artist");
    assertThat(result.get(1).title()).isEqualTo("Song Title One");
    assertThat(result.get(1).artist()).isEqualTo("Artist One");
  }

  @Test
  void shouldFilterOutComingUpEntries() {
    // given
    String response =
        createSuccessResponse(
            entry("SONG TITLE", "ARTIST NAME", "2024-01-14T22:00:00.000Z"),
            entry("Coming Up", "Coming Up", "2024-01-14T21:00:00.000Z"),
            entry("ANOTHER SONG", "ANOTHER ARTIST", "2024-01-14T20:00:00.000Z"));
    when(mockClient.getMetadataHistory(anyString())).thenReturn(response);

    // when
    List<Track> result = bundesmuxLoader.load(TEST_STREAM_NAME);

    // then
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Track::title).containsExactly("Another Song", "Song Title");
  }

  @Test
  void shouldRemoveDuplicateTracks() {
    // given
    String firstResponse =
        createSuccessResponse(
            entryWithEnd(
                "SAME SONG", "SAME ARTIST", "2024-01-14T20:00:00.000Z", "2024-01-14T20:03:00.000Z"),
            entryWithEnd(
                "DIFFERENT SONG",
                "DIFFERENT ARTIST",
                "2024-01-14T19:00:00.000Z",
                "2024-01-14T19:03:00.000Z"));
    String secondResponse =
        createSuccessResponse(
            entryWithEnd(
                "SAME SONG",
                "SAME ARTIST",
                "2024-01-14T18:00:00.000Z",
                "2024-01-14T18:03:00.000Z"));

    when(mockClient.getMetadataHistory(anyString()))
        .thenReturn(firstResponse)
        .thenReturn(secondResponse)
        .thenReturn(createEmptyResponse());

    // when
    List<Track> result = bundesmuxLoader.load(TEST_STREAM_NAME);

    // then - order is reversed (oldest first after reversal)
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Track::title).containsExactly("Different Song", "Same Song");
  }

  @Test
  void shouldHandleBackwardsPagination() {
    // given - three responses going backwards in time using endDate for pagination
    String firstResponse =
        createSuccessResponse(
            entryWithEnd(
                "SONG 1", "ARTIST 1", "2024-01-14T20:30:00.000Z", "2024-01-14T20:33:00.000Z"),
            entryWithEnd(
                "SONG 2", "ARTIST 2", "2024-01-14T20:00:00.000Z", "2024-01-14T20:03:00.000Z"));
    String secondResponse =
        createSuccessResponse(
            entryWithEnd(
                "SONG 3", "ARTIST 3", "2024-01-14T19:30:00.000Z", "2024-01-14T19:33:00.000Z"),
            entryWithEnd(
                "SONG 4", "ARTIST 4", "2024-01-14T19:00:00.000Z", "2024-01-14T19:03:00.000Z"));
    String thirdResponse =
        createSuccessResponse(
            entryWithEnd(
                "SONG 5", "ARTIST 5", "2024-01-14T18:30:00.000Z", "2024-01-14T18:33:00.000Z"));

    when(mockClient.getMetadataHistory(anyString()))
        .thenReturn(firstResponse)
        .thenReturn(secondResponse)
        .thenReturn(thirdResponse)
        .thenReturn(createEmptyResponse());

    // when
    List<Track> result = bundesmuxLoader.load(TEST_STREAM_NAME);

    // then
    assertThat(result).hasSize(5);
    assertThat(result)
        .extracting(Track::title)
        .containsExactly("Song 5", "Song 4", "Song 3", "Song 2", "Song 1");
  }

  @Test
  void shouldStopAtPreviousDay23Hour() {
    // given - response with entry before the cutoff time (2024-01-13T23:00:00.000Z)
    String response =
        createSuccessResponse(entry("LAST SONG", "LAST ARTIST", "2024-01-13T22:00:00.000Z"));
    when(mockClient.getMetadataHistory(anyString())).thenReturn(response);

    // when
    List<Track> result = bundesmuxLoader.load(TEST_STREAM_NAME);

    // then - should collect all entries as we're still within the range
    assertThat(result).hasSize(1);
  }

  @Test
  void shouldHandleEmptyResponse() {
    // given
    when(mockClient.getMetadataHistory(anyString())).thenReturn(createEmptyResponse());

    // when
    List<Track> result = bundesmuxLoader.load(TEST_STREAM_NAME);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldHandleUnsuccessfulResponse() {
    // given
    String response = "$R[0]={success:!1,error:\"Some error\"}";
    when(mockClient.getMetadataHistory(anyString())).thenReturn(response);

    // when
    List<Track> result = bundesmuxLoader.load(TEST_STREAM_NAME);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldThrowExceptionOnInvalidJson() {
    // given
    when(mockClient.getMetadataHistory(anyString())).thenReturn("Invalid response");

    // when / then
    assertThatThrownBy(() -> bundesmuxLoader.load(TEST_STREAM_NAME))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to load tracks from Bundesmux API");
  }

  @Test
  void shouldHandleNullTitleOrArtist() {
    // given
    String response =
        "$R[0]={success:!0,data:{entries:["
            + "{id:1,title:null,artist:\"ARTIST\",startDate:\"2024-01-14T22:00:00.000Z\"},"
            + "{id:2,title:\"TITLE\",artist:null,startDate:\"2024-01-14T21:00:00.000Z\"},"
            + "{id:3,title:\"VALID TITLE\",artist:\"VALID ARTIST\",startDate:\"2024-01-14T20:00:00.000Z\"}"
            + "]}}";
    when(mockClient.getMetadataHistory(anyString())).thenReturn(response);

    // when
    List<Track> result = bundesmuxLoader.load(TEST_STREAM_NAME);

    // then - only the entry with both title and artist should be included
    assertThat(result).hasSize(1);
    assertThat(result.get(0).title()).isEqualTo("Valid Title");
    assertThat(result.get(0).artist()).isEqualTo("Valid Artist");
  }

  @Test
  void shouldMakeMultipleRequestsUntilFullDayCollected() {
    // given
    when(mockClient.getMetadataHistory(anyString()))
        .thenReturn(
            createSuccessResponse(
                entryWithEnd(
                    "SONG 1", "ARTIST 1", "2024-01-14T20:00:00.000Z", "2024-01-14T20:03:00.000Z")))
        .thenReturn(
            createSuccessResponse(
                entryWithEnd(
                    "SONG 2", "ARTIST 2", "2024-01-14T18:00:00.000Z", "2024-01-14T18:03:00.000Z")))
        .thenReturn(
            createSuccessResponse(
                entryWithEnd(
                    "SONG 3", "ARTIST 3", "2024-01-14T16:00:00.000Z", "2024-01-14T16:03:00.000Z")))
        .thenReturn(createEmptyResponse());

    // when
    bundesmuxLoader.load(TEST_STREAM_NAME);

    // then - should make at least 3 requests
    verify(mockClient, times(4)).getMetadataHistory(anyString());
  }

  private String createSuccessResponse(String... entries) {
    StringBuilder json = new StringBuilder();
    json.append("$R[0]={success:!0,data:{entries:[");
    for (int i = 0; i < entries.length; i++) {
      json.append(entries[i]);
      if (i < entries.length - 1) {
        json.append(",");
      }
    }
    json.append("]}}");
    return json.toString();
  }

  private String createEmptyResponse() {
    return "$R[0]={success:!0,data:{entries:[]}}";
  }

  private String entry(String title, String artist, String startDate) {
    return entryWithEnd(title, artist, startDate, null);
  }

  private String entryWithEnd(String title, String artist, String startDate, String endDate) {
    String endPart = endDate != null ? ",endDate:\"" + endDate + "\"" : ",endDate:null";
    return String.format(
        "{id:%s,title:\"%s\",artist:\"%s\",startDate:\"%s\"%s}",
        System.nanoTime(), title, artist, startDate, endPart);
  }
}
