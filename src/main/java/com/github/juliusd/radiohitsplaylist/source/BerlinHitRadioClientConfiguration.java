package com.github.juliusd.radiohitsplaylist.source;

import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

public class BerlinHitRadioClientConfiguration {

  BerlinHitRadioClient berlinHitRadioClient() {
    return Feign.builder()
      .decoder(new JacksonDecoder())
      .encoder(new JacksonEncoder())
      .logLevel(Logger.Level.FULL)
      .target(BerlinHitRadioClient.class, System.getProperty("berlinHitRadioUrl"));
  }

  public BerlinHitRadioLoader berlinHitRadioLoader() {
    return new BerlinHitRadioLoader(berlinHitRadioClient());
  }
}
