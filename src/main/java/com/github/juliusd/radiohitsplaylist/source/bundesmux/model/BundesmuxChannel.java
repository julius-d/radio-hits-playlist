package com.github.juliusd.radiohitsplaylist.source.bundesmux.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Represents channel information from the Bundesmux API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BundesmuxChannel {

  private String identifier;
  private String displayName;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}
