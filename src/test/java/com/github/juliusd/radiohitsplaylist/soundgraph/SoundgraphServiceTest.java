package com.github.juliusd.radiohitsplaylist.soundgraph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.juliusd.radiohitsplaylist.config.SoundgraphConfig;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import se.michaelthelin.spotify.SpotifyApi;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SoundgraphServiceTest {

    private SoundgraphService soundgraphService;
    @Mock
    private SoundgraphSpotifyWrapper soundgraphSpotifyWrapper;

    @BeforeEach
    void setUp() {
        soundgraphService = new SoundgraphService(soundgraphSpotifyWrapper);
    }

    @Test
    void shouldProcessLimitStep() throws Exception {
        // given
        List<SoundgraphSong> inputTracks = List.of(
                new SoundgraphSong(URI.create("spotify:track:track1"), false),
                new SoundgraphSong(URI.create("spotify:track:track2"), false),
                new SoundgraphSong(URI.create("spotify:track:track3"), false),
                new SoundgraphSong(URI.create("spotify:track:track4"), false),
                new SoundgraphSong(URI.create("spotify:track:track5"), false));

        when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id"))
                .thenReturn(inputTracks);

        // when
        List<SoundgraphSong> limitedTracks = soundgraphService.processSoundgraphConfig(
                new SoundgraphConfig(
                        "target_playlist_id",
                        new SoundgraphConfig.Pipe(List.of(
                                new SoundgraphConfig.LoadPlaylistStep("source_playlist_id"),
                                new SoundgraphConfig.LimitStep(3)))));

        // then
        assertThat(limitedTracks).hasSize(3).extracting(SoundgraphSong::uri)
                .containsExactly(
                        URI.create("spotify:track:track1"),
                        URI.create("spotify:track:track2"),
                        URI.create("spotify:track:track3"));
    }

    @Test
    void shouldHandleLimitGreaterThanInputSize() throws Exception {
        // given
        List<SoundgraphSong> inputTracks = List.of(
                new SoundgraphSong(URI.create("spotify:track:track1"), false),
                new SoundgraphSong(URI.create("spotify:track:track2"), false));

        when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id"))
                .thenReturn(inputTracks);

        // when
        List<SoundgraphSong> limitedTracks = soundgraphService.processSoundgraphConfig(
                new SoundgraphConfig(
                        "target_playlist_id",
                        new SoundgraphConfig.Pipe(List.of(
                                new SoundgraphConfig.LoadPlaylistStep("source_playlist_id"),
                                new SoundgraphConfig.LimitStep(5)))));

        // then
        assertThat(limitedTracks).hasSize(2).extracting(SoundgraphSong::uri)
                .containsExactly(
                        URI.create("spotify:track:track1"),
                        URI.create("spotify:track:track2"));
    }

    @Test
    void shouldFilterOutExplicitTracks() throws Exception {
        // given
        List<SoundgraphSong> inputTracks = List.of(
                new SoundgraphSong(URI.create("spotify:track:track1"), true), // explicit
                new SoundgraphSong(URI.create("spotify:track:track2"), false), // non-explicit
                new SoundgraphSong(URI.create("spotify:track:track3"), true), // explicit
                new SoundgraphSong(URI.create("spotify:track:track4"), false)); // non-explicit

        when(soundgraphSpotifyWrapper.getPlaylistTracks("source_playlist_id"))
                .thenReturn(inputTracks);

        // when
        List<SoundgraphSong> filteredTracks = soundgraphService.processSoundgraphConfig(
                new SoundgraphConfig(
                        "target_playlist_id",
                        new SoundgraphConfig.Pipe(List.of(
                                new SoundgraphConfig.LoadPlaylistStep("source_playlist_id"),
                                new SoundgraphConfig.FilterOutExplicitStep()))));

        // then
        assertThat(filteredTracks).hasSize(2).extracting(SoundgraphSong::uri)
                .containsExactly(
                        URI.create("spotify:track:track2"),
                        URI.create("spotify:track:track4"));
    }

}