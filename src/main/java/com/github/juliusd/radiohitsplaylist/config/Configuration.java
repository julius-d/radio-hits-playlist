package com.github.juliusd.radiohitsplaylist.config;

import java.util.List;

public record Configuration(
  SpotifyConfiguration spotify,
  List<ShuffleTaskConfiguration> shuffleTasks,
  List<ReCreateFamilyRadioPlaylistTaskConfiguration> reCreateFamilyRadioPlaylistTasks,
  List<ReCreateBerlinHitRadioPlaylistTaskConfiguration> reCreateBerlinHitRadioPlaylistTasks,
  String bundesmuxUrl,
  List<ReCreateBundesmuxPlaylistTaskConfiguration> reCreateBundesmuxPlaylistTasks

) {
}
