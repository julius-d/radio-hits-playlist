package com.github.juliusd.radiohitsplaylist.source.family;

import com.github.juliusd.radiohitsplaylist.Track;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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

  public List<Track> load(String channelKey, String earliestSongTime, int trackLimit) {
    ZoneId zone = clock.getZone();
    LocalDate yesterday = LocalDate.now(clock).minusDays(1);

    LocalTime earliestTime =
        LocalTime.parse(earliestSongTime, DateTimeFormatter.ofPattern("HH:mm"));

    List<FamilyRadioTrack> allTracks = new ArrayList<>();
    for (int hour = earliestTime.getHour(); hour < 24; hour++) {
      long ts = yesterday.atTime(hour, 0, 0).atZone(zone).toEpochSecond();
      FamilyRadioResponse response = familyRadioClient.getPlaylist(channelKey, ts);
      if (response.error() == 0 && response.data() != null) {
        allTracks.addAll(response.data());
      }
    }

    return allTracks.stream()
        .sorted(Comparator.comparingLong(FamilyRadioTrack::ts))
        .filter(
            track -> {
              LocalTime trackTime = Instant.ofEpochSecond(track.ts()).atZone(zone).toLocalTime();
              return !trackTime.isBefore(earliestTime);
            })
        .map(track -> new Track(track.title(), track.artist()))
        .distinct()
        .limit(trackLimit)
        .toList();
  }
}
