package com.github.juliusd.radiohitsplaylist.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class SoundgraphConfigTest {

    @Test
    void shouldParseYamlConfig() throws Exception {
        // given
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        InputStream yamlStream = getClass().getResourceAsStream("/soundgraph-config.yaml");

        // when
        SoundgraphConfig config = mapper.readValue(yamlStream, SoundgraphConfig.class);

        // then
        assertThat(config.targetPlaylistId()).isEqualTo("your_target_playlist_id");
        assertThat(config.steps()).hasSize(3);

        // Verify first step (combine)
        SoundgraphConfig.Step firstStep = config.steps().get(0);
        assertThat(firstStep).isInstanceOf(SoundgraphConfig.CombineStep.class);
        SoundgraphConfig.CombineStep combineStep = (SoundgraphConfig.CombineStep) firstStep;
        assertThat(combineStep.sources()).hasSize(2);

        // Verify first source (playlist)
        SoundgraphConfig.Source firstSource = combineStep.sources().get(0);
        assertThat(firstSource).isInstanceOf(SoundgraphConfig.PlaylistSource.class);
        SoundgraphConfig.PlaylistSource playlistSource = (SoundgraphConfig.PlaylistSource) firstSource;
        assertThat(playlistSource.playlistId()).isEqualTo("source_playlist_1");
        assertThat(playlistSource.steps()).hasSize(2);
        assertThat(playlistSource.steps().get(0)).isInstanceOf(SoundgraphConfig.ShuffleStep.class);
        assertThat(playlistSource.steps().get(1)).isInstanceOf(SoundgraphConfig.LimitStep.class);
        assertThat(((SoundgraphConfig.LimitStep) playlistSource.steps().get(1)).value()).isEqualTo(100);

        // Verify second source (album)
        SoundgraphConfig.Source secondSource = combineStep.sources().get(1);
        assertThat(secondSource).isInstanceOf(SoundgraphConfig.AlbumSource.class);
        SoundgraphConfig.AlbumSource albumSource = (SoundgraphConfig.AlbumSource) secondSource;
        assertThat(albumSource.albumId()).isEqualTo("source_album_1");
        assertThat(albumSource.steps()).hasSize(2);
        assertThat(albumSource.steps().get(0)).isInstanceOf(SoundgraphConfig.ShuffleStep.class);
        assertThat(albumSource.steps().get(1)).isInstanceOf(SoundgraphConfig.LimitStep.class);
        assertThat(((SoundgraphConfig.LimitStep) albumSource.steps().get(1)).value()).isEqualTo(50);

        // Verify second step (shuffle)
        SoundgraphConfig.Step secondStep = config.steps().get(1);
        assertThat(secondStep).isInstanceOf(SoundgraphConfig.ShuffleStep.class);

        // Verify third step (limit)
        SoundgraphConfig.Step thirdStep = config.steps().get(2);
        assertThat(thirdStep).isInstanceOf(SoundgraphConfig.LimitStep.class);
        assertThat(((SoundgraphConfig.LimitStep) thirdStep).value()).isEqualTo(150);
    }
} 