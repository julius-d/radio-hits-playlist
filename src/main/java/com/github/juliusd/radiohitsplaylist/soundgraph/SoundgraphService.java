package com.github.juliusd.radiohitsplaylist.soundgraph;

import com.github.juliusd.radiohitsplaylist.config.SoundgraphConfig;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SoundgraphService {
    private final SoundgraphSpotifyWrapper spotifyWrapper;

    public SoundgraphService(SoundgraphSpotifyWrapper soundgraphSpotifyWrapper) {
        this.spotifyWrapper = soundgraphSpotifyWrapper;
    }

    public List<SoundgraphSong> processSoundgraphConfig(SoundgraphConfig config) throws IOException, SpotifyWebApiException, ParseException {
        List<SoundgraphSong> tracks = processPipe(config.pipe());
        spotifyWrapper.updatePlaylist(config.targetPlaylistId(), tracks);
        return tracks;
    }

    private List<SoundgraphSong> processCombineStep(SoundgraphConfig.CombineStep combineStep) throws IOException, SpotifyWebApiException, ParseException {
        List<List<SoundgraphSong>> sourceTracksLists = new ArrayList<>();
        
        // First collect all source tracks
        for (SoundgraphConfig.Pipe pipe : combineStep.sources()) {
            List<SoundgraphSong> sourceTracks = processPipe(pipe);
            sourceTracksLists.add(sourceTracks);
        }
        
        // Interleave the tracks
        List<SoundgraphSong> combinedTracks = new ArrayList<>();
        int maxSize = sourceTracksLists.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);
                
        for (int i = 0; i < maxSize; i++) {
            for (List<SoundgraphSong> sourceTracks : sourceTracksLists) {
                if (i < sourceTracks.size()) {
                    combinedTracks.add(sourceTracks.get(i));
                }
            }
        }
        
        return combinedTracks;
    }

    private List<SoundgraphSong> processPipe(SoundgraphConfig.Pipe pipe) throws IOException, SpotifyWebApiException, ParseException {
        List<SoundgraphSong> tracks = new ArrayList<>();
        
        for (SoundgraphConfig.Step step : pipe.steps()) {
            if (step instanceof SoundgraphConfig.LoadPlaylistStep) {
                tracks = spotifyWrapper.getPlaylistTracks(((SoundgraphConfig.LoadPlaylistStep) step).playlistId());
            } else if (step instanceof SoundgraphConfig.LoadAlbumStep) {
                tracks = spotifyWrapper.getAlbumTracks(((SoundgraphConfig.LoadAlbumStep) step).albumId());
            } else if (step instanceof SoundgraphConfig.CombineStep) {
                tracks = processCombineStep((SoundgraphConfig.CombineStep) step);
            } else if (step instanceof SoundgraphConfig.ShuffleStep) {
                tracks = processShuffleStep(tracks);
            } else if (step instanceof SoundgraphConfig.LimitStep) {
                tracks = processLimitStep(tracks, ((SoundgraphConfig.LimitStep) step).value());
            } else if (step instanceof SoundgraphConfig.DedupStep) {
                tracks = processDedupStep(tracks);
            } else if (step instanceof SoundgraphConfig.FilterOutExplicitStep) {
                tracks = processFilterOutExplicitStep(tracks);
            } else if (step instanceof SoundgraphConfig.LoadArtistTopTracksStep) {
                tracks = spotifyWrapper.getArtistTopTracks(((SoundgraphConfig.LoadArtistTopTracksStep) step).artistId());
            }
        }
        
        return tracks;
    }

    private List<SoundgraphSong> processShuffleStep(List<SoundgraphSong> tracks) {
        List<SoundgraphSong> shuffledTracks = new ArrayList<>(tracks);
        Collections.shuffle(shuffledTracks);
        return shuffledTracks;
    }

    private List<SoundgraphSong> processLimitStep(List<SoundgraphSong> tracks, int limit) {
        return tracks.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<SoundgraphSong> processDedupStep(List<SoundgraphSong> tracks) {
        return tracks.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private List<SoundgraphSong> processFilterOutExplicitStep(List<SoundgraphSong> tracks) {
        return tracks.stream()
                .filter(song -> !song.explicit())
                .collect(Collectors.toList());
    }
} 