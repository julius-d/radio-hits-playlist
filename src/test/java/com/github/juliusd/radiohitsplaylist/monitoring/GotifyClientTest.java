package com.github.juliusd.radiohitsplaylist.monitoring;

import com.github.juliusd.radiohitsplaylist.config.NotifierConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

@WireMockTest
class GotifyClientTest {

  private static final String TOKEN = "test-token";
  private GotifyClient gotifyClient;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
    gotifyClient = new GotifyClientConfiguration().gotifyClient(new NotifierConfiguration(
      true,
      true,
      wmRuntimeInfo.getHttpBaseUrl(),
      TOKEN)
    );

  }

  @Test
  void testSendMessageSuccess() {
    stubFor(post(urlEqualTo("/message"))
      .willReturn(aResponse()
        .withStatus(200)));

    gotifyClient.sendMessage(TOKEN, new GotfiyMessage(
      "Test title",
      "Test message",
      2
    ));

    verify(postRequestedFor(urlEqualTo("/message"))
      .withHeader("X-Gotify-Key", equalTo(TOKEN))
      .withRequestBody(equalToJson(
        """
        {
          "title": "Test title",
          "message": "Test message",
          "priority": 2
        }
        """)));
  }

}
