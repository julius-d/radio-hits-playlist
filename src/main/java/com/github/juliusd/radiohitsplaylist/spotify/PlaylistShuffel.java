package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.monitoring.Notifier;
import com.neovisionaries.i18n.CountryCode;
import java.io.IOException;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

public class PlaylistShuffel {

  private final SpotifyApi spotifyApi;

  public PlaylistShuffel(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }

  public void moveFirst5TracksToTheEndOfThePlaylist(String playlistId, Notifier notifier) {
    try {
      var playlist = spotifyApi.getPlaylist(playlistId).market(CountryCode.DE).build().execute();

      int totalAmountOfTracks = playlist.getTracks().getTotal();
      var reorderPlaylistsItemsRequest =
          spotifyApi
              .reorderPlaylistsItems(playlistId, 0, totalAmountOfTracks)
              .range_length(5)
              .build();
      reorderPlaylistsItemsRequest.execute();
      System.out.println("Shuffled Playlist: " + playlist.getName());
      notifier.recordPlaylistShuffled(playlist.getName());
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      throw new SpotifyException(e);
    }
  }
}
