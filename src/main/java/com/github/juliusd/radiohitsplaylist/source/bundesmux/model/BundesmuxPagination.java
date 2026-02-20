package com.github.juliusd.radiohitsplaylist.source.bundesmux.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Represents pagination information from the Bundesmux API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BundesmuxPagination {

  private Integer page;
  private Integer totalPages;
  private Integer totalDocs;
  private Boolean hasNextPage;
  private Boolean hasPrevPage;

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public Integer getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }

  public Integer getTotalDocs() {
    return totalDocs;
  }

  public void setTotalDocs(Integer totalDocs) {
    this.totalDocs = totalDocs;
  }

  public Boolean getHasNextPage() {
    return hasNextPage;
  }

  public void setHasNextPage(Boolean hasNextPage) {
    this.hasNextPage = hasNextPage;
  }

  public Boolean getHasPrevPage() {
    return hasPrevPage;
  }

  public void setHasPrevPage(Boolean hasPrevPage) {
    this.hasPrevPage = hasPrevPage;
  }
}
