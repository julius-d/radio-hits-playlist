package com.github.juliusd.radiohitsplaylist.soundgraph;

import com.github.juliusd.radiohitsplaylist.config.SoundgraphConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
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

    public List<SoundgraphSong> processSoundgraphConfig(SoundgraphConfig config) throws IOException, SpotifyWebApiException, ParseException {
        List<SoundgraphSong> tracks = processPipe(config.pipe());
        updatePlaylist(config.targetPlaylistId(), tracks);
        return tracks;
    }

    private List<SoundgraphSong> processCombineStep(SoundgraphConfig.CombineStep combineStep) throws IOException, SpotifyWebApiException, ParseException {
        List<SoundgraphSong> combinedTracks = new ArrayList<>();
        
        for (SoundgraphConfig.Pipe pipe : combineStep.sources()) {
            List<SoundgraphSong> sourceTracks = processPipe(pipe);
            combinedTracks.addAll(sourceTracks);
        }
        
        return combinedTracks;
    }

    private List<SoundgraphSong> processPipe(SoundgraphConfig.Pipe pipe) throws IOException, SpotifyWebApiException, ParseException {
        List<SoundgraphSong> tracks = new ArrayList<>();
        
        for (SoundgraphConfig.Step step : pipe.steps()) {
            if (step instanceof SoundgraphConfig.LoadPlaylistStep) {
                tracks = getPlaylistTracks(((SoundgraphConfig.LoadPlaylistStep) step).playlistId());
            } else if (step instanceof SoundgraphConfig.LoadAlbumStep) {
                tracks = getAlbumTracks(((SoundgraphConfig.LoadAlbumStep) step).albumId());
            } else if (step instanceof SoundgraphConfig.CombineStep) {
                tracks = processCombineStep((SoundgraphConfig.CombineStep) step);
            } else if (step instanceof SoundgraphConfig.ShuffleStep) {
                tracks = processShuffleStep(tracks);
            } else if (step instanceof SoundgraphConfig.LimitStep) {
                tracks = processLimitStep(tracks, ((SoundgraphConfig.LimitStep) step).value());
            } else if (step instanceof SoundgraphConfig.DedupStep) {
                tracks = processDedupStep(tracks);
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

    private List<SoundgraphSong> getPlaylistTracks(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        List<SoundgraphSong> tracks = new ArrayList<>();
        
        PlaylistTrack[] playlistTracks = spotifyApi.getPlaylistsItems(playlistId)
                .build()
                .execute()
                .getItems();
        
        for (PlaylistTrack playlistTrack : playlistTracks) {
            tracks.add(new SoundgraphSong(URI.create(playlistTrack.getTrack().getUri())));
        }
        
        return tracks;
    }

    private List<SoundgraphSong> getAlbumTracks(String albumId) throws IOException, SpotifyWebApiException, ParseException {
        List<SoundgraphSong> tracks = new ArrayList<>();
        
        TrackSimplified[] albumTracks = spotifyApi.getAlbumsTracks(albumId)
                .build()
                .execute()
                .getItems();
        
        for (TrackSimplified track : albumTracks) {
            tracks.add(new SoundgraphSong(URI.create(track.getUri())));
        }
        
        return tracks;
    }

    private void updatePlaylist(String playlistId, List<SoundgraphSong> tracks) throws IOException, SpotifyWebApiException, ParseException {
        // Only clear and add if there are tracks
        if (tracks.isEmpty()) {
            return;
        }

        // Convert SoundgraphSongs to URIs for the Spotify API
        List<String> trackUris = tracks.stream()
                .map(song -> song.uri().toString())
                .collect(Collectors.toList());

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