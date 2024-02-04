package com.github.juliusd;

import com.neovisionaries.i18n.CountryCode;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

public class PlaylistShuffel {

  private final SpotifyApi spotifyApi;

  public PlaylistShuffel(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }


  public void moveFirst5TracksToTheEndOfThePlaylist(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
    var playlist = spotifyApi.getPlaylist(playlistId)
      .market(CountryCode.DE)
      .build().execute();

    int totalAmountOfTracks = playlist.getTracks().getTotal();
    var reorderPlaylistsItemsRequest = spotifyApi.reorderPlaylistsItems(playlistId, 0, totalAmountOfTracks).range_length(5).build();
    reorderPlaylistsItemsRequest.execute();
    System.out.println("Shuffled Playlist: " + playlist.getName());
  }

}
