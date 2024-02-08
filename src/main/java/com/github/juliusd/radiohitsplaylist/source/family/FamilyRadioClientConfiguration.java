package com.github.juliusd.radiohitsplaylist.source.family;

import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

public class FamilyRadioClientConfiguration {

  FamilyRadioClient berlinHitRadioClient() {
    return Feign.builder()
      .decoder(new JacksonDecoder())
      .encoder(new JacksonEncoder())
      .logLevel(Logger.Level.FULL)
      .target(FamilyRadioClient.class, System.getProperty("familyRadioUrl"));
  }

  public FamilyRadioLoader familyRadioLoader() {
    return new FamilyRadioLoader(berlinHitRadioClient());
  }
}
