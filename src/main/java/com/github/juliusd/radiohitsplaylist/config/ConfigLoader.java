package com.github.juliusd.radiohitsplaylist.config;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.io.File;
import java.util.List;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

public class ConfigLoader {

  public Configuration loadConfig(String filePath) {
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("Configuration file path cannot be null or empty");
    }
    ObjectMapper mapper =
        YAMLMapper.builder()
            .withConfigOverride(
                List.class,
                override ->
                    override.setNullHandling(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY)))
            .build();
    return mapper.readValue(new File(filePath), Configuration.class);
  }
}
