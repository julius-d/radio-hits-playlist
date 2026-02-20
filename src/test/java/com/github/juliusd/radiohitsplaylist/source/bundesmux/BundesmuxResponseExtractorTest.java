package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BundesmuxResponseExtractorTest {

  @Test
  void shouldExtractJsonFromRealApiResponse() {
    String jsResponse =
        ";0x00000629;((self.$R=self.$R||{})['server-fn:13']=[],($R=>$R[0]={success:!0,data:$R[1]={entries:$R[2]=[]},error:null})($R['server-fn:13']))";
    String extracted = BundesmuxResponseExtractor.extractJson(jsResponse);
    assertThat(extracted).contains("\"success\":true");
    assertThat(extracted).contains("\"data\":");
    assertThat(extracted).contains("\"error\":null");
    assertThat(extracted).doesNotContain("$R[");
  }

  @Test
  void shouldRemoveDollarRAssignments() {
    String jsResponse = "$R[0]={success:!0,data:$R[1]={entries:[]}}";
    String extracted = BundesmuxResponseExtractor.extractJson(jsResponse);
    assertThat(extracted).doesNotContain("$R[1]");
    assertThat(extracted).contains("\"entries\":[]");
  }

  @Test
  void shouldQuoteUnquotedFieldNames() {
    String jsResponse = "$R[0]={success:!0,data:{entries:[]}}";
    String extracted = BundesmuxResponseExtractor.extractJson(jsResponse);
    assertThat(extracted).contains("\"success\":");
    assertThat(extracted).contains("\"data\":");
    assertThat(extracted).contains("\"entries\":");
  }

  @Test
  void shouldHandleJavaScriptBooleans() {
    String jsResponse = "$R[0]={success:!0,hasNextPage:!1,error:null}";
    String extracted = BundesmuxResponseExtractor.extractJson(jsResponse);
    assertThat(extracted).contains("\"success\":true");
    assertThat(extracted).contains("\"hasNextPage\":false");
  }

  @Test
  void shouldHandleNullValues() {
    String jsResponse = "$R[0]={success:!0,error:null}";
    String extracted = BundesmuxResponseExtractor.extractJson(jsResponse);
    assertThat(extracted).contains("\"error\":null");
  }

  @Test
  void shouldThrowExceptionForNullInput() {
    assertThatThrownBy(() -> BundesmuxResponseExtractor.extractJson(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Response is null or empty");
  }

  @Test
  void shouldThrowExceptionForEmptyInput() {
    assertThatThrownBy(() -> BundesmuxResponseExtractor.extractJson(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Response is null or empty");
  }

  @Test
  void shouldThrowExceptionWhenJsonNotFound() {
    String invalidResponse = "This is not a valid response";
    assertThatThrownBy(() -> BundesmuxResponseExtractor.extractJson(invalidResponse))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Could not find JSON in response");
  }

  @Test
  void shouldHandleComplexRealResponse() {
    String jsResponse =
        ";0x00000629;((self.$R=self.$R||{})['server-fn:13']=[],($R=>$R[0]={success:!0,data:$R[1]={entries:$R[2]=[$R[3]={id:199026,title:\"MAMMA MIA\",artist:\"ABBA\",startDate:\"2026-02-19T23:11:53.037Z\"}],pagination:$R[11]={page:1,hasNextPage:!1}},error:null})($R['server-fn:13']))";
    String extracted = BundesmuxResponseExtractor.extractJson(jsResponse);
    assertThat(extracted).contains("\"success\":true");
    assertThat(extracted).contains("\"pagination\":");
    assertThat(extracted).contains("\"hasNextPage\":false");
    assertThat(extracted).doesNotContain("$R[");
  }
}
