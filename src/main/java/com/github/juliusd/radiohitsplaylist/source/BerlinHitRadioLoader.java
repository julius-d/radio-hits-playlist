package com.github.juliusd.radiohitsplaylist.source;

import com.github.juliusd.radiohitsplaylist.Track;

import java.util.List;

public class BerlinHitRadioLoader {

  private final BerlinHitRadioClient berlinHitRadioClient;

  BerlinHitRadioLoader(BerlinHitRadioClient berlinHitRadioClient) {
    this.berlinHitRadioClient = berlinHitRadioClient;
  }

  public List<Track> load() {
    List<BerlinHitRadioTrackWrapper> berlinHitRadioTrackWrappers = berlinHitRadioClient.hitFinder(24);
    return berlinHitRadioTrackWrappers.stream().map(BerlinHitRadioTrackWrapper::track).map(it -> new Track(it.title(), it.artist())).toList();
  }
}
