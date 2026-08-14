package com.github.juliusd.radiohitsplaylist.source.family;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.radiohitsplaylist.config.Configuration;
import com.github.juliusd.radiohitsplaylist.config.NotifierConfiguration;
import com.github.juliusd.radiohitsplaylist.config.SpotifyConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest
class FamilyRadioClientTest {

  private static final String TEST_CHANNEL_KEY = "test-channel-key";
  private static final long TEST_TIMESTAMP = 1700000000L;

  private FamilyRadioClient familyRadioClient;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
    Configuration configuration =
        new Configuration(
            new SpotifyConfiguration("myRefreshToken", "myClientId", "myClientSecret"),
            List.of(),
            List.of(),
            wmRuntimeInfo.getHttpBaseUrl(),
            List.of(),
            List.of(),
            null,
            null,
            List.of(),
            List.of(),
            new NotifierConfiguration(false, false, null, null));
    familyRadioClient = new FamilyRadioClientConfiguration(configuration).familyRadioClient();
  }

  @Test
  void shouldGetPlaylistSuccessfully() {
    // given
    String expectedResponse =
        """
        {
          "error": 0,
          "data": [
            {
              "ts": 1700003600,
              "title": "Test Song Alpha",
              "artist": "Test Artist One",
              "cover": "https://example.com/cover.jpg",
              "buylink": "https://example.com/buy",
              "hook": "https://example.com/hook.m4a"
            },
            {
              "ts": 1700003700,
              "title": "Test Song Beta",
              "artist": "Test Artist Two",
              "cover": false,
              "buylink": false,
              "hook": false
            }
          ],
          "fromcache": 1
        }
        """;

    stubFor(get(urlPathEqualTo("/ctrl-api/getPlaylist")).willReturn(okJson(expectedResponse)));

    // when
    FamilyRadioResponse response = familyRadioClient.getPlaylist(TEST_CHANNEL_KEY, TEST_TIMESTAMP);

    // then
    assertThat(response).isNotNull();
    assertThat(response.error()).isEqualTo(0);
    assertThat(response.data()).hasSize(2);

    FamilyRadioTrack firstTrack = response.data().get(0);
    assertThat(firstTrack.ts()).isEqualTo(1700003600L);
    assertThat(firstTrack.title()).isEqualTo("Test Song Alpha");
    assertThat(firstTrack.artist()).isEqualTo("Test Artist One");

    FamilyRadioTrack secondTrack = response.data().get(1);
    assertThat(secondTrack.ts()).isEqualTo(1700003700L);
    assertThat(secondTrack.title()).isEqualTo("Test Song Beta");
    assertThat(secondTrack.artist()).isEqualTo("Test Artist Two");

    verify(
        getRequestedFor(urlPathEqualTo("/ctrl-api/getPlaylist"))
            .withQueryParam("k", equalTo(TEST_CHANNEL_KEY))
            .withQueryParam("typ", equalTo("hour"))
            .withQueryParam("ts", equalTo(String.valueOf(TEST_TIMESTAMP)))
            .withHeader("Accept", equalTo("application/json, text/plain, */*"))
            .withHeader("Accept-Language", equalTo("de,en-US;q=0.7,en;q=0.3")));
  }
}
