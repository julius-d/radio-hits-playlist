package com.github.juliusd.radiohitsplaylist.source.youngpeople;

import com.github.juliusd.radiohitsplaylist.config.Configuration;
import feign.Feign;
import feign.Logger;

public class YoungPeopleClientConfiguration {

  private final Configuration configuration;

  public YoungPeopleClientConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  YoungPeopleClient youngPeopleClient() {
    return Feign.builder()
        .logLevel(Logger.Level.FULL)
        .target(YoungPeopleClient.class, configuration.youngPeopleUrl());
  }

  public YoungPeopleLoader youngPeopleLoader() {
    return new YoungPeopleLoader(youngPeopleClient());
  }
}
