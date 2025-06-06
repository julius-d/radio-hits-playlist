package com.github.juliusd.radiohitsplaylist.config;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigLoader {

  public Configuration loadConfig(String filePath) {
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("Configuration file path cannot be null or empty");
    }
    try {
      ObjectMapper mapper = YAMLMapper.builder()
        .build();
      mapper.configOverride(List.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
      return mapper.readValue(new File(filePath), Configuration.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
