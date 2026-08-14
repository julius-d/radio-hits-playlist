package com.github.juliusd.radiohitsplaylist.source.family;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.juliusd.radiohitsplaylist.Track;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FamilyRadioLoaderTest {

  private static final String TEST_CHANNEL_KEY = "test-channel-key";

  private FamilyRadioClient mockClient;
  private Clock fixedClock;
  private FamilyRadioLoader familyRadioLoader;

  @BeforeEach
  void setUp() {
    mockClient = mock(FamilyRadioClient.class);
    fixedClock = Clock.fixed(Instant.parse("2024-01-15T12:00:00Z"), ZoneId.of("UTC"));
    familyRadioLoader = new FamilyRadioLoader(mockClient, fixedClock);
    // Default: all 24 hourly calls return empty
    when(mockClient.getPlaylist(eq(TEST_CHANNEL_KEY), anyLong()))
        .thenReturn(new FamilyRadioResponse(0, List.of()));
  }

  @Test
  void shouldOnlyFetchHoursFromEarliestTimeOnward() {
    // earliestSongTime 08:00 → should request hours 8..23 = 16 calls, not 24
    familyRadioLoader.load(TEST_CHANNEL_KEY, "08:00", 100);

    verify(mockClient, times(16)).getPlaylist(eq(TEST_CHANNEL_KEY), anyLong());
    // hour 7 must NOT be requested
    long hour7ts = Instant.parse("2024-01-14T07:00:00Z").getEpochSecond();
    verify(mockClient, times(0)).getPlaylist(TEST_CHANNEL_KEY, hour7ts);
  }

  @Test
  void shouldUseCorrectTimestampsForYesterday() {
    familyRadioLoader.load(TEST_CHANNEL_KEY, "00:00", 100);

    // Yesterday is 2024-01-14; hour 0 = 1705190400, hour 23 = 1705273200
    verify(mockClient, times(24)).getPlaylist(eq(TEST_CHANNEL_KEY), anyLong());
    verify(mockClient).getPlaylist(TEST_CHANNEL_KEY, 1705190400L); // 2024-01-14T00:00:00Z
    verify(mockClient).getPlaylist(TEST_CHANNEL_KEY, 1705273200L); // 2024-01-14T23:00:00Z
  }

  @Test
  void shouldLoadTracksWithSortingFilteringAndLimiting() {
    givenHourResponse(6, track("Morning Song", "Artist A", "2024-01-14T06:00:00Z"));
    givenHourResponse(14, track("Afternoon Hit", "Artist B", "2024-01-14T14:00:00Z"));
    givenHourResponse(20, track("Evening Tune", "Artist C", "2024-01-14T20:00:00Z"));
    givenHourResponse(22, track("Night Beat", "Artist D", "2024-01-14T22:00:00Z"));
    givenHourResponse(15, track("Afternoon Hit", "Artist B", "2024-01-14T15:00:00Z")); // duplicate

    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_KEY, "08:00", 5);

    assertThat(result)
        .hasSize(3)
        .extracting(Track::title)
        .containsExactly("Afternoon Hit", "Evening Tune", "Night Beat");
  }

  @Test
  void shouldFilterTracksBeforeEarliestTime() {
    // Both tracks fall within hour 10 (which is fetched), but 10:15 is before the 10:30 cutoff
    givenHourResponse(
        10,
        track("Too Early Song", "Artist A", "2024-01-14T10:15:00Z"),
        track("Late Morning Song", "Artist B", "2024-01-14T10:30:00Z"));
    givenHourResponse(15, track("Afternoon Song", "Artist D", "2024-01-14T15:00:00Z"));

    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_KEY, "10:30", 10);

    assertThat(result)
        .hasSize(2)
        .extracting(Track::title)
        .containsExactly("Late Morning Song", "Afternoon Song");
  }

  @Test
  void shouldLimitResults() {
    givenHourResponse(
        8,
        track("Song 1", "Artist 1", "2024-01-14T08:00:00Z"),
        track("Song 2", "Artist 2", "2024-01-14T08:10:00Z"),
        track("Song 3", "Artist 3", "2024-01-14T08:20:00Z"),
        track("Song 4", "Artist 4", "2024-01-14T08:30:00Z"),
        track("Song 5", "Artist 5", "2024-01-14T08:40:00Z"));

    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_KEY, "06:00", 3);

    assertThat(result).hasSize(3);
  }

  @Test
  void shouldRemoveDuplicateTracks() {
    givenHourResponse(
        8,
        track("Song A", "Artist X", "2024-01-14T08:00:00Z"),
        track("Song B", "Artist Y", "2024-01-14T08:10:00Z"));
    givenHourResponse(
        9,
        track("Song A", "Artist X", "2024-01-14T09:00:00Z"), // same title+artist
        track("Song B", "Artist Y", "2024-01-14T09:10:00Z")); // same title+artist

    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_KEY, "06:00", 10);

    assertThat(result).hasSize(2).extracting(Track::title).containsExactly("Song A", "Song B");
  }

  @Test
  void shouldSkipHoursWithErrors() {
    givenHourResponse(8, track("Good Song", "Artist A", "2024-01-14T08:00:00Z"));
    long errorHourTs = Instant.parse("2024-01-14T09:00:00Z").getEpochSecond();
    when(mockClient.getPlaylist(TEST_CHANNEL_KEY, errorHourTs))
        .thenReturn(new FamilyRadioResponse(1, List.of()));

    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_KEY, "00:00", 10);

    assertThat(result).hasSize(1).extracting(Track::title).containsExactly("Good Song");
  }

  @Test
  void shouldHandleEmptyResponse() {
    List<Track> result = familyRadioLoader.load(TEST_CHANNEL_KEY, "08:00", 5);

    assertThat(result).isEmpty();
  }

  private FamilyRadioTrack track(String title, String artist, String isoInstant) {
    long ts = Instant.parse(isoInstant).getEpochSecond();
    return new FamilyRadioTrack(ts, title, artist);
  }

  private void givenHourResponse(int hour, FamilyRadioTrack... tracks) {
    long ts =
        Instant.parse("2024-01-14T00:00:00Z")
            .atOffset(ZoneOffset.UTC)
            .withHour(hour)
            .toEpochSecond();
    when(mockClient.getPlaylist(TEST_CHANNEL_KEY, ts))
        .thenReturn(new FamilyRadioResponse(0, List.of(tracks)));
  }
}
