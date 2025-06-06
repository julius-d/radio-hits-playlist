package com.github.juliusd.radiohitsplaylist.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

public record SoundgraphConfig(
    @JsonProperty("name") String name,
    @JsonProperty("targetPlaylist") String targetPlaylistId,
    @JsonProperty("pipe") Pipe pipe
) {
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = LoadPlaylistStep.class, name = "loadPlaylist"),
        @JsonSubTypes.Type(value = LoadAlbumStep.class, name = "loadAlbum"),
        @JsonSubTypes.Type(value = CombineStep.class, name = "combine"),
        @JsonSubTypes.Type(value = ShuffleStep.class, name = "shuffle"),
        @JsonSubTypes.Type(value = LimitStep.class, name = "limit"),
        @JsonSubTypes.Type(value = DedupStep.class, name = "dedup"),
        @JsonSubTypes.Type(value = FilterOutExplicitStep.class, name = "filterOutExplicit"),
        @JsonSubTypes.Type(value = LoadArtistTopTracksStep.class, name = "loadArtistTopTracks"),
        @JsonSubTypes.Type(value = FilterArtistsFromStep.class, name = "filterArtistsFrom")
    })
    public sealed interface Step permits LoadPlaylistStep, LoadAlbumStep, CombineStep, ShuffleStep, LimitStep, DedupStep, FilterOutExplicitStep, LoadArtistTopTracksStep, FilterArtistsFromStep {
        String type();
    }

    public record Pipe(
        List<Step> steps
    ) {}

    public record LoadPlaylistStep(
        @JsonProperty("playlistId") String playlistId,
        @JsonProperty("name") String name
    ) implements Step {
        @Override
        public String type() {
            return "loadPlaylist";
        }
    }

    public record LoadAlbumStep(
        @JsonProperty("albumId") String albumId,
        @JsonProperty("name") String name
    ) implements Step {
        @Override
        public String type() {
            return "loadAlbum";
        }
    }

    public record CombineStep(
        List<Pipe> sources
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

    public record DedupStep() implements Step {
        @Override
        public String type() {
            return "dedup";
        }
    }

    public record FilterOutExplicitStep() implements Step {
        @Override
        public String type() {
            return "filterOutExplicit";
        }
    }

    public record LoadArtistTopTracksStep(
        @JsonProperty("artistId") String artistId,
        @JsonProperty("name") String name
    ) implements Step {
        @Override
        public String type() {
            return "loadArtistTopTracks";
        }
    }

    public record FilterArtistsFromStep(
        @JsonProperty("denylist") Pipe denylist
    ) implements Step {
        @Override
        public String type() {
            return "filterArtistsFrom";
        }
    }
}