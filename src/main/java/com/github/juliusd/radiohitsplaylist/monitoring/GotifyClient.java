package com.github.juliusd.radiohitsplaylist.monitoring;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

interface GotifyClient {

  @Headers({
    "Content-Type: application/json",
    "X-Gotify-Key: {apiToken}",
  })
  @RequestLine("POST /message")
  void sendMessage(
    @Param("apiToken") String apiToken,
    GotfiyMessage gotfiyMessage
  );
}
