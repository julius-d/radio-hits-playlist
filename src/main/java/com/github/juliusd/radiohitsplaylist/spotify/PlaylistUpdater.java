package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PlaylistUpdater {
  private final SpotifyApi spotifyApi;
  private final TrackFinder trackFinder;

  public PlaylistUpdater(SpotifyApi spotifyApi, TrackFinder trackFinder) {
    this.spotifyApi = spotifyApi;
    this.trackFinder = trackFinder;
  }

  public void update(List<Track> tracks, String playlistId, String descriptionPrefix) {
    var limitedTracks = limitTrack(tracks);
    var spotifyTrackUris = findSpotifyTrackIds(limitedTracks);
    storeOnPlayList(spotifyTrackUris, playlistId);
    updateDescription(playlistId, descriptionPrefix);
  }

  /**
   * A max of 100 tracks can be replaced with one call.
   * See also <a href="https://developer.spotify.com/documentation/web-api/reference/reorder-or-replace-playlists-tracks">spotify API docu</a>
   */
  private List<Track> limitTrack(List<Track> tracks) {
    return tracks.stream().limit(100).toList();
  }

  private void updateDescription(String playlistId, String descriptionPrefix) {
    try {
      String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
      spotifyApi.changePlaylistsDetails(playlistId).description(descriptionPrefix.trim() + " " + today).build().execute();
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      throw new SpotifyException(e);
    }
  }

  private void storeOnPlayList(List<String> spotifyTrackUris, String playlistId) {
    try {
      JsonArray uris = new Gson().toJsonTree(spotifyTrackUris, new TypeToken<List<String>>() {
      }.getType()).getAsJsonArray();
      String result = spotifyApi
        .replacePlaylistsItems(playlistId, uris)
        .build()
        .execute();
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      throw new SpotifyException(e);
    }
  }

  private List<String> findSpotifyTrackIds(List<Track> tracks) {
    return tracks.stream()
      .flatMap(track -> trackFinder.findSpotifyTrack(track).stream())
      .toList();
  }

}
