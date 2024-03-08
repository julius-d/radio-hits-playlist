package com.github.juliusd.radiohitsplaylist.spotify;

import java.net.URI;
import java.util.List;

public record SpotifyTrack(
  String name,
  List<String> artists,
  URI uri,
  URI albumCover
) {
}
