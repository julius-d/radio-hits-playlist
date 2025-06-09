package com.github.juliusd.radiohitsplaylist.soundgraph;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AlbumType {
  ALBUM("album"),
  SINGLE("single"),
  COMPILATION("compilation"),
  APPEARS_ON("appears_on");

  private final String value;

  AlbumType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static AlbumType fromString(String value) {
    for (AlbumType type : AlbumType.values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid album type: " + value);
  }
}
