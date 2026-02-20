package com.github.juliusd.radiohitsplaylist.source.bundesmux.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Represents the top-level API response from the Bundesmux API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BundesmuxApiResponse {

  private Boolean success;
  private BundesmuxData data;
  private String error;

  public Boolean getSuccess() {
    return success;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }

  public BundesmuxData getData() {
    return data;
  }

  public void setData(BundesmuxData data) {
    this.data = data;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
