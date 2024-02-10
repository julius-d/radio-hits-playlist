package com.github.juliusd.radiohitsplaylist.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;

public class ConfigLoader {

  public Configuration loadConfig(String filePath) {
    try {
      ObjectMapper mapper = YAMLMapper.builder()
        .build();
        return mapper.readValue(new File(filePath), Configuration.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
