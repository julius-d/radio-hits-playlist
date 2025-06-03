package com.github.juliusd.radiohitsplaylist.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

public record SoundgraphConfig(
    @JsonProperty("targetPlaylist") String targetPlaylistId,
    List<Step> steps
) {
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = CombineStep.class, name = "combine"),
        @JsonSubTypes.Type(value = ShuffleStep.class, name = "shuffle"),
        @JsonSubTypes.Type(value = LimitStep.class, name = "limit")
    })
    public sealed interface Step permits CombineStep, ShuffleStep, LimitStep {
        String type();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "sourceType")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = PlaylistSource.class, name = "playlist"),
        @JsonSubTypes.Type(value = AlbumSource.class, name = "album")
    })
    public sealed interface Source permits PlaylistSource, AlbumSource {
        String sourceType();
        List<Step> steps();
    }

    public record PlaylistSource(
        @JsonProperty("playlistId") String playlistId,
        List<Step> steps
    ) implements Source {
        @Override
        public String sourceType() {
            return "playlist";
        }
    }

    public record AlbumSource(
        @JsonProperty("albumId") String albumId,
        List<Step> steps
    ) implements Source {
        @Override
        public String sourceType() {
            return "album";
        }
    }

    public record CombineStep(
        List<Source> sources
    ) implements Step {
        @Override
        public String type() {
            return "combine";
        }
    }

    public record ShuffleStep() implements Step {
        @Override
        public String type() {
            return "shuffle";
        }
    }

    public record LimitStep(
        int value
    ) implements Step {
        @Override
        public String type() {
            return "limit";
        }
    }
} 