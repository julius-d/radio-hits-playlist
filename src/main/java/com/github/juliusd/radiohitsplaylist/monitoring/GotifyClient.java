package com.github.juliusd.radiohitsplaylist.monitoring;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface GotifyClient {

  @Headers({
    "X-Gotify-Key: {apiToken}",
  })
  @RequestLine("POST /message")
  void sendMessage(
    @Param("apiToken") String apiToken,
    GotfiyMessage gotfiyMessage
  );
}
