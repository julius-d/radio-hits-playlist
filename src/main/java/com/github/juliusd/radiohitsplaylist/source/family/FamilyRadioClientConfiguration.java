package com.github.juliusd.radiohitsplaylist.source.family;

import com.github.juliusd.radiohitsplaylist.config.Configuration;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.time.Clock;

public class FamilyRadioClientConfiguration {

  private final Configuration configuration;

  public FamilyRadioClientConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  FamilyRadioClient familyRadioClient() {
    return Feign.builder()
        .decoder(new JacksonDecoder())
        .encoder(new JacksonEncoder())
        .logLevel(Logger.Level.FULL)
        .target(FamilyRadioClient.class, configuration.familyRadioUrl());
  }

  public FamilyRadioLoader familyRadioLoader() {
    return new FamilyRadioLoader(familyRadioClient(), Clock.systemDefaultZone());
  }
}
