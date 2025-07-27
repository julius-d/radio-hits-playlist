package com.github.juliusd.radiohitsplaylist.source.family;

import com.github.juliusd.radiohitsplaylist.Track;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FamilyRadioLoader {

  private final FamilyRadioClient familyRadioClient;
  private final Clock clock;

  FamilyRadioLoader(FamilyRadioClient familyRadioClient, Clock clock) {
    this.familyRadioClient = familyRadioClient;
    this.clock = clock;
  }

  public List<Track> load(String channelId, String earliestSongTime, int trackLimit) {
    // Get tracks from yesterday (24 hours worth)
    LocalDate yesterday = LocalDate.now(clock).minusDays(1);
    LocalDateTime fromDateTime = yesterday.atStartOfDay();
    LocalDateTime toDateTime = yesterday.atTime(23, 59, 59);

    String from = fromDateTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    String to = toDateTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

    List<FamilyRadioTrackWrapper> allTracks = new ArrayList<>();
    String nextOffset = null;
    FamilyRadioResponse response;

    // Load all pages
    do {
      if (nextOffset == null) {
        // First page
        response = familyRadioClient.getTrackHistory(channelId, from, to);
      } else {
        // Subsequent pages
        response = familyRadioClient.getTrackHistoryWithOffset(channelId, nextOffset, from, to);
      }

      allTracks.addAll(response.items());
      nextOffset = response.next();

      // Continue if there's a next page and we got some items
    } while (nextOffset != null && !response.items().isEmpty());

    // Parse the earliest song time threshold
    LocalTime earliestTime =
        LocalTime.parse(earliestSongTime, DateTimeFormatter.ofPattern("HH:mm"));

    return allTracks.stream()
        .sorted(Comparator.comparing(wrapper -> Instant.parse(wrapper.start())))
        .filter(
            wrapper -> {
              Instant startInstant = Instant.parse(wrapper.start());
              LocalTime trackTime = startInstant.atOffset(ZoneOffset.UTC).toLocalTime();
              return !trackTime.isBefore(earliestTime);
            })
        .map(FamilyRadioTrackWrapper::track)
        .map(it -> new Track(it.title(), it.artist()))
        .distinct()
        .limit(trackLimit)
        .toList();
  }
}
