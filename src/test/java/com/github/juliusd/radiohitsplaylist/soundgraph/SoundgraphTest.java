package com.github.juliusd.radiohitsplaylist.soundgraph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.juliusd.radiohitsplaylist.config.SoundgraphConfig;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.SpotifyApi;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest
class SoundgraphTest {

    private SoundgraphService soundgraphService;
    private WireMock wireMock;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        wireMock = wmRuntimeInfo.getWireMock();
        var spotifyApi = buildSpotifyApiForLocalhost(wmRuntimeInfo.getHttpPort());
        var soundgraphSpotifyWrapper = new SoundgraphSpotifyWrapper(spotifyApi);
        soundgraphService = new SoundgraphService(soundgraphSpotifyWrapper);
    }

    private void givenPlaylistTracksResponse(String playlistId) {
        String playlistResponse = //language=json
            """
            {
                "href": "https://api.spotify.com/v1/playlists/source_playlist_1/tracks",
                "limit": 20,
                "next": null,
                "offset": 0,
                "previous": null,
                "total": 3,
                "items": [
                    {
                        "added_at": "2024-01-01T00:00:00Z",
                        "track": {
                            "album": {
                                "album_type": "album",
                                "total_tracks": 10,
                                "id": "album1",
                                "name": "Album 1",
                                "release_date": "2024-01-01",
                                "release_date_precision": "day",
                                "type": "album",
                                "uri": "spotify:album:album1",
                                "artists": [
                                    {
                                        "external_urls": {
                                            "spotify": "https://open.spotify.com/artist/artist1"
                                        },
                                        "href": "https://api.spotify.com/v1/artists/artist1",
                                        "id": "artist1",
                                        "name": "Artist 1",
                                        "type": "artist",
                                        "uri": "spotify:artist:artist1"
                                    }
                                ]
                            },
                            "artists": [
                                {
                                    "external_urls": {
                                        "spotify": "https://open.spotify.com/artist/artist1"
                                    },
                                    "href": "https://api.spotify.com/v1/artists/artist1",
                                    "id": "artist1",
                                    "name": "Artist 1",
                                    "type": "artist",
                                    "uri": "spotify:artist:artist1"
                                }
                            ],
                            "disc_number": 1,
                            "duration_ms": 180000,
                            "explicit": false,
                            "external_ids": {
                                "isrc": "ISRC1"
                            },
                            "href": "https://api.spotify.com/v1/tracks/track1",
                            "id": "track1",
                            "is_playable": true,
                            "name": "Track 1",
                            "popularity": 80,
                            "preview_url": "https://p.scdn.co/mp3-preview/track1",
                            "track_number": 1,
                            "type": "track",
                            "uri": "spotify:track:track1",
                            "is_local": false
                        }
                    },
                    {
                        "added_at": "2024-01-01T00:00:00Z",
                        "is_local": false,
                        "track": {
                            "album": {
                                "album_type": "album",
                                "total_tracks": 10,
                                "href": "https://api.spotify.com/v1/albums/album2",
                                "id": "album2",
                                "name": "Album 2",
                                "release_date": "2024-01-01",
                                "release_date_precision": "day",
                                "type": "album",
                                "uri": "spotify:album:album2",
                                "artists": [
                                    {
                                        "external_urls": {
                                            "spotify": "https://open.spotify.com/artist/artist2"
                                        },
                                        "href": "https://api.spotify.com/v1/artists/artist2",
                                        "id": "artist2",
                                        "name": "Artist 2",
                                        "type": "artist",
                                        "uri": "spotify:artist:artist2"
                                    }
                                ]
                            },
                            "artists": [
                                {
                                    "external_urls": {
                                        "spotify": "https://open.spotify.com/artist/artist2"
                                    },
                                    "href": "https://api.spotify.com/v1/artists/artist2",
                                    "id": "artist2",
                                    "name": "Artist 2",
                                    "type": "artist",
                                    "uri": "spotify:artist:artist2"
                                }
                            ],
                            "disc_number": 1,
                            "duration_ms": 180000,
                            "explicit": false,
                            "external_ids": {
                                "isrc": "ISRC2"
                            },
                            "href": "https://api.spotify.com/v1/tracks/track2",
                            "id": "track2",
                            "is_playable": true,
                            "name": "Track 2",
                            "popularity": 80,
                            "preview_url": "https://p.scdn.co/mp3-preview/track2",
                            "track_number": 1,
                            "type": "track",
                            "uri": "spotify:track:track2",
                            "is_local": false
                        }
                    },
                    {
                        "added_at": "2024-01-01T00:00:00Z",
                        "is_local": false,
                        "track": {
                            "album": {
                                "album_type": "album",
                                "total_tracks": 10,
                                "href": "https://api.spotify.com/v1/albums/album3",
                                "id": "album3",
                                "name": "Album 3",
                                "release_date": "2024-01-01",
                                "release_date_precision": "day",
                                "type": "album",
                                "uri": "spotify:album:album3",
                                "artists": [
                                    {
                                        "external_urls": {
                                            "spotify": "https://open.spotify.com/artist/artist3"
                                        },
                                        "href": "https://api.spotify.com/v1/artists/artist3",
                                        "id": "artist3",
                                        "name": "Artist 3",
                                        "type": "artist",
                                        "uri": "spotify:artist:artist3"
                                    }
                                ]
                            },
                            "artists": [
                                {
                                    "external_urls": {
                                        "spotify": "https://open.spotify.com/artist/artist3"
                                    },
                                    "href": "https://api.spotify.com/v1/artists/artist3",
                                    "id": "artist3",
                                    "name": "Artist 3",
                                    "type": "artist",
                                    "uri": "spotify:artist:artist3"
                                }
                            ],
                            "disc_number": 1,
                            "duration_ms": 180000,
                            "explicit": false,
                            "external_ids": {
                                "isrc": "ISRC3"
                            },
                            "href": "https://api.spotify.com/v1/tracks/track3",
                            "id": "track3",
                            "is_playable": true,
                            "name": "Track 3",
                            "popularity": 80,
                            "track_number": 1,
                            "type": "track",
                            "uri": "spotify:track:track3",
                            "is_local": false
                        }
                    }
                ]
            }""";
        wireMock.register(stubFor(get(urlPathEqualTo("/v1/playlists/" + playlistId + "/tracks"))
            .willReturn(okJson(playlistResponse))));
    }

    private void givenAlbumTracksResponse(String albumId) {
        String albumResponse = //language=json
            """
            {
                "items": [
                    {
                        "uri": "spotify:track:album1",
                        "name": "Album Track 1",
                        "artists": [{"name":"Album Artist 1"}],
                        "explicit": true
                    },
                    {
                        "uri": "spotify:track:album2",
                        "name": "Album Track 2",
                        "artists": [{"name":"Album Artist 2"}],
                        "explicit": false
                    }
                ]
            }""";
        wireMock.register(stubFor(get(urlPathEqualTo("/v1/albums/" + albumId + "/tracks"))
            .willReturn(okJson(albumResponse))));
    }

    private void givenPlaylistUpdateWillBeAccepted(String playlistId) {
        wireMock.register(stubFor(put(urlPathEqualTo("/v1/playlists/" + playlistId + "/tracks"))
            .willReturn(okJson("{}"))));

        wireMock.register(stubFor(post(urlPathEqualTo("/v1/playlists/" + playlistId + "/tracks"))
            .willReturn(okJson("{}"))));
    }

    private void whenProcessSoundgraphConfig(String configYaml) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SoundgraphConfig config = mapper.readValue(configYaml, SoundgraphConfig.class);
        soundgraphService.processSoundgraphConfig(config);
    }

    @Test
    void shouldProcessSoundgraphConfigWithPlaylistAndAlbumSources() throws Exception {
        // given
        String configYaml = //language=yaml
            """
            targetPlaylist: "target_playlist_id"
            pipe:
              steps:
                - type: combine
                  sources:
                    - steps:
                        - type: loadPlaylist
                          playlistId: "source_playlist_1"
                        - type: limit
                          value: 2
                    - steps:
                        - type: loadAlbum
                          albumId: "source_album_1"
                        - type: limit
                          value: 1
                - type: shuffle
                - type: limit
                  value: 3
            """;

        givenPlaylistTracksResponse("source_playlist_1");
        givenAlbumTracksResponse("source_album_1");

        // Mock playlist update responses
        givenPlaylistUpdateWillBeAccepted("target_playlist_id");

        // when
        whenProcessSoundgraphConfig(configYaml);

        // then
        // Verify playlist tracks were fetched
        verify(getRequestedFor(urlPathEqualTo("/v1/playlists/source_playlist_1/tracks")));

        // Verify album tracks were fetched
        verify(getRequestedFor(urlPathEqualTo("/v1/albums/source_album_1/tracks")));

        // Verify tracks were replaced in playlist
        verify(putRequestedFor(urlPathEqualTo("/v1/playlists/target_playlist_id/tracks"))
            .withRequestBody(matchingJsonPath("$.uris", containing("spotify:track:track1")))
            .withRequestBody(matchingJsonPath("$.uris", containing("spotify:track:track2")))
            .withRequestBody(matchingJsonPath("$.uris", containing("spotify:track:album1"))));
    }

    @Test
    void shouldProcessSoundgraphConfigWithSinglePlaylistSource() throws Exception {
        // given
        String configYaml = //language=yaml
            """
            targetPlaylist: "target_playlist_id"
            pipe:
              steps:
                - type: loadPlaylist
                  playlistId: "source_playlist_1"
                - type: limit
                  value: 100
                - type: shuffle
            """;

        givenPlaylistTracksResponse("source_playlist_1");
        givenPlaylistUpdateWillBeAccepted("target_playlist_id");

        // when
        whenProcessSoundgraphConfig(configYaml);

        // then
        // Verify playlist tracks were fetched
        verify(getRequestedFor(urlPathEqualTo("/v1/playlists/source_playlist_1/tracks")));

        // Verify tracks were replaced in playlist
        verify(putRequestedFor(urlPathEqualTo("/v1/playlists/target_playlist_id/tracks"))
            .withRequestBody(matchingJsonPath("$.uris", containing("spotify:track:track1")))
            .withRequestBody(matchingJsonPath("$.uris", containing("spotify:track:track2")))
            .withRequestBody(matchingJsonPath("$.uris", containing("spotify:track:track3"))));
    }

    @Test
    void shouldRemoveDuplicateTracks() throws Exception {
        // given
        String configYaml = //language=yaml
            """
            targetPlaylist: "target_playlist_id"
            pipe:
              steps:
                - type: loadPlaylist
                  playlistId: "source_playlist_1"
                - type: dedup
            """;

        // Mock playlist response with duplicate tracks
        String playlistResponseWithDuplicates = //language=json
            """
            {
                "items": [
                    {
                        "added_at": "2024-01-01T00:00:00Z",
                        "track": {
                            "album": {
                                "album_type": "album",
                                "total_tracks": 10,
                                "id": "album1",
                                "name": "Album 1",
                                "release_date": "2024-01-01",
                                "release_date_precision": "day",
                                "type": "album",
                                "uri": "spotify:album:album1",
                                "artists": [
                                    {
                                        "external_urls": {
                                            "spotify": "https://open.spotify.com/artist/artist1"
                                        },
                                        "href": "https://api.spotify.com/v1/artists/artist1",
                                        "id": "artist1",
                                        "name": "Artist 1",
                                        "type": "artist",
                                        "uri": "spotify:artist:artist1"
                                    }
                                ]
                            },
                            "artists": [
                                {
                                    "external_urls": {
                                        "spotify": "https://open.spotify.com/artist/artist1"
                                    },
                                    "href": "https://api.spotify.com/v1/artists/artist1",
                                    "id": "artist1",
                                    "name": "Artist 1",
                                    "type": "artist",
                                    "uri": "spotify:artist:artist1"
                                }
                            ],
                            "disc_number": 1,
                            "duration_ms": 180000,
                            "explicit": false,
                            "external_ids": {
                                "isrc": "ISRC1"
                            },
                            "href": "https://api.spotify.com/v1/tracks/track1",
                            "id": "track1",
                            "is_playable": true,
                            "name": "Track 1",
                            "popularity": 80,
                            "preview_url": "https://p.scdn.co/mp3-preview/track1",
                            "track_number": 1,
                            "type": "track",
                            "uri": "spotify:track:track1",
                            "is_local": false
                        }
                    },
                    {
                        "added_at": "2024-01-01T00:00:00Z",
                        "track": {
                            "album": {
                                "album_type": "album",
                                "total_tracks": 10,
                                "id": "album1",
                                "name": "Album 1",
                                "release_date": "2024-01-01",
                                "release_date_precision": "day",
                                "type": "album",
                                "uri": "spotify:album:album1",
                                "artists": [
                                    {
                                        "external_urls": {
                                            "spotify": "https://open.spotify.com/artist/artist1"
                                        },
                                        "href": "https://api.spotify.com/v1/artists/artist1",
                                        "id": "artist1",
                                        "name": "Artist 1",
                                        "type": "artist",
                                        "uri": "spotify:artist:artist1"
                                    }
                                ]
                            },
                            "artists": [
                                {
                                    "external_urls": {
                                        "spotify": "https://open.spotify.com/artist/artist1"
                                    },
                                    "href": "https://api.spotify.com/v1/artists/artist1",
                                    "id": "artist1",
                                    "name": "Artist 1",
                                    "type": "artist",
                                    "uri": "spotify:artist:artist1"
                                }
                            ],
                            "disc_number": 1,
                            "duration_ms": 180000,
                            "explicit": false,
                            "external_ids": {
                                "isrc": "ISRC1"
                            },
                            "href": "https://api.spotify.com/v1/tracks/track1",
                            "id": "track1",
                            "is_playable": true,
                            "name": "Track 1",
                            "popularity": 80,
                            "preview_url": "https://p.scdn.co/mp3-preview/track1",
                            "track_number": 1,
                            "type": "track",
                            "uri": "spotify:track:track1",
                            "is_local": false
                        }
                    },
                    {
                        "added_at": "2024-01-01T00:00:00Z",
                        "track": {
                            "album": {
                                "album_type": "album",
                                "total_tracks": 10,
                                "id": "album2",
                                "name": "Album 2",
                                "release_date": "2024-01-01",
                                "release_date_precision": "day",
                                "type": "album",
                                "uri": "spotify:album:album2",
                                "artists": [
                                    {
                                        "external_urls": {
                                            "spotify": "https://open.spotify.com/artist/artist2"
                                        },
                                        "href": "https://api.spotify.com/v1/artists/artist2",
                                        "id": "artist2",
                                        "name": "Artist 2",
                                        "type": "artist",
                                        "uri": "spotify:artist:artist2"
                                    }
                                ]
                            },
                            "artists": [
                                {
                                    "external_urls": {
                                        "spotify": "https://open.spotify.com/artist/artist2"
                                    },
                                    "href": "https://api.spotify.com/v1/artists/artist2",
                                    "id": "artist2",
                                    "name": "Artist 2",
                                    "type": "artist",
                                    "uri": "spotify:artist:artist2"
                                }
                            ],
                            "disc_number": 1,
                            "duration_ms": 180000,
                            "explicit": false,
                            "external_ids": {
                                "isrc": "ISRC2"
                            },
                            "href": "https://api.spotify.com/v1/tracks/track2",
                            "id": "track2",
                            "is_playable": true,
                            "name": "Track 2",
                            "popularity": 80,
                            "preview_url": "https://p.scdn.co/mp3-preview/track2",
                            "track_number": 1,
                            "type": "track",
                            "uri": "spotify:track:track2",
                            "is_local": false
                        }
                    }
                ]
            }
            """;
        wireMock.register(stubFor(get(urlPathEqualTo("/v1/playlists/source_playlist_1/tracks"))
            .willReturn(okJson(playlistResponseWithDuplicates))));

        givenPlaylistUpdateWillBeAccepted("target_playlist_id");

        // when
        whenProcessSoundgraphConfig(configYaml);

        // then
        verify(getRequestedFor(urlPathEqualTo("/v1/playlists/source_playlist_1/tracks")));

        // Verify that only unique tracks were added to the playlist
        verify(putRequestedFor(urlPathEqualTo("/v1/playlists/target_playlist_id/tracks"))
            .withRequestBody(matchingJsonPath("$.uris", containing("spotify:track:track1")))
            .withRequestBody(matchingJsonPath("$.uris", containing("spotify:track:track2"))));
        
        // Verify that track1 appears only once
        verify(putRequestedFor(urlPathEqualTo("/v1/playlists/target_playlist_id/tracks"))
            .withRequestBody(matchingJsonPath("$.uris", not(containing("spotify:track:track1,spotify:track:track1")))));
    }

    @Test
    void shouldFilterOutExplicitTracks() throws Exception {
        // given
        String configYaml = //language=yaml
            """
            targetPlaylist: "target_playlist_id"
            pipe:
              steps:
                - type: loadPlaylist
                  playlistId: "source_playlist_1"
                - type: filterOutExplicit
            """;

        // Mock playlist response with explicit and non-explicit tracks
        String playlistResponseWithExplicitTracks = //language=json
            """
            {
                "items": [
                    {
                        "added_at": "2024-01-01T00:00:00Z",
                        "track": {
                            "album": {
                                "album_type": "album",
                                "total_tracks": 10,
                                "id": "album1",
                                "name": "Album 1",
                                "release_date": "2024-01-01",
                                "release_date_precision": "day",
                                "type": "album",
                                "uri": "spotify:album:album1",
                                "artists": [
                                    {
                                        "external_urls": {
                                            "spotify": "https://open.spotify.com/artist/artist1"
                                        },
                                        "href": "https://api.spotify.com/v1/artists/artist1",
                                        "id": "artist1",
                                        "name": "Artist 1",
                                        "type": "artist",
                                        "uri": "spotify:artist:artist1"
                                    }
                                ]
                            },
                            "artists": [
                                {
                                    "external_urls": {
                                        "spotify": "https://open.spotify.com/artist/artist1"
                                    },
                                    "href": "https://api.spotify.com/v1/artists/artist1",
                                    "id": "artist1",
                                    "name": "Artist 1",
                                    "type": "artist",
                                    "uri": "spotify:artist:artist1"
                                }
                            ],
                            "disc_number": 1,
                            "duration_ms": 180000,
                            "explicit": true,
                            "external_ids": {
                                "isrc": "ISRC1"
                            },
                            "href": "https://api.spotify.com/v1/tracks/track1",
                            "id": "track1",
                            "is_playable": true,
                            "name": "Track 1",
                            "popularity": 80,
                            "preview_url": "https://p.scdn.co/mp3-preview/track1",
                            "track_number": 1,
                            "type": "track",
                            "uri": "spotify:track:track1",
                            "is_local": false
                        }
                    },
                    {
                        "added_at": "2024-01-01T00:00:00Z",
                        "track": {
                            "album": {
                                "album_type": "album",
                                "total_tracks": 10,
                                "id": "album2",
                                "name": "Album 2",
                                "release_date": "2024-01-01",
                                "release_date_precision": "day",
                                "type": "album",
                                "uri": "spotify:album:album2",
                                "artists": [
                                    {
                                        "external_urls": {
                                            "spotify": "https://open.spotify.com/artist/artist2"
                                        },
                                        "href": "https://api.spotify.com/v1/artists/artist2",
                                        "id": "artist2",
                                        "name": "Artist 2",
                                        "type": "artist",
                                        "uri": "spotify:artist:artist2"
                                    }
                                ]
                            },
                            "artists": [
                                {
                                    "external_urls": {
                                        "spotify": "https://open.spotify.com/artist/artist2"
                                    },
                                    "href": "https://api.spotify.com/v1/artists/artist2",
                                    "id": "artist2",
                                    "name": "Artist 2",
                                    "type": "artist",
                                    "uri": "spotify:artist:artist2"
                                }
                            ],
                            "disc_number": 1,
                            "duration_ms": 180000,
                            "explicit": false,
                            "external_ids": {
                                "isrc": "ISRC2"
                            },
                            "href": "https://api.spotify.com/v1/tracks/track2",
                            "id": "track2",
                            "is_playable": true,
                            "name": "Track 2",
                            "popularity": 80,
                            "preview_url": "https://p.scdn.co/mp3-preview/track2",
                            "track_number": 1,
                            "type": "track",
                            "uri": "spotify:track:track2",
                            "is_local": false
                        }
                    }
                ]
            }
            """;
        wireMock.register(stubFor(get(urlPathEqualTo("/v1/playlists/source_playlist_1/tracks"))
            .willReturn(okJson(playlistResponseWithExplicitTracks))));

        givenPlaylistUpdateWillBeAccepted("target_playlist_id");

        // when
        whenProcessSoundgraphConfig(configYaml);

        // then
        verify(getRequestedFor(urlPathEqualTo("/v1/playlists/source_playlist_1/tracks")));

        // Verify that only non-explicit tracks were added to the playlist
        verify(putRequestedFor(urlPathEqualTo("/v1/playlists/target_playlist_id/tracks"))
            .withRequestBody(matchingJsonPath("$.uris", containing("spotify:track:track2"))));
        
        // Verify that explicit track was not added
        verify(putRequestedFor(urlPathEqualTo("/v1/playlists/target_playlist_id/tracks"))
            .withRequestBody(matchingJsonPath("$.uris", not(containing("spotify:track:track1")))));
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