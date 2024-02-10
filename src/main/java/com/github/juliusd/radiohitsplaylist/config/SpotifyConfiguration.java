package com.github.juliusd.radiohitsplaylist.config;

public record SpotifyConfiguration(
  String refreshToken,
  String clientId,
  String clientSecret
) {
}
