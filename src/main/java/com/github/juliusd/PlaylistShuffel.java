package com.github.juliusd;

import com.neovisionaries.i18n.CountryCode;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.IOException;

public class PlaylistShuffel {

  public static void main(String[] args) throws IOException, ParseException, SpotifyWebApiException {
    System.out.println("Start");
    System.out.println("Version: " + PlaylistShuffel.class.getPackage().getImplementationVersion());

    String spotifyAccessToken = System.getProperty("spotifyAccessToken");
    if (spotifyAccessToken == null || spotifyAccessToken.isBlank()) {
      throw new RuntimeException("spotifyAccessToken is needed");
    }

    String spotifyRefreshToken = System.getProperty("spotifyRefreshToken");
    if (spotifyRefreshToken == null || spotifyRefreshToken.isBlank()) {
      throw new RuntimeException("spotifyRefreshToken is needed");
    }

    SpotifyApi spotifyApi = new SpotifyApi.Builder()
      .setAccessToken(spotifyAccessToken)
      .setRefreshToken(spotifyRefreshToken)
      .build();

    String playlistId = "***REMOVED***";
    var playlist = spotifyApi.getPlaylist(playlistId)
      .market(CountryCode.DE)
      .build().execute();

    printPlayList(playlist);
    Integer totalAmountOfTracks = playlist.getTracks().getTotal();
    moveFirst5TracksToTheEndOfThePlaylist(spotifyApi, playlistId, totalAmountOfTracks);


    System.out.println("Done");
  }

  private static void printPlayList(Playlist playlist) {
    System.out.println("Playlist: " + playlist.getName());
    for (PlaylistTrack track : playlist.getTracks().getItems()) {
      System.out.println(track.getTrack().getName() + " " + track.getTrack().getId());
    }
  }

  private static void moveFirst5TracksToTheEndOfThePlaylist(SpotifyApi spotifyApi, String playlistId, Integer totalAmountOfTracks) throws IOException, SpotifyWebApiException, ParseException {
    var reorderPlaylistsItemsRequest = spotifyApi.reorderPlaylistsItems(playlistId, 0, totalAmountOfTracks).range_length(5).build();
    reorderPlaylistsItemsRequest.execute();
  }

}
