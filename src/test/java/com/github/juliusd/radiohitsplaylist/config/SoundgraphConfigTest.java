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
        assertThat(config.getTargetPlaylistId()).isEqualTo("your_target_playlist_id");
        assertThat(config.getSteps()).hasSize(3);

        // Verify first step (combine)
        SoundgraphConfig.Step firstStep = config.getSteps().get(0);
        assertThat(firstStep).isInstanceOf(SoundgraphConfig.CombineStep.class);
        SoundgraphConfig.CombineStep combineStep = (SoundgraphConfig.CombineStep) firstStep;
        assertThat(combineStep.getSources()).hasSize(2);

        // Verify first source (playlist)
        SoundgraphConfig.Source firstSource = combineStep.getSources().get(0);
        assertThat(firstSource).isInstanceOf(SoundgraphConfig.PlaylistSource.class);
        SoundgraphConfig.PlaylistSource playlistSource = (SoundgraphConfig.PlaylistSource) firstSource;
        assertThat(playlistSource.getPlaylistId()).isEqualTo("source_playlist_1");
        assertThat(playlistSource.getSteps()).hasSize(2);
        assertThat(playlistSource.getSteps().get(0)).isInstanceOf(SoundgraphConfig.ShuffleStep.class);
        assertThat(playlistSource.getSteps().get(1)).isInstanceOf(SoundgraphConfig.LimitStep.class);
        assertThat(((SoundgraphConfig.LimitStep) playlistSource.getSteps().get(1)).getValue()).isEqualTo(100);

        // Verify second source (album)
        SoundgraphConfig.Source secondSource = combineStep.getSources().get(1);
        assertThat(secondSource).isInstanceOf(SoundgraphConfig.AlbumSource.class);
        SoundgraphConfig.AlbumSource albumSource = (SoundgraphConfig.AlbumSource) secondSource;
        assertThat(albumSource.getAlbumId()).isEqualTo("source_album_1");
        assertThat(albumSource.getSteps()).hasSize(2);
        assertThat(albumSource.getSteps().get(0)).isInstanceOf(SoundgraphConfig.ShuffleStep.class);
        assertThat(albumSource.getSteps().get(1)).isInstanceOf(SoundgraphConfig.LimitStep.class);
        assertThat(((SoundgraphConfig.LimitStep) albumSource.getSteps().get(1)).getValue()).isEqualTo(50);

        // Verify second step (shuffle)
        SoundgraphConfig.Step secondStep = config.getSteps().get(1);
        assertThat(secondStep).isInstanceOf(SoundgraphConfig.ShuffleStep.class);

        // Verify third step (limit)
        SoundgraphConfig.Step thirdStep = config.getSteps().get(2);
        assertThat(thirdStep).isInstanceOf(SoundgraphConfig.LimitStep.class);
        assertThat(((SoundgraphConfig.LimitStep) thirdStep).getValue()).isEqualTo(150);
    }
} 