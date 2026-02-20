package com.github.juliusd.radiohitsplaylist.source.bundesmux.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Represents the data section of a Bundesmux API response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BundesmuxData {

  private List<BundesmuxMetadataEntry> entries;
  private BundesmuxPagination pagination;
  private BundesmuxChannel channel;

  public List<BundesmuxMetadataEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<BundesmuxMetadataEntry> entries) {
    this.entries = entries;
  }

  public BundesmuxPagination getPagination() {
    return pagination;
  }

  public void setPagination(BundesmuxPagination pagination) {
    this.pagination = pagination;
  }

  public BundesmuxChannel getChannel() {
    return channel;
  }

  public void setChannel(BundesmuxChannel channel) {
    this.channel = channel;
  }
}
