package com.github.juliusd.radiohitsplaylist.soundgraph;

import com.github.juliusd.radiohitsplaylist.config.SoundgraphConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class SoundgraphService {
    private final SpotifyApi spotifyApi;

    public SoundgraphService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public void processSoundgraphConfig(SoundgraphConfig config) throws IOException, SpotifyWebApiException, ParseException {
        List<String> trackUris = processSteps(config);
        updatePlaylist(config.getTargetPlaylistId(), trackUris);
    }

    private List<String> processSteps(SoundgraphConfig config) throws IOException, SpotifyWebApiException, ParseException {
        List<String> trackUris = new ArrayList<>();
        
        for (SoundgraphConfig.Step step : config.getSteps()) {
            if (step instanceof SoundgraphConfig.CombineStep) {
                trackUris = processCombineStep((SoundgraphConfig.CombineStep) step);
            } else if (step instanceof SoundgraphConfig.ShuffleStep) {
                trackUris = processShuffleStep(trackUris);
            } else if (step instanceof SoundgraphConfig.LimitStep) {
                trackUris = processLimitStep(trackUris, ((SoundgraphConfig.LimitStep) step).getValue());
            }
        }
        
        return trackUris;
    }

    private List<String> processCombineStep(SoundgraphConfig.CombineStep combineStep) throws IOException, SpotifyWebApiException, ParseException {
        List<String> combinedTracks = new ArrayList<>();
        
        for (SoundgraphConfig.Source source : combineStep.getSources()) {
            List<String> sourceTracks;
            
            if (source instanceof SoundgraphConfig.PlaylistSource) {
                sourceTracks = getPlaylistTracks(((SoundgraphConfig.PlaylistSource) source).getPlaylistId());
            } else if (source instanceof SoundgraphConfig.AlbumSource) {
                sourceTracks = getAlbumTracks(((SoundgraphConfig.AlbumSource) source).getAlbumId());
            } else {
                throw new IllegalArgumentException("Unsupported source type: " + source.getSourceType());
            }
            
            // Process any nested steps
            if (source.getSteps() != null) {
                for (SoundgraphConfig.Step step : source.getSteps()) {
                    if (step instanceof SoundgraphConfig.ShuffleStep) {
                        sourceTracks = processShuffleStep(sourceTracks);
                    } else if (step instanceof SoundgraphConfig.LimitStep) {
                        sourceTracks = processLimitStep(sourceTracks, ((SoundgraphConfig.LimitStep) step).getValue());
                    }
                }
            }
            
            combinedTracks.addAll(sourceTracks);
        }
        
        return combinedTracks;
    }

    private List<String> processShuffleStep(List<String> tracks) {
        List<String> shuffledTracks = new ArrayList<>(tracks);
        Collections.shuffle(shuffledTracks);
        return shuffledTracks;
    }

    private List<String> processLimitStep(List<String> tracks, int limit) {
        return tracks.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<String> getPlaylistTracks(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        List<String> trackUris = new ArrayList<>();
        
        PlaylistTrack[] tracks = spotifyApi.getPlaylistsItems(playlistId)
                .build()
                .execute()
                .getItems();
        
        for (PlaylistTrack playlistTrack : tracks) {
            trackUris.add(playlistTrack.getTrack().getUri());
        }
        
        return trackUris;
    }

    private List<String> getAlbumTracks(String albumId) throws IOException, SpotifyWebApiException, ParseException {
        List<String> trackUris = new ArrayList<>();
        
        TrackSimplified[] tracks = spotifyApi.getAlbumsTracks(albumId)
                .build()
                .execute()
                .getItems();
        
        for (TrackSimplified track : tracks) {
            trackUris.add(track.getUri());
        }
        
        return trackUris;
    }

    private void updatePlaylist(String playlistId, List<String> trackUris) throws IOException, SpotifyWebApiException, ParseException {
        // Only clear and add if there are tracks
        if (trackUris.isEmpty()) {
            return;
        }

        // Handle first 100 tracks with replacePlaylistsItems
        List<String> firstChunk = trackUris.subList(0, Math.min(100, trackUris.size()));
        JsonArray uris = new Gson().toJsonTree(firstChunk, new TypeToken<List<String>>() {
        }.getType()).getAsJsonArray();

        spotifyApi.replacePlaylistsItems(playlistId, uris)
                .build()
                .execute();

        // Add remaining tracks in chunks of 100
        if (trackUris.size() > 100) {
            for (int i = 100; i < trackUris.size(); i += 100) {
                List<String> chunk = trackUris.subList(i, Math.min(i + 100, trackUris.size()));
                JsonArray chunkUris = new Gson().toJsonTree(chunk, new TypeToken<List<String>>() {
                }.getType()).getAsJsonArray();
                
                spotifyApi.addItemsToPlaylist(playlistId, chunkUris)
                        .build()
                        .execute();
            }
        }
    }
} 