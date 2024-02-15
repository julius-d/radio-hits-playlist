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

import static java.util.function.Predicate.not;

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
        .description(descriptionPrefix.trim() + " " + today)
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
    String quoteQuery = "artist:\"" + track.artist().trim() + "\" track:\"" + track.title() + "\"";

    String titleAndArtist = track.title() + " " + track.artist();
    String unquotedQuery;
    if (titleAndArtist.length() < 100) {
      unquotedQuery = titleAndArtist + " artist: " + track.artist() + " track: " + track.title();
    } else {
      unquotedQuery = titleAndArtist;
    }

    Optional<se.michaelthelin.spotify.model_objects.specification.Track> song;
    if (track.artist().contains("&")) {
      var firstArtist = Arrays.stream(track.artist().split("&"))
        .filter(not(String::isBlank))
        .map(String::trim)
        .findFirst()
        .orElse(track.artist());
      String firstArtistQuoteQuery = "artist:\"" + firstArtist + "\" track:\"" + track.title() + "\"";
      song = execSearch(quoteQuery).or(() -> execSearch(firstArtistQuoteQuery)).or(() -> execSearch(unquotedQuery));
    } else {
      song = execSearch(quoteQuery).or(() -> execSearch(unquotedQuery));
    }

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
      throw new SpotifyException("Failed to search for " + q, e);
    }
  }

}
