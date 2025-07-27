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

  private static final String TEST_CHANNEL_ID = "3bb7d791-128a-424f-9ef8-378bd426d833";

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
  void shouldGetTrackHistorySuccessfully() {
    // given
    String expectedResponse =
        """
        {
          "size": 3,
          "items": [
            {
              "track": {
                "trackId": "test-track-id-001",
                "title": "Test Song Alpha",
                "artistCredits": "Test Artist One"
              },
              "start": "2023-01-01T10:00:00.000Z"
            },
            {
              "track": {
                "trackId": "test-track-id-002",
                "title": "Test Song Beta",
                "artistCredits": "Test Artist Two",
                "artwork": "https://example.org/test-artwork-url"
              },
              "start": "2023-01-01T09:57:00.000Z"
            },
            {
              "track": {
                "trackId": "test-track-id-003",
                "title": "Test Song Gamma",
                "artistCredits": "Test Artist Three"
              },
              "start": "2023-01-01T09:54:00.000Z"
            }
          ],
          "next": "test-pagination-token-123"
        }
        """;

    stubFor(
        get(urlPathEqualTo("/channels/" + TEST_CHANNEL_ID + "/track-history"))
            .willReturn(okJson(expectedResponse)));

    // when
    FamilyRadioResponse response =
        familyRadioClient.getTrackHistory(
            TEST_CHANNEL_ID, "2023-01-01T09:00:00.000Z", "2023-01-01T10:59:59.999Z");

    // then
    assertThat(response).isNotNull();
    assertThat(response.size()).isEqualTo(3);
    assertThat(response.next()).isEqualTo("test-pagination-token-123");
    assertThat(response.items()).hasSize(3);

    // Verify first track
    FamilyRadioTrackWrapper firstItem = response.items().get(0);
    assertThat(firstItem.track().trackId()).isEqualTo("test-track-id-001");
    assertThat(firstItem.track().title()).isEqualTo("Test Song Alpha");
    assertThat(firstItem.track().artist()).isEqualTo("Test Artist One");
    assertThat(firstItem.track().artwork()).isNull();
    assertThat(firstItem.start()).isEqualTo("2023-01-01T10:00:00.000Z");

    // Verify second track (with artwork)
    FamilyRadioTrackWrapper secondItem = response.items().get(1);
    assertThat(secondItem.track().trackId()).isEqualTo("test-track-id-002");
    assertThat(secondItem.track().title()).isEqualTo("Test Song Beta");
    assertThat(secondItem.track().artist()).isEqualTo("Test Artist Two");
    assertThat(secondItem.track().artwork()).isEqualTo("https://example.org/test-artwork-url");
    assertThat(secondItem.start()).isEqualTo("2023-01-01T09:57:00.000Z");

    // Verify request was made with correct headers and parameters
    verify(
        getRequestedFor(urlPathEqualTo("/channels/" + TEST_CHANNEL_ID + "/track-history"))
            .withQueryParam("limit", equalTo("10"))
            .withQueryParam("check-favorites", equalTo("false"))
            .withQueryParam("from", equalTo("2023-01-01T09:00:00.000Z"))
            .withQueryParam("to", equalTo("2023-01-01T10:59:59.999Z"))
            .withHeader("Accept", equalTo("application/json, text/plain, */*"))
            .withHeader("Accept-Language", equalTo("de,en-US;q=0.7,en;q=0.3"))
            .withHeader(
                "User-Agent",
                equalTo(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0")));
  }

  @Test
  void shouldGetTrackHistoryWithOffsetSuccessfully() {
    // given
    String expectedResponse =
        """
        {
          "size": 2,
          "items": [
            {
              "track": {
                "trackId": "test-track-id-004",
                "title": "Test Song Delta",
                "artistCredits": "Test Artist Four"
              },
              "start": "2023-01-01T09:51:00.000Z"
            },
            {
              "track": {
                "trackId": "test-track-id-005",
                "title": "Test Song Epsilon",
                "artistCredits": "Test Artist Five"
              },
              "start": "2023-01-01T09:48:00.000Z"
            }
          ],
          "next": null
        }
        """;

    stubFor(
        get(urlPathEqualTo("/channels/" + TEST_CHANNEL_ID + "/track-history"))
            .willReturn(okJson(expectedResponse)));

    // when
    FamilyRadioResponse response =
        familyRadioClient.getTrackHistoryWithOffset(
            TEST_CHANNEL_ID,
            "test-pagination-token-123",
            "2023-01-01T09:00:00.000Z",
            "2023-01-01T10:59:59.999Z");

    // then
    assertThat(response).isNotNull();
    assertThat(response.size()).isEqualTo(2);
    assertThat(response.next()).isNull(); // Last page
    assertThat(response.items()).hasSize(2);

    // Verify first track on second page
    FamilyRadioTrackWrapper firstItem = response.items().get(0);
    assertThat(firstItem.track().trackId()).isEqualTo("test-track-id-004");
    assertThat(firstItem.track().title()).isEqualTo("Test Song Delta");
    assertThat(firstItem.track().artist()).isEqualTo("Test Artist Four");

    // Verify request was made with correct parameters including offset
    verify(
        getRequestedFor(urlPathEqualTo("/channels/" + TEST_CHANNEL_ID + "/track-history"))
            .withQueryParam("limit", equalTo("10"))
            .withQueryParam("offset", equalTo("test-pagination-token-123"))
            .withQueryParam("check-favorites", equalTo("false"))
            .withQueryParam("from", equalTo("2023-01-01T09:00:00.000Z"))
            .withQueryParam("to", equalTo("2023-01-01T10:59:59.999Z")));
  }
}
