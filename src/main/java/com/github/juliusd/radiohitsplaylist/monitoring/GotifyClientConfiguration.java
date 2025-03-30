package com.github.juliusd.radiohitsplaylist.monitoring;

import com.github.juliusd.radiohitsplaylist.config.NotifierConfiguration;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

public class GotifyClientConfiguration {

  public GotifyNotifier notifier(NotifierConfiguration config) {
    GotifyClient gotifyClient = gotifyClient(config);
    return new GotifyNotifier(gotifyClient, config);
  }

  GotifyClient gotifyClient(NotifierConfiguration config) {
    return Feign.builder()
      .decoder(new JacksonDecoder())
      .encoder(new JacksonEncoder())
      .logLevel(Logger.Level.FULL)
      .target(GotifyClient.class, config.gotifyUrl());
  }
}
