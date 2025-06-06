package com.github.juliusd.radiohitsplaylist.soundgraph;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.i18n.CountryCode;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Episode;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import org.apache.hc.core5.http.ParseException;

import static com.github.juliusd.radiohitsplaylist.Logger.log;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SoundgraphSpotifyWrapper {
    private final SpotifyApi spotifyApi;

    public SoundgraphSpotifyWrapper(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public List<SoundgraphSong> getPlaylistTracks(String playlistId)
            throws IOException, SpotifyWebApiException, ParseException {
        try {
            List<SoundgraphSong> tracks = new ArrayList<>();

            PlaylistTrack[] playlistTracks = spotifyApi.getPlaylistsItems(playlistId)
                    .market(CountryCode.DE)
                    .build()
                    .execute()
                    .getItems();

            for (PlaylistTrack playlistTrack : playlistTracks) {
                if (playlistTrack.getTrack() instanceof Track track) {
                    tracks.add(new SoundgraphSong(URI.create(track.getUri()), track.getIsExplicit()));
                } else if (playlistTrack.getTrack() instanceof Episode episode) {
                    tracks.add(new SoundgraphSong(URI.create(episode.getUri()), episode.getExplicit()));
                }
            }
            return tracks;
        } catch (Exception e) {
            log("Error loading tracks from playlist " + playlistId + ": " + e.getMessage());
            throw new RuntimeException("Error loading tracks from playlist " + playlistId, e);
        }
    }

    public List<SoundgraphSong> getAlbumTracks(String albumId)
            throws IOException, SpotifyWebApiException, ParseException {
        List<SoundgraphSong> tracks = new ArrayList<>();

        TrackSimplified[] albumTracks = spotifyApi.getAlbumsTracks(albumId)
                .market(CountryCode.DE)
                .build()
                .execute()
                .getItems();

        for (TrackSimplified track : albumTracks) {
            tracks.add(new SoundgraphSong(
                    URI.create(track.getUri()),
                    track.getIsExplicit()));
        }

        return tracks;
    }

    public void updatePlaylist(String playlistId, List<SoundgraphSong> tracks)
            throws IOException, SpotifyWebApiException, ParseException {
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