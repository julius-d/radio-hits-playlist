package com.github.juliusd.radiohitsplaylist;

import com.github.juliusd.radiohitsplaylist.config.ConfigLoader;
import com.github.juliusd.radiohitsplaylist.config.Configuration;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioLoader;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioLoader;
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

    var configuration = new ConfigLoader().loadConfig(System.getProperty("configFilePath"));

    var spotifyApi = buildSpotifyApi(configuration);
    var playlistShuffel = new PlaylistShuffel(spotifyApi);
    var berlinHitRadioLoader = new BerlinHitRadioClientConfiguration().berlinHitRadioLoader();
    var familyRadioLoader = new FamilyRadioClientConfiguration().familyRadioLoader();

    playlistShuffel.moveFirst5TracksToTheEndOfThePlaylist("***REMOVED***");
    playlistShuffel.moveFirst5TracksToTheEndOfThePlaylist("***REMOVED***");
    playlistShuffel.moveFirst5TracksToTheEndOfThePlaylist("***REMOVED***");


    refreshFamilyPlaylistFromSource(familyRadioLoader, spotifyApi, "teddy-live", "***REMOVED***", "Familien Radio Hits für Groß und Klein - aktualisiert am ");
    refreshFamilyPlaylistFromSource(familyRadioLoader, spotifyApi, "teddy-deutschpop", "***REMOVED***", "Deutsch-Pop Radio ohne Ende, für die ganze Familien - aktualisiert am ");
    refreshPlaylistFromSource(berlinHitRadioLoader, spotifyApi, "t40", "***REMOVED***", "Top Radio Hits aus Berlin - aktualisiert am ");
    refreshPlaylistFromSource(berlinHitRadioLoader, spotifyApi, "air", "***REMOVED***", "Radio Hits aus Berlin - aktualisiert am ");

    System.out.println("Done");
  }

  private static void refreshPlaylistFromSource(BerlinHitRadioLoader berlinHitRadioLoader, SpotifyApi spotifyApi, String streamName, String playlistId, String descriptionPrefix) {
    List<Track> tracks = berlinHitRadioLoader.load(streamName);
    var playlistUpdater = new PlaylistUpdater(spotifyApi);
    playlistUpdater.update(tracks, playlistId, descriptionPrefix);
    System.out.println("Refreshed " + streamName + " with " + tracks.size() + " tracks");
  }

  private static void refreshFamilyPlaylistFromSource(FamilyRadioLoader familyRadioLoader, SpotifyApi spotifyApi, String streamName, String playlistId, String descriptionPrefix) {
    List<Track> tracks = familyRadioLoader.load(streamName);
    var playlistUpdater = new PlaylistUpdater(spotifyApi);
    playlistUpdater.update(tracks, playlistId, descriptionPrefix);
    System.out.println("Refreshed family radio " + streamName);
  }

  private static SpotifyApi buildSpotifyApi(Configuration configuration) throws IOException, SpotifyWebApiException, ParseException {
    String spotifyRefreshToken = configuration.spotify().refreshToken();
    if (spotifyRefreshToken == null || spotifyRefreshToken.isBlank()) {
      throw new RuntimeException("spotifyRefreshToken is needed");
    }

    String clientSecret = configuration.spotify().clientSecret();
    if (clientSecret == null || clientSecret.isBlank()) {
      throw new RuntimeException("clientSecret is needed");
    }

    SpotifyApi spotifyApi = new SpotifyApi.Builder()
      .setRefreshToken(spotifyRefreshToken)
      .setClientId(configuration.spotify().clientId())
      .setClientSecret(clientSecret)
      .build();

    var authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
    var authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

    spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

    System.out.println("New AccessToken expires in: " + authorizationCodeCredentials.getExpiresIn() + " seconds");
    return spotifyApi;
  }
}
