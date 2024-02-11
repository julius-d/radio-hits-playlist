package com.github.juliusd.radiohitsplaylist.config;

public record ReCreateFamilyRadioPlaylistTaskConfiguration(
  String streamName,
  String playlistId,
  String descriptionPrefix
) {
}
