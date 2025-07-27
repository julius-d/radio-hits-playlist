package com.github.juliusd.radiohitsplaylist.source.family;

import com.fasterxml.jackson.annotation.JsonProperty;

record FamilyRadioTrack(
    String trackId, String title, @JsonProperty("artistCredits") String artist, String artwork) {}
