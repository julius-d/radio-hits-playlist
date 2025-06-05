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
        assertThat(limitedTracks).hasSize(3);
        assertThat(limitedTracks.get(0).uri()).isEqualTo(URI.create("spotify:track:track1"));
        assertThat(limitedTracks.get(1).uri()).isEqualTo(URI.create("spotify:track:track2"));
        assertThat(limitedTracks.get(2).uri()).isEqualTo(URI.create("spotify:track:track3"));
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
        assertThat(limitedTracks).hasSize(2);
        assertThat(limitedTracks.get(0).uri()).isEqualTo(URI.create("spotify:track:track1"));
        assertThat(limitedTracks.get(1).uri()).isEqualTo(URI.create("spotify:track:track2"));
    }

}