package com.github.juliusd.radiohitsplaylist;

import com.github.juliusd.radiohitsplaylist.source.BerlinHitRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.spotify.PlaylistShuffel;
import com.github.juliusd.radiohitsplaylist.spotify.PlaylistUpdater;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.List;

public class Main {

  public static void main(String[] args) throws IOException, ParseException, SpotifyWebApiException {
    System.out.println("Start");
    System.out.println("Version: " + PlaylistShuffel.class.getPackage().getImplementationVersion());

    SpotifyApi spotifyApi = buildSpotifyApi();


    var playlistShuffel = new PlaylistShuffel(spotifyApi);

    playlistShuffel.moveFirst5TracksToTheEndOfThePlaylist("***REMOVED***");
    playlistShuffel.moveFirst5TracksToTheEndOfThePlaylist("***REMOVED***");
    playlistShuffel.moveFirst5TracksToTheEndOfThePlaylist("***REMOVED***");

    var berlinHitRadioLoader = new BerlinHitRadioClientConfiguration().berlinHitRadioLoader();
    List<Track> tracks = berlinHitRadioLoader.load();
    var playlistUpdater = new PlaylistUpdater(spotifyApi);
    playlistUpdater.update(tracks, "***REMOVED***");

    System.out.println("Done");
  }

  private static SpotifyApi buildSpotifyApi() throws IOException, SpotifyWebApiException, ParseException {
    String spotifyAccessToken = System.getProperty("spotifyAccessToken");

    String spotifyRefreshToken = System.getProperty("spotifyRefreshToken");
    if (spotifyRefreshToken == null || spotifyRefreshToken.isBlank()) {
      throw new RuntimeException("spotifyRefreshToken is needed");
    }

    String clientSecret = System.getProperty("spotifyClientSecret");
    if (clientSecret == null || clientSecret.isBlank()) {
      throw new RuntimeException("clientSecret is needed");
    }

    var clientId = "***REMOVED***";

    SpotifyApi spotifyApi = new SpotifyApi.Builder()
      .setAccessToken(spotifyAccessToken)
      .setRefreshToken(spotifyRefreshToken)
      .setClientId(clientId)
      .setClientSecret(clientSecret)
      .build();

    var authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
    var authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

    spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

    System.out.println("New AccessToken expires in: " + authorizationCodeCredentials.getExpiresIn() + " seconds");
    return spotifyApi;
  }
}