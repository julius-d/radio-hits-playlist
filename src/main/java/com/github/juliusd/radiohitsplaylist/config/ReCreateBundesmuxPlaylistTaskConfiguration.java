package com.github.juliusd.radiohitsplaylist.config;

public record ReCreateBundesmuxPlaylistTaskConfiguration(
  String streamName,
  String playlistId,
  String descriptionPrefix
) {
}
