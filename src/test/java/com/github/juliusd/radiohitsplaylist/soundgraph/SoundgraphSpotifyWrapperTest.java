package com.github.juliusd.radiohitsplaylist.soundgraph;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.SpotifyApi;

@WireMockTest
class SoundgraphSpotifyWrapperTest {

  private SoundgraphSpotifyWrapper soundgraphSpotifyWrapper;
  private WireMock wireMock;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
    wireMock = wmRuntimeInfo.getWireMock();
    var spotifyApi = buildSpotifyApiForLocalhost(wmRuntimeInfo.getHttpPort());
    soundgraphSpotifyWrapper = new SoundgraphSpotifyWrapper(spotifyApi);
  }

  @Test
  void shouldReturnNewestAlbumTracks() {
    // given
    String artistId = "artist1";
    givenArtistAlbumsResponse(artistId);
    givenAlbumTracksResponse("album2");

    // when
    List<SoundgraphSong> tracks =
        soundgraphSpotifyWrapper.getArtistNewestAlbumTracks(
            artistId, List.of(AlbumType.ALBUM), List.of());

    // then
    assertThat(tracks)
        .hasSize(2)
        .extracting(SoundgraphSong::title)
        .containsExactly("Track 1", "Track 2");
  }

  @Test
  void shouldReturnEmptyListWhenNoAlbumsFound() {
    // given
    String artistId = "artist1";
    givenEmptyArtistAlbumsResponse(artistId);

    // when
    List<SoundgraphSong> tracks =
        soundgraphSpotifyWrapper.getArtistNewestAlbumTracks(
            artistId, List.of(AlbumType.ALBUM), List.of());

    // then
    assertThat(tracks).isEmpty();
  }

  @Test
  void shouldHandleMultipleAlbumTypes() {
    // given
    String artistId = "artist1";
    givenArtistAlbumsWithMultipleTypesResponse(artistId);
    givenAlbumTracksResponse("album1");

    // when
    List<SoundgraphSong> tracks =
        soundgraphSpotifyWrapper.getArtistNewestAlbumTracks(
            artistId, List.of(AlbumType.ALBUM), List.of());

    // then
    assertThat(tracks)
        .hasSize(2)
        .extracting(SoundgraphSong::title)
        .containsExactly("Track 1", "Track 2");
  }

  @Test
  void shouldHandlePaginatedAlbumResults() {
    // given
    String artistId = "artist1";
    givenPaginatedArtistAlbumsResponse(artistId);
    givenAlbumTracksResponse("album3"); // Should get tracks from the newest album (album3)

    // when
    List<SoundgraphSong> tracks =
        soundgraphSpotifyWrapper.getArtistNewestAlbumTracks(
            artistId, List.of(AlbumType.ALBUM), List.of());

    // then
    assertThat(tracks)
        .hasSize(2)
        .extracting(SoundgraphSong::title)
        .containsExactly("Track 1", "Track 2");
  }

  @Test
  void shouldExcludeAlbumsWithTitleContaining() {
    // given
    String artistId = "artist1";
    given2NewestAlbumsOneDeluxOnNormal(artistId);
    givenAlbumTracksResponse("album1");

    // when: Exclude albums with 'Deluxe' in the title
    var tracks =
        soundgraphSpotifyWrapper.getArtistNewestAlbumTracks(
            artistId, List.of(AlbumType.ALBUM), List.of("Deluxe"));

    assertThat(tracks)
        .hasSize(2)
        .extracting(SoundgraphSong::title)
        .containsExactly("Track 1", "Track 2");

    verify(
        getRequestedFor(urlPathEqualTo("/v1/artists/" + artistId + "/albums"))
            .withQueryParam("market", equalTo("DE"))
            .withQueryParam("limit", equalTo("50"))
            .withQueryParam("offset", equalTo("0")));

    verify(getRequestedFor(urlPathEqualTo("/v1/albums/album1/tracks")));

    verify(0, getRequestedFor(urlPathEqualTo("/v1/albums/album2/tracks")));
  }

  private void given2NewestAlbumsOneDeluxOnNormal(String artistId) {
    wireMock.register(
        get(urlPathEqualTo("/v1/artists/" + artistId + "/albums"))
            .withQueryParam("market", equalTo("DE"))
            .willReturn(
                okJson(
                    """
                    {
                      "href": "https://api.spotify.com/v1/artists/%s/albums",
                      "items": [
                        {
                          "album_type": "album",
                          "id": "album1",
                          "name": "Normal Album",
                          "release_date": "2024-01-01",
                          "total_tracks": 10,
                          "type": "album",
                          "album_group": "album",
                          "artists": [
                            {
                              "id": "artist1",
                              "name": "Test Artist"
                            }
                          ]
                        },
                        {
                          "album_type": "album",
                          "id": "album2",
                          "name": "Deluxe Edition",
                          "release_date": "2024-02-01",
                          "total_tracks": 10,
                          "type": "album",
                          "album_group": "album",
                          "artists": [
                            {
                              "id": "artist1",
                              "name": "Test Artist"
                            }
                          ]
                        }
                      ],
                      "limit": 50,
                      "next": null,
                      "offset": 0,
                      "previous": null,
                      "total": 2
                    }
                    """
                        .formatted(artistId))));
  }

  private void givenArtistAlbumsResponse(String artistId) {
    wireMock.register(
        get(urlPathEqualTo("/v1/artists/" + artistId + "/albums"))
            .withQueryParam("market", equalTo("DE"))
            .willReturn(
                okJson(
                    """
                    {
                      "href": "https://api.spotify.com/v1/artists/%s/albums",
                      "items": [
                        {
                          "album_type": "album",
                          "id": "album1",
                          "name": "Old Album",
                          "release_date": "2023-01-01",
                          "total_tracks": 10,
                          "type": "album",
                          "album_group": "album",
                          "artists": [
                            {
                              "id": "artist1",
                              "name": "Test Artist"
                            }
                          ]
                        },
                        {
                          "album_type": "album",
                          "id": "album2",
                          "name": "New Album",
                          "release_date": "2024-01-01",
                          "total_tracks": 10,
                          "type": "album",
                          "album_group": "album",
                          "artists": [
                            {
                              "id": "artist1",
                              "name": "Test Artist"
                            }
                          ]
                        }
                      ],
                      "limit": 50,
                      "next": null,
                      "offset": 0,
                      "previous": null,
                      "total": 2
                    }
                    """
                        .formatted(artistId))));
  }

  private void givenEmptyArtistAlbumsResponse(String artistId) {
    wireMock.register(
        get(urlPathEqualTo("/v1/artists/" + artistId + "/albums"))
            .withQueryParam("market", equalTo("DE"))
            .willReturn(
                okJson(
                    """
                    {
                      "href": "https://api.spotify.com/v1/artists/%s/albums",
                      "items": [],
                      "limit": 50,
                      "next": null,
                      "offset": 0,
                      "previous": null,
                      "total": 0
                    }
                    """
                        .formatted(artistId))));
  }

  private void givenArtistAlbumsWithMultipleTypesResponse(String artistId) {
    wireMock.register(
        get(urlPathEqualTo("/v1/artists/" + artistId + "/albums"))
            .withQueryParam("market", equalTo("DE"))
            .willReturn(
                okJson(
                    """
                    {
                      "href": "https://api.spotify.com/v1/artists/%s/albums",
                      "items": [
                        {
                          "album_type": "album",
                          "id": "album1",
                          "name": "Old Album",
                          "release_date": "2023-01-01",
                          "total_tracks": 10,
                          "type": "album",
                          "album_group": "album",
                          "artists": [
                            {
                              "id": "artist1",
                              "name": "Test Artist"
                            }
                          ]
                        },
                        {
                          "album_type": "single",
                          "id": "single1",
                          "name": "New Single",
                          "release_date": "2024-01-01",
                          "total_tracks": 2,
                          "type": "single",
                          "album_group": "single",
                          "artists": [
                            {
                              "id": "artist1",
                              "name": "Test Artist"
                            }
                          ]
                        }
                      ],
                      "limit": 50,
                      "next": null,
                      "offset": 0,
                      "previous": null,
                      "total": 2
                    }
                    """
                        .formatted(artistId))));
  }

  private void givenAlbumTracksResponse(String albumId) {
    wireMock.register(
        get(urlPathEqualTo("/v1/albums/" + albumId + "/tracks"))
            .withQueryParam("market", equalTo("DE"))
            .willReturn(
                okJson(
                    """
                    {
                      "href": "https://api.spotify.com/v1/albums/%s/tracks",
                      "items": [
                        {
                          "artists": [
                            {
                              "id": "artist1",
                              "name": "Artist 1"
                            }
                          ],
                          "id": "track1",
                          "name": "Track 1",
                          "track_number": 1,
                          "type": "track",
                          "uri": "spotify:track:track1",
                          "is_local": false,
                          "explicit": false
                        },
                        {
                          "artists": [
                            {
                              "id": "artist1",
                              "name": "Artist 1"
                            }
                          ],
                          "id": "track2",
                          "name": "Track 2",
                          "track_number": 2,
                          "type": "track",
                          "uri": "spotify:track:track2",
                          "is_local": false,
                          "explicit": false
                        }
                      ],
                      "limit": 50,
                      "next": null,
                      "offset": 0,
                      "previous": null,
                      "total": 2
                    }
                    """
                        .formatted(albumId))));
  }

  private void givenPaginatedArtistAlbumsResponse(String artistId) {
    // First page response
    wireMock.register(
        get(urlPathEqualTo("/v1/artists/" + artistId + "/albums"))
            .withQueryParam("market", equalTo("DE"))
            .withQueryParam("limit", equalTo("50"))
            .withQueryParam("offset", equalTo("0"))
            .willReturn(
                okJson(
                    """
                    {
                      "href": "https://api.spotify.com/v1/artists/%s/albums",
                      "items": [
                        {
                          "album_type": "album",
                          "id": "album1",
                          "name": "Old Album",
                          "release_date": "2023-01-01",
                          "total_tracks": 10,
                          "type": "album",
                          "album_group": "album",
                          "artists": [
                            {
                              "id": "artist1",
                              "name": "Test Artist"
                            }
                          ]
                        },
                        {
                          "album_type": "album",
                          "id": "album2",
                          "name": "Middle Album",
                          "release_date": "2023-06-01",
                          "total_tracks": 10,
                          "type": "album",
                          "album_group": "album",
                          "artists": [
                            {
                              "id": "artist1",
                              "name": "Test Artist"
                            }
                          ]
                        }
                      ],
                      "limit": 50,
                      "next": "https://api.spotify.com/v1/artists/%s/albums?offset=2&limit=50",
                      "offset": 0,
                      "previous": null,
                      "total": 3
                    }
                    """
                        .formatted(artistId, artistId))));

    // Second page response
    wireMock.register(
        get(urlPathEqualTo("/v1/artists/" + artistId + "/albums"))
            .withQueryParam("market", equalTo("DE"))
            .withQueryParam("limit", equalTo("50"))
            .withQueryParam("offset", equalTo("2"))
            .willReturn(
                okJson(
                    """
                    {
                      "href": "https://api.spotify.com/v1/artists/%s/albums",
                      "items": [
                        {
                          "album_type": "album",
                          "id": "album3",
                          "name": "Newest Album",
                          "release_date": "2024-01-01",
                          "total_tracks": 10,
                          "type": "album",
                          "album_group": "album",
                          "artists": [
                            {
                              "id": "artist1",
                              "name": "Test Artist"
                            }
                          ]
                        }
                      ],
                      "limit": 50,
                      "next": null,
                      "offset": 2,
                      "previous": "https://api.spotify.com/v1/artists/%s/albums?offset=0&limit=50",
                      "total": 3
                    }
                    """
                        .formatted(artistId, artistId))));
  }

  private static SpotifyApi buildSpotifyApiForLocalhost(int port) {
    return new SpotifyApi.Builder()
        .setScheme("http")
        .setHost("localhost")
        .setPort(port)
        .setAccessToken("test-token")
        .build();
  }
}
