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
    // given
    FamilyRadioResponse mockResponse = createMockResponse();
    when(mockClient.getTrackHistory(eq(TEST_CHANNEL_ID), any(), any())).thenReturn(mockResponse);

    // when
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_ID, "08:00", 5);

    // then
    assertThat(result).hasSize(3); // Should be filtered (Morning Song removed) and deduplicated

    // Verify tracks are sorted by time (earliest first)
    // Morning Song (06:00) should be filtered out since it's before 08:00
    assertThat(result.get(0).title()).isEqualTo("Afternoon Hit"); // 14:00
    assertThat(result.get(1).title()).isEqualTo("Evening Tune"); // 20:00
    assertThat(result.get(2).title()).isEqualTo("Night Beat"); // 22:00

    // Verify tracks are unique (duplicate "Afternoon Hit" should be removed)
    long afternoonHitCount =
        result.stream().filter(track -> "Afternoon Hit".equals(track.title())).count();
    assertThat(afternoonHitCount).isEqualTo(1);
  }

  @Test
  void shouldFilterTracksBeforeEarliestTime() {
    // given
    FamilyRadioResponse mockResponse = createMockResponseWithEarlyTracks();
    when(mockClient.getTrackHistory(eq(TEST_CHANNEL_ID), any(), any())).thenReturn(mockResponse);

    // when
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_ID, "10:00", 10);

    // then
    assertThat(result).hasSize(2); // Only tracks after 10:00 should be included
    assertThat(result.get(0).title()).isEqualTo("Late Morning Song"); // 10:30
    assertThat(result.get(1).title()).isEqualTo("Afternoon Song"); // 15:00
  }

  @Test
  void shouldHandlePagination() {
    // given
    FamilyRadioResponse firstPage = createFirstPageResponse();
    FamilyRadioResponse secondPage = createSecondPageResponse();

    when(mockClient.getTrackHistory(eq(TEST_CHANNEL_ID), any(), any())).thenReturn(firstPage);
    when(mockClient.getTrackHistoryWithOffset(
            eq(TEST_CHANNEL_ID), eq("next-page-token"), any(), any()))
        .thenReturn(secondPage);

    // when
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_ID, "06:00", 10);

    // then
    assertThat(result).hasSize(4); // 2 from first page + 2 from second page

    // Verify pagination calls
    verify(mockClient).getTrackHistory(eq(TEST_CHANNEL_ID), any(), any());
    verify(mockClient)
        .getTrackHistoryWithOffset(eq(TEST_CHANNEL_ID), eq("next-page-token"), any(), any());
  }

  @Test
  void shouldLimitResults() {
    // given
    FamilyRadioResponse mockResponse = createLargeResponse();
    when(mockClient.getTrackHistory(eq(TEST_CHANNEL_ID), any(), any())).thenReturn(mockResponse);

    // when
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_ID, "06:00", 3);

    // then
    assertThat(result).hasSize(3);
  }

  @Test
  void shouldUseCorrectDateRange() {
    // given
    FamilyRadioResponse mockResponse = createMockResponse();
    when(mockClient.getTrackHistory(eq(TEST_CHANNEL_ID), any(), any())).thenReturn(mockResponse);

    // when
    familyRadioLoader.load(TEST_CHANNEL_ID, "08:00", 5);

    verify(mockClient).getTrackHistory(TEST_CHANNEL_ID, "2024-01-14T00:00:00Z", "2024-01-14T23:59:59Z");
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
    FamilyRadioResponse responseWithDuplicates = createResponseWithDuplicates();
    when(mockClient.getTrackHistory(eq(TEST_CHANNEL_ID), any(), any()))
        .thenReturn(responseWithDuplicates);

    // when
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_ID, "06:00", 10);

    // then
    assertThat(result).hasSize(2); // Should have unique tracks only
    assertThat(result.stream().map(Track::title)).containsExactly("Song A", "Song B");
  }

  private FamilyRadioResponse createMockResponse() {
    List<FamilyRadioTrackWrapper> tracks =
        List.of(
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("1", "Morning Song", "Artist A", null),
                "2024-01-14T06:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("2", "Afternoon Hit", "Artist B", null),
                "2024-01-14T14:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("3", "Evening Tune", "Artist C", null),
                "2024-01-14T20:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("4", "Night Beat", "Artist D", null),
                "2024-01-14T22:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("5", "Afternoon Hit", "Artist B", null), // Duplicate
                "2024-01-14T15:00:00.000Z"));
    return new FamilyRadioResponse(5, tracks, null);
  }

  private FamilyRadioResponse createMockResponseWithEarlyTracks() {
    List<FamilyRadioTrackWrapper> tracks =
        List.of(
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("1", "Early Morning Song", "Artist A", null),
                "2024-01-14T07:00:00.000Z"), // Before 10:00, should be filtered
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("2", "Late Morning Song", "Artist B", null),
                "2024-01-14T10:30:00.000Z"), // After 10:00, should be included
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("3", "Very Early Song", "Artist C", null),
                "2024-01-14T05:00:00.000Z"), // Before 10:00, should be filtered
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("4", "Afternoon Song", "Artist D", null),
                "2024-01-14T15:00:00.000Z") // After 10:00, should be included
            );
    return new FamilyRadioResponse(4, tracks, null);
  }

  private FamilyRadioResponse createFirstPageResponse() {
    List<FamilyRadioTrackWrapper> tracks =
        List.of(
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("1", "Page 1 Song A", "Artist A", null),
                "2024-01-14T08:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("2", "Page 1 Song B", "Artist B", null),
                "2024-01-14T09:00:00.000Z"));
    return new FamilyRadioResponse(2, tracks, "next-page-token");
  }

  private FamilyRadioResponse createSecondPageResponse() {
    List<FamilyRadioTrackWrapper> tracks =
        List.of(
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("3", "Page 2 Song A", "Artist C", null),
                "2024-01-14T10:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("4", "Page 2 Song B", "Artist D", null),
                "2024-01-14T11:00:00.000Z"));
    return new FamilyRadioResponse(2, tracks, null); // Last page
  }

  private FamilyRadioResponse createLargeResponse() {
    List<FamilyRadioTrackWrapper> tracks =
        List.of(
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("1", "Song 1", "Artist 1", null), "2024-01-14T08:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("2", "Song 2", "Artist 2", null), "2024-01-14T09:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("3", "Song 3", "Artist 3", null), "2024-01-14T10:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("4", "Song 4", "Artist 4", null), "2024-01-14T11:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("5", "Song 5", "Artist 5", null), "2024-01-14T12:00:00.000Z"));
    return new FamilyRadioResponse(5, tracks, null);
  }

  private FamilyRadioResponse createResponseWithDuplicates() {
    List<FamilyRadioTrackWrapper> tracks =
        List.of(
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("1", "Song A", "Artist X", null), "2024-01-14T08:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("2", "Song B", "Artist Y", null), "2024-01-14T09:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("3", "Song A", "Artist X", null), // Same title and artist
                "2024-01-14T10:00:00.000Z"),
            new FamilyRadioTrackWrapper(
                new FamilyRadioTrack("4", "Song B", "Artist Y", null), // Same title and artist
                "2024-01-14T11:00:00.000Z"));
    return new FamilyRadioResponse(4, tracks, null);
  }
}
