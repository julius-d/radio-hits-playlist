package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.juliusd.radiohitsplaylist.Track;
import com.github.juliusd.radiohitsplaylist.source.bundesmux.model.BundesmuxApiResponse;
import com.github.juliusd.radiohitsplaylist.source.bundesmux.model.BundesmuxMetadataEntry;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BundesmuxLoader {

  private final BundesmuxClient bundesmuxClient;
  private final Clock clock;
  private final ObjectMapper objectMapper;

  BundesmuxLoader(BundesmuxClient bundesmuxClient, Clock clock) {
    this.bundesmuxClient = bundesmuxClient;
    this.clock = clock;
    this.objectMapper = new ObjectMapper();
  }

  public List<Track> load(String streamName) {
    LocalDate yesterday = LocalDate.now(clock).minusDays(1);
    Instant currentTime = yesterday.atTime(20, 0, 0, 0).toInstant(ZoneOffset.UTC);
    Instant endTime = yesterday.atTime(9, 0, 0, 0).toInstant(ZoneOffset.UTC);

    List<Track> allTracks = new ArrayList<>();

    while (currentTime.isAfter(endTime)) {
      try {
        String args = BundesmuxArgsEncoder.buildArgs(streamName, currentTime, 1, 20);
        String jsResponse = bundesmuxClient.getMetadataHistory(args);
        String json = BundesmuxResponseExtractor.extractJson(jsResponse);

        BundesmuxApiResponse response = objectMapper.readValue(json, BundesmuxApiResponse.class);

        if (response.getSuccess() == null
            || !response.getSuccess()
            || response.getData() == null
            || response.getData().getEntries() == null) {
          break;
        }

        List<BundesmuxMetadataEntry> entries = response.getData().getEntries();

        if (entries.isEmpty()) {
          break;
        }

        // Process entries and add to track list
        for (BundesmuxMetadataEntry entry : entries) {
          String title = TextNormalizer.normalizeIfAllCaps(entry.getTitle());
          String artist = TextNormalizer.normalizeIfAllCaps(entry.getArtist());

          if (title != null
              && artist != null
              && !title.equalsIgnoreCase("Coming Up")
              && !artist.equalsIgnoreCase("Coming Up")) {
            Track track = new Track(title, artist);
            if (!allTracks.contains(track)) {
              allTracks.add(track);
            }
          }
        }

        String lastEndDate = entries.get(entries.size() - 1).getEndDate();
        if (lastEndDate == null) {
          break;
        }
        currentTime =
            min(currentTime, Instant.parse(lastEndDate).truncatedTo(ChronoUnit.MINUTES))
                .minus(10, ChronoUnit.MINUTES);
        if (allTracks.size() > 100) {
          break;
        }
      } catch (Exception e) {
        throw new RuntimeException("Failed to load tracks from Bundesmux API", e);
      }
    }

    Collections.reverse(allTracks);
    return Collections.unmodifiableList(allTracks);
  }

  private static Instant min(Instant instant1, Instant instant2) {
    return instant1.isBefore(instant2) ? instant1 : instant2;
  }
}
