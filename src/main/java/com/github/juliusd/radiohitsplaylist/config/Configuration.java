package com.github.juliusd.radiohitsplaylist.config;

import java.util.List;

public record Configuration(
  SpotifyConfiguration spotify,
  List<ShuffleTaskConfiguration> shuffleTasks

) {
}
