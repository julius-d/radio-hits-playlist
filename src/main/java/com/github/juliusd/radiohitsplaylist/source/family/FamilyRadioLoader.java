package com.github.juliusd.radiohitsplaylist.source.family;

import com.github.juliusd.radiohitsplaylist.Track;

import java.util.List;

public class FamilyRadioLoader {

  private final FamilyRadioClient familyRadioClient;

  FamilyRadioLoader(FamilyRadioClient familyRadioClient) {
    this.familyRadioClient = familyRadioClient;
  }

  public List<Track> load(String streamName) {
    List<FamilyTrackWrapper> familyTrackWrappers = familyRadioClient.hitFinder(streamName, 48);
    return familyTrackWrappers.stream().map(FamilyTrackWrapper::track).map(it -> new Track(it.title(), it.artist())).toList();
  }
}
