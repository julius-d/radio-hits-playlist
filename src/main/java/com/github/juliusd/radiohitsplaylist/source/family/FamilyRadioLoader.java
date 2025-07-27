package com.github.juliusd.radiohitsplaylist.source.family;

import com.github.juliusd.radiohitsplaylist.Track;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FamilyRadioLoader {

  private final FamilyRadioClient familyRadioClient;

  FamilyRadioLoader(FamilyRadioClient familyRadioClient) {
    this.familyRadioClient = familyRadioClient;
  }

  public List<Track> load(String channelId) {
    // Get tracks from yesterday (24 hours worth)
    LocalDate yesterday = LocalDate.now().minusDays(1);
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

    return allTracks.stream()
        .map(FamilyRadioTrackWrapper::track)
        .map(it -> new Track(it.title(), it.artist()))
        .distinct()
        .toList();
  }
}
