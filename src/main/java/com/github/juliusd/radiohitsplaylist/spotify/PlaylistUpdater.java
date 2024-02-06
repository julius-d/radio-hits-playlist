package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
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

  public void update(List<Track> tracks, String playlistId) {
    List<String> spotifyTrackUris = findSpotifyTrackIds(tracks);
    storeOnPlayList(spotifyTrackUris, playlistId);
    updateDescription(playlistId);
  }

  private void updateDescription(String playlistId) {
    try {
      String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
      spotifyApi
        .changePlaylistsDetails(playlistId)
        .description("Radio Hits  aus Berlin - aktualisiert am " + today)
        .build().execute();
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      throw new SpotifyException(e);
    }
  }

  private void storeOnPlayList(List<String> spotifyTrackUris, String playlistId) {
    try {
      spotifyApi
        .replacePlaylistsItems(playlistId, spotifyTrackUris.toArray(String[]::new))
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
    System.out.println(track + " | " + q1 + "|" + q2 + "| " + song.map(it -> it.getHref()).orElse("-"));
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
