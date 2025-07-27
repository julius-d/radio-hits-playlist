package com.github.juliusd.radiohitsplaylist.source.family;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.juliusd.radiohitsplaylist.Track;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FamilyRadioLoaderTest {

  private static final String TEST_CHANNEL_ID = "test-channel-123";

  private FamilyRadioClient mockClient;
  private Clock fixedClock;
  private FamilyRadioLoader familyRadioLoader;

  @BeforeEach
  void setUp() {
    mockClient = mock(FamilyRadioClient.class);
    fixedClock = Clock.fixed(Instant.parse("2024-01-15T12:00:00Z"), ZoneId.of("UTC"));
    familyRadioLoader = new FamilyRadioLoader(mockClient, fixedClock);
  }

  @Test
  void shouldLoadTracksWithSortingFilteringAndLimiting() {
    givenTrackHistory(
        track("Morning Song", "Artist A", "2024-01-14T06:00:00.000Z"),
        track("Afternoon Hit", "Artist B", "2024-01-14T14:00:00.000Z"),
        track("Evening Tune", "Artist C", "2024-01-14T20:00:00.000Z"),
        track("Night Beat", "Artist D", "2024-01-14T22:00:00.000Z"),
        track("Afternoon Hit", "Artist B", "2024-01-14T15:00:00.000Z") // Duplicate
        );

    // when
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_ID, "08:00", 5);

    // then
    assertThat(result)
        .hasSize(3)
        .extracting(Track::title)
        .containsExactly("Afternoon Hit", "Evening Tune", "Night Beat");
  }

  @Test
  void shouldFilterTracksBeforeEarliestTime() {
    // given
    givenTrackHistory(
        track(
            "Early Morning Song",
            "Artist A",
            "2024-01-14T07:00:00.000Z"), // Before 10:00, should be filtered
        track(
            "Late Morning Song",
            "Artist B",
            "2024-01-14T10:30:00.000Z"), // After 10:00, should be included
        track(
            "Very Early Song",
            "Artist C",
            "2024-01-14T05:00:00.000Z"), // Before 10:00, should be filtered
        track(
            "Afternoon Song",
            "Artist D",
            "2024-01-14T15:00:00.000Z") // After 10:00, should be included
        );

    // when
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_ID, "10:00", 10);

    // then
    assertThat(result)
        .hasSize(2)
        .extracting(Track::title)
        .containsExactly("Late Morning Song", "Afternoon Song");
  }

  @Test
  void shouldHandlePagination() {
    // given
    givenTrackHistoryWithNextPage(
        "next-page-token",
        track("Page 1 Song A", "Artist A", "2024-01-14T08:00:00.000Z"),
        track("Page 1 Song B", "Artist B", "2024-01-14T09:00:00.000Z"));

    FamilyRadioResponse secondPage =
        new FamilyRadioResponse(
            2,
            List.of(
                track("Page 2 Song A", "Artist C", "2024-01-14T10:00:00.000Z"),
                track("Page 2 Song B", "Artist D", "2024-01-14T11:00:00.000Z")),
            null);

    when(mockClient.getTrackHistoryWithOffset(
            eq(TEST_CHANNEL_ID), eq("next-page-token"), any(), any()))
        .thenReturn(secondPage);

    // when
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_ID, "06:00", 10);

    // then
    assertThat(result)
        .hasSize(4) // 2 from first page + 2 from second page
        .extracting(Track::title)
        .containsExactly("Page 1 Song A", "Page 1 Song B", "Page 2 Song A", "Page 2 Song B");

    // Verify pagination calls
    verify(mockClient).getTrackHistory(eq(TEST_CHANNEL_ID), any(), any());
    verify(mockClient)
        .getTrackHistoryWithOffset(eq(TEST_CHANNEL_ID), eq("next-page-token"), any(), any());
  }

  @Test
  void shouldLimitResults() {
    // given
    givenTrackHistory(
        track("Song 1", "Artist 1", "2024-01-14T08:00:00.000Z"),
        track("Song 2", "Artist 2", "2024-01-14T09:00:00.000Z"),
        track("Song 3", "Artist 3", "2024-01-14T10:00:00.000Z"),
        track("Song 4", "Artist 4", "2024-01-14T11:00:00.000Z"),
        track("Song 5", "Artist 5", "2024-01-14T12:00:00.000Z"));

    // when
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_ID, "06:00", 3);

    // then
    assertThat(result).hasSize(3);
  }

  @Test
  void shouldUseCorrectDateRange() {
    // given
    givenTrackHistory(track("Morning Song", "Artist A", "2024-01-14T06:00:00.000Z"));

    // when
    familyRadioLoader.load(TEST_CHANNEL_ID, "08:00", 5);

    // then - verify the date range is yesterday (2024-01-14)
    verify(mockClient)
        .getTrackHistory(TEST_CHANNEL_ID, "2024-01-14T00:00:00Z", "2024-01-14T23:59:59Z");
  }

  @Test
  void shouldHandleEmptyResponse() {
    // given
    FamilyRadioResponse emptyResponse = new FamilyRadioResponse(0, List.of(), null);
    when(mockClient.getTrackHistory(eq(TEST_CHANNEL_ID), any(), any())).thenReturn(emptyResponse);

    // when
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_ID, "08:00", 5);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldRemoveDuplicateTracks() {
    // given
    givenTrackHistory(
        track("Song A", "Artist X", "2024-01-14T08:00:00.000Z"),
        track("Song B", "Artist Y", "2024-01-14T09:00:00.000Z"),
        track("Song A", "Artist X", "2024-01-14T10:00:00.000Z"), // Same title and artist
        track("Song B", "Artist Y", "2024-01-14T11:00:00.000Z") // Same title and artist
        );

    // when
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_ID, "06:00", 10);

    // then
    assertThat(result).hasSize(2).extracting(Track::title).containsExactly("Song A", "Song B");
  }

  private FamilyRadioTrackWrapper track(String title, String artist, String timestamp) {
    return new FamilyRadioTrackWrapper(
        new FamilyRadioTrack(String.valueOf(System.nanoTime()), title, artist, null), timestamp);
  }

  private void givenTrackHistory(FamilyRadioTrackWrapper... tracks) {
    FamilyRadioResponse mockResponse =
        new FamilyRadioResponse(tracks.length, List.of(tracks), null);
    when(mockClient.getTrackHistory(eq(TEST_CHANNEL_ID), any(), any())).thenReturn(mockResponse);
  }

  private void givenTrackHistoryWithNextPage(
      String nextPageToken, FamilyRadioTrackWrapper... tracks) {
    FamilyRadioResponse mockResponse =
        new FamilyRadioResponse(tracks.length, List.of(tracks), nextPageToken);
    when(mockClient.getTrackHistory(eq(TEST_CHANNEL_ID), any(), any())).thenReturn(mockResponse);
  }
}
