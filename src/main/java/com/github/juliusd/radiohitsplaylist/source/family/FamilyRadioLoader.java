package com.github.juliusd.radiohitsplaylist.source.family;

import com.github.juliusd.radiohitsplaylist.Track;
import java.util.List;
import java.util.stream.Stream;

public class FamilyRadioLoader {

  private final FamilyRadioClient familyRadioClient;

  FamilyRadioLoader(FamilyRadioClient familyRadioClient) {
    this.familyRadioClient = familyRadioClient;
  }

  public List<Track> load(String streamName) {
    return Stream.of(6, 9, 12, 15)
        .flatMap(hour -> familyRadioClient.hitFinder(streamName, -1, hour, 48).stream())
        .map(FamilyRadioTrackWrapper::track)
        .map(it -> new Track(it.title(), it.artist()))
        .distinct()
        .toList();
  }
}
