package com.github.juliusd.radiohitsplaylist.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

public class SoundgraphConfig {
    @JsonProperty("targetPlaylist")
    private String targetPlaylistId;
    
    private List<Step> steps;

    public String getTargetPlaylistId() {
        return targetPlaylistId;
    }

    public void setTargetPlaylistId(String targetPlaylistId) {
        this.targetPlaylistId = targetPlaylistId;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = CombineStep.class, name = "combine"),
        @JsonSubTypes.Type(value = ShuffleStep.class, name = "shuffle"),
        @JsonSubTypes.Type(value = LimitStep.class, name = "limit")
    })
    public static abstract class Step {
        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "sourceType")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = PlaylistSource.class, name = "playlist"),
        @JsonSubTypes.Type(value = AlbumSource.class, name = "album")
    })
    public static abstract class Source {
        private String sourceType;
        private List<Step> steps;

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public List<Step> getSteps() {
            return steps;
        }

        public void setSteps(List<Step> steps) {
            this.steps = steps;
        }
    }

    public static class PlaylistSource extends Source {
        @JsonProperty("playlistId")
        private String playlistId;

        public PlaylistSource() {
            setSourceType("playlist");
        }

        public String getPlaylistId() {
            return playlistId;
        }

        public void setPlaylistId(String playlistId) {
            this.playlistId = playlistId;
        }
    }

    public static class AlbumSource extends Source {
        @JsonProperty("albumId")
        private String albumId;

        public AlbumSource() {
            setSourceType("album");
        }

        public String getAlbumId() {
            return albumId;
        }

        public void setAlbumId(String albumId) {
            this.albumId = albumId;
        }
    }

    public static class CombineStep extends Step {
        private List<Source> sources;

        public CombineStep() {
            setType("combine");
        }

        public List<Source> getSources() {
            return sources;
        }

        public void setSources(List<Source> sources) {
            this.sources = sources;
        }
    }

    public static class ShuffleStep extends Step {
        public ShuffleStep() {
            setType("shuffle");
        }
    }

    public static class LimitStep extends Step {
        private int value;

        public LimitStep() {
            setType("limit");
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
} 