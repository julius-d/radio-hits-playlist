package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
import com.github.juliusd.radiohitsplaylist.monitoring.Notifier;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

public class PlaylistUpdater {
  private final SpotifyApi spotifyApi;
  private final TrackFinder trackFinder;
  private final TrackCache trackCache;
  private final Notifier notifier;

  public PlaylistUpdater(
      SpotifyApi spotifyApi, TrackFinder trackFinder, TrackCache trackCache, Notifier notifier) {
    this.spotifyApi = spotifyApi;
    this.trackFinder = trackFinder;
    this.trackCache = trackCache;
    this.notifier = notifier;
  }

  public void update(List<Track> tracks, String playlistId, String descriptionPrefix) {
    var spotifyTrackUris = findSpotifyTrackIds(tracks);
    storeOnPlayList(spotifyTrackUris, playlistId);
    updateDescription(playlistId, descriptionPrefix);
  }

  private void updateDescription(String playlistId, String descriptionPrefix) {
    try {
      if (descriptionPrefix == null || descriptionPrefix.trim().isEmpty()) {
        return;
      }

      String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
      spotifyApi
          .changePlaylistsDetails(playlistId)
          .description(descriptionPrefix.trim() + " " + today)
          .build()
          .execute();
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      throw new SpotifyException(e);
    }
  }

  private void storeOnPlayList(List<URI> spotifyTrackUris, String playlistId) {
    try {
      if (spotifyTrackUris.isEmpty()) {
        return;
      }

      List<String> trackUris = spotifyTrackUris.stream().map(URI::toString).toList();

      List<String> firstChunk = trackUris.subList(0, Math.min(100, trackUris.size()));
      JsonArray uris =
          new Gson()
              .toJsonTree(firstChunk, new TypeToken<List<String>>() {}.getType())
              .getAsJsonArray();

      spotifyApi.replacePlaylistsItems(playlistId, uris).build().execute();

      // Add remaining tracks in chunks of 100
      if (trackUris.size() > 100) {
        for (int i = 100; i < trackUris.size(); i += 100) {
          List<String> chunk = trackUris.subList(i, Math.min(i + 100, trackUris.size()));
          JsonArray chunkUris =
              new Gson()
                  .toJsonTree(chunk, new TypeToken<List<String>>() {}.getType())
                  .getAsJsonArray();

          spotifyApi.addItemsToPlaylist(playlistId, chunkUris).build().execute();
        }
      }
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      throw new SpotifyException(e);
    }
  }

  private List<URI> findSpotifyTrackIds(List<Track> tracks) {
    return tracks.stream()
        .map(this::findTrackInCacheOrViaSpotifyApi)
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<URI> findTrackInCacheOrViaSpotifyApi(Track track) {
    return trackCache
        .findTrack(track)
        .map(
            uri -> {
              notifier.recordCacheHit();
              return uri;
            })
        .or(
            () -> {
              notifier.recordCacheMiss();
              return trackFinder
                  .findSpotifyTrack(track)
                  .map(
                      spotifyTrack -> {
                        URI trackUri = spotifyTrack.uri();
                        if (isExactMatch(track, spotifyTrack)) {
                          trackCache.storeTrack(track, trackUri);
                        }
                        return trackUri;
                      });
            });
  }

  private static boolean isExactMatch(Track track, SpotifyTrack spotifyTrack) {
    return track.artist().equals(spotifyTrack.artists().stream().collect(Collectors.joining(" & ")))
        && track.title().equals(spotifyTrack.name());
  }
}
