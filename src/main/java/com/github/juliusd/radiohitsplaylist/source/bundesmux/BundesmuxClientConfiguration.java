package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import com.github.juliusd.radiohitsplaylist.config.Configuration;
import feign.Feign;
import feign.Logger;

public class BundesmuxClientConfiguration {

  private final Configuration configuration;

  public BundesmuxClientConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  BundesmuxClient bundesmuxClient() {
    return Feign.builder()
        .logLevel(Logger.Level.FULL)
        .target(BundesmuxClient.class, configuration.bundesmuxUrl());
  }

  public BundesmuxLoader bundesmuxLoader() {
    return new BundesmuxLoader(bundesmuxClient());
  }
}
