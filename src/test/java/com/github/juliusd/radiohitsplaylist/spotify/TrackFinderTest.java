package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.SpotifyApi;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class TrackFinderTest {

  private TrackFinder trackFinder;
  private WireMock wireMock;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
    wireMock = wmRuntimeInfo.getWireMock();
    var spotifyApi = buildSpotifyApiForLocalhost(wmRuntimeInfo.getHttpPort());
    trackFinder = new TrackFinder(spotifyApi);
  }

  @Test
  void searchesForSingleArtistWhenNoExactMatch() {
    wireMock.register(stubFor(get(urlPathEqualTo("/v1/search"))
      .willReturn(okJson(
        """
          {
            "tracks": {
              "limit": 2,
              "next": null,
              "offset": 0,
              "previous": null,
              "total": 0,
              "items": []
            }
          }
          """))));

    wireMock.register(stubFor(get(urlPathEqualTo("/v1/search"))
      .withQueryParam("q", equalTo("artist:\"Udo Lindenberg\" track:\"Komet\""))
      .willReturn(okJson(
        """
          {
            "tracks": {
              "limit": 2,
              "next": null,
              "offset": 0,
              "previous": null,
              "total": 1,
              "items": [
                {
                  "album": {
                    "album_type": "single",
                    "total_tracks": 1,
                    "external_urls": {
                      "spotify": "https://open.spotify.com/album/2XWoQ5VUcmGYL1025mZ5dO"
                    },
                    "href": "https://api.spotify.com/v1/albums/2XWoQ5VUcmGYL1025mZ5dO",
                    "id": "2XWoQ5VUcmGYL1025mZ5dO",
                    "images": [
                      {
                        "url": "https://i.scdn.co/image/ab67616d0000b2738945d66b89a2fefd8e6b2181",
                        "height": 640,
                        "width": 640
                      },
                      {
                        "url": "https://i.scdn.co/image/ab67616d00001e028945d66b89a2fefd8e6b2181",
                        "height": 300,
                        "width": 300
                      },
                      {
                        "url": "https://i.scdn.co/image/ab67616d000048518945d66b89a2fefd8e6b2181",
                        "height": 64,
                        "width": 64
                      }
                    ],
                    "name": "Komet",
                    "release_date": "2023-01-19",
                    "release_date_precision": "day",
                    "type": "album",
                    "uri": "spotify:album:2XWoQ5VUcmGYL1025mZ5dO",
                    "artists": [
                      {
                        "external_urls": {
                          "spotify": "https://open.spotify.com/artist/7iWcRnQMinCoV2u5ICgsW0"
                        },
                        "href": "https://api.spotify.com/v1/artists/7iWcRnQMinCoV2u5ICgsW0",
                        "id": "7iWcRnQMinCoV2u5ICgsW0",
                        "name": "Udo Lindenberg",
                        "type": "artist",
                        "uri": "spotify:artist:7iWcRnQMinCoV2u5ICgsW0"
                      },
                      {
                        "external_urls": {
                          "spotify": "https://open.spotify.com/artist/1qQLhymHXFPtP5U8KNKsm6"
                        },
                        "href": "https://api.spotify.com/v1/artists/1qQLhymHXFPtP5U8KNKsm6",
                        "id": "1qQLhymHXFPtP5U8KNKsm6",
                        "name": "Apache 207",
                        "type": "artist",
                        "uri": "spotify:artist:1qQLhymHXFPtP5U8KNKsm6"
                      }
                    ],
                    "is_playable": true
                  },
                  "artists": [
                    {
                      "external_urls": {
                        "spotify": "https://open.spotify.com/artist/7iWcRnQMinCoV2u5ICgsW0"
                      },
                      "href": "https://api.spotify.com/v1/artists/7iWcRnQMinCoV2u5ICgsW0",
                      "id": "7iWcRnQMinCoV2u5ICgsW0",
                      "name": "Udo Lindenberg",
                      "type": "artist",
                      "uri": "spotify:artist:7iWcRnQMinCoV2u5ICgsW0"
                    },
                    {
                      "external_urls": {
                        "spotify": "https://open.spotify.com/artist/1qQLhymHXFPtP5U8KNKsm6"
                      },
                      "href": "https://api.spotify.com/v1/artists/1qQLhymHXFPtP5U8KNKsm6",
                      "id": "1qQLhymHXFPtP5U8KNKsm6",
                      "name": "Apache 207",
                      "type": "artist",
                      "uri": "spotify:artist:1qQLhymHXFPtP5U8KNKsm6"
                    }
                  ],
                  "disc_number": 1,
                  "duration_ms": 167356,
                  "explicit": false,
                  "external_ids": {
                    "isrc": "DEA622202192"
                  },
                  "external_urls": {
                    "spotify": "https://open.spotify.com/track/7oQepKHmXDaPC3rgeLRvQu"
                  },
                  "href": "https://api.spotify.com/v1/tracks/7oQepKHmXDaPC3rgeLRvQu",
                  "id": "7oQepKHmXDaPC3rgeLRvQu",
                  "is_playable": true,
                  "name": "Komet",
                  "popularity": 78,
                  "track_number": 1,
                  "type": "track",
                  "uri": "spotify:track:7oQepKHmXDaPC3rgeLRvQu",
                  "is_local": false
                }
              ]
            }
          }"""))));


    Optional<SpotifyTrack> spotifyTrack = trackFinder.findSpotifyTrack(new Track("Komet", "Udo Lindenberg & Apache 207"));

    verify(getRequestedFor(urlPathEqualTo("/v1/search"))
      .withQueryParam("q", equalTo("artist:\"Udo Lindenberg & Apache 207\" track:\"Komet\""))
      .withQueryParam("market", equalTo("DE"))
      .withQueryParam("limit", equalTo("2"))
      .withQueryParam("type", equalTo("track"))
    );
    verify(getRequestedFor(urlPathEqualTo("/v1/search"))
      .withQueryParam("q", equalTo("artist:\"Udo Lindenberg\" track:\"Komet\""))
      .withQueryParam("market", equalTo("DE"))
      .withQueryParam("limit", equalTo("2"))
      .withQueryParam("type", equalTo("track"))
    );
    verify(2, getRequestedFor(urlPathEqualTo("/v1/search")));

    assertThat(spotifyTrack.map(SpotifyTrack::uri)).asString().contains("spotify:track:7oQepKHmXDaPC3rgeLRvQu");
  }

  @Test
  void searchWithUnquotedQuery() {
    wireMock.register(stubFor(get(urlPathEqualTo("/v1/search"))
      .willReturn(okJson(
        """
          {
            "tracks": {
              "limit": 2,
              "next": null,
              "offset": 0,
              "previous": null,
              "total": 0,
              "items": []
            }
          }
          """))));

    wireMock.register(stubFor(get(urlPathEqualTo("/v1/search"))
      .withQueryParam("q", equalTo("artist:Lil Nas X track:Star Walkin'"))
      .willReturn(okJson(
        """
          {
            "tracks": {
              "limit": 2,
              "next": null,
              "offset": 0,
              "previous": null,
              "total": 1,
              "items": [
                {
                  "album": {
                    "album_type": "single",
                    "total_tracks": 1,
                    "external_urls": {
                      "spotify": "https://open.spotify.com/album/0aIy6J8M9yHTnjtRu81Nr9"
                    },
                    "href": "https://api.spotify.com/v1/albums/0aIy6J8M9yHTnjtRu81Nr9",
                    "id": "0aIy6J8M9yHTnjtRu81Nr9",
                    "images": [
                      {
                        "height": 640,
                        "url": "https://i.scdn.co/image/ab67616d0000b27304cd9a1664fb4539a55643fe",
                        "width": 640
                      },
                      {
                        "height": 300,
                        "url": "https://i.scdn.co/image/ab67616d00001e0204cd9a1664fb4539a55643fe",
                        "width": 300
                      },
                      {
                        "height": 64,
                        "url": "https://i.scdn.co/image/ab67616d0000485104cd9a1664fb4539a55643fe",
                        "width": 64
                      }
                    ],
                    "name": "STAR WALKIN' (League of Legends Worlds Anthem)",
                    "release_date": "2022-09-22",
                    "release_date_precision": "day",
                    "type": "album",
                    "uri": "spotify:album:0aIy6J8M9yHTnjtRu81Nr9",
                    "artists": [
                      {
                        "external_urls": {
                          "spotify": "https://open.spotify.com/artist/7jVv8c5Fj3E9VhNjxT4snq"
                        },
                        "href": "https://api.spotify.com/v1/artists/7jVv8c5Fj3E9VhNjxT4snq",
                        "id": "7jVv8c5Fj3E9VhNjxT4snq",
                        "name": "Lil Nas X",
                        "type": "artist",
                        "uri": "spotify:artist:7jVv8c5Fj3E9VhNjxT4snq"
                      }
                    ],
                    "is_playable": true
                  },
                  "artists": [
                    {
                      "external_urls": {
                        "spotify": "https://open.spotify.com/artist/7jVv8c5Fj3E9VhNjxT4snq"
                      },
                      "href": "https://api.spotify.com/v1/artists/7jVv8c5Fj3E9VhNjxT4snq",
                      "id": "7jVv8c5Fj3E9VhNjxT4snq",
                      "name": "Lil Nas X",
                      "type": "artist",
                      "uri": "spotify:artist:7jVv8c5Fj3E9VhNjxT4snq"
                    }
                  ],
                  "disc_number": 1,
                  "duration_ms": 210575,
                  "explicit": true,
                  "external_ids": {
                    "isrc": "USSM12208809"
                  },
                  "external_urls": {
                    "spotify": "https://open.spotify.com/track/38T0tPVZHcPZyhtOcCP7pF"
                  },
                  "href": "https://api.spotify.com/v1/tracks/38T0tPVZHcPZyhtOcCP7pF",
                  "id": "38T0tPVZHcPZyhtOcCP7pF",
                  "is_playable": true,
                  "name": "STAR WALKIN' (League of Legends Worlds Anthem)",
                  "popularity": 78,
                  "track_number": 1,
                  "type": "track",
                  "uri": "spotify:track:38T0tPVZHcPZyhtOcCP7pF",
                  "is_local": false
                }
              ]
            }
          }
          """))));

    Optional<SpotifyTrack> spotifyTrack = trackFinder.findSpotifyTrack(new Track("Star Walkin'", "Lil Nas X"));

    verify(getRequestedFor(urlPathEqualTo("/v1/search"))
      .withQueryParam("q", equalTo("artist:\"Lil Nas X\" track:\"Star Walkin'\""))
      .withQueryParam("market", equalTo("DE"))
      .withQueryParam("limit", equalTo("2"))
      .withQueryParam("type", equalTo("track"))
    );

    verify(getRequestedFor(urlPathEqualTo("/v1/search"))
      .withQueryParam("q", equalTo("artist:Lil Nas X track:Star Walkin'"))
      .withQueryParam("market", equalTo("DE"))
      .withQueryParam("limit", equalTo("2"))
      .withQueryParam("type", equalTo("track"))
    );

    verify(2, getRequestedFor(urlPathEqualTo("/v1/search")));


    assertThat(spotifyTrack.map(SpotifyTrack::uri)).asString().contains("spotify:track:38T0tPVZHcPZyhtOcCP7pF");
  }

  private static SpotifyApi buildSpotifyApiForLocalhost(int port) {
    return new SpotifyApi.Builder()
      .setScheme("http")
      .setHost("localhost")
      .setPort(port)
      .setRefreshToken("spotifyRefreshToken")
      .setClientId("123")
      .setClientSecret("clientSecret")
      .setAccessToken("myAccessToken")
      .build();
  }

}
