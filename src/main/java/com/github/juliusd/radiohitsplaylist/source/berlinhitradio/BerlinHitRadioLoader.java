package com.github.juliusd.radiohitsplaylist.source.berlinhitradio;

import com.github.juliusd.radiohitsplaylist.Track;
import java.util.List;
import java.util.stream.Stream;

public class BerlinHitRadioLoader {

  private final BerlinHitRadioClient berlinHitRadioClient;

  BerlinHitRadioLoader(BerlinHitRadioClient berlinHitRadioClient) {
    this.berlinHitRadioClient = berlinHitRadioClient;
  }

  public List<Track> load(String streamName) {
    return Stream.of(6, 9, 12, 15)
        .flatMap(hour -> berlinHitRadioClient.hitFinder(streamName, -1, hour, 48).stream())
        .map(BerlinHitRadioTrackWrapper::track)
        .map(it -> new Track(it.title(), it.artist()))
        .distinct()
        .toList();
  }
}
