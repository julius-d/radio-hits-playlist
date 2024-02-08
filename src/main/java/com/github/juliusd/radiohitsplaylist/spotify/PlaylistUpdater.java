package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.i18n.CountryCode;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PlaylistUpdater {
  private final SpotifyApi spotifyApi;

  public PlaylistUpdater(SpotifyApi spotifyApi) {

    this.spotifyApi = spotifyApi;
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
      spotifyApi
        .changePlaylistsDetails(playlistId)
        .description(descriptionPrefix + today)
        .build().execute();
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
      .flatMap(track -> findSpotifyTrack(track).stream())
      .toList();
  }

  private Optional<String> findSpotifyTrack(Track track) {
    String q1 = "artist:\"" + track.artist().replaceAll(" &", ",") + "\" track:\"" + track.title() + "\"";
    String q2 = track.title() + " " + track.artist() + " artist: " + track.artist() + " track: " + track.title();
    var song = execSearch(q1).or(() -> execSearch(q2));
    // System.out.println(track + " | " + q1 + "|" + q2 + "| " + song.map(it -> it.getHref()).orElse("-"));
    return song
      .map(se.michaelthelin.spotify.model_objects.specification.Track::getUri);
  }

  private Optional<se.michaelthelin.spotify.model_objects.specification.Track> execSearch(String q) {
    try {
      var searchTracksRequest = spotifyApi.searchTracks(q)
        .market(CountryCode.DE)
        .limit(2).build();
      var trackPaging = searchTracksRequest.execute();
      return Arrays.stream(trackPaging.getItems()).findFirst();
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      throw new SpotifyException(e);
    }
  }

}
