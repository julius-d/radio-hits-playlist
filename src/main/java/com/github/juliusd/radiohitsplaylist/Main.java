package com.github.juliusd.radiohitsplaylist;

import com.github.juliusd.radiohitsplaylist.config.ConfigLoader;
import com.github.juliusd.radiohitsplaylist.config.Configuration;
import com.github.juliusd.radiohitsplaylist.config.ReCreateBerlinHitRadioPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.config.ReCreateFamilyRadioPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioLoader;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioLoader;
import com.github.juliusd.radiohitsplaylist.spotify.PlaylistShuffel;
import com.github.juliusd.radiohitsplaylist.spotify.PlaylistUpdater;
import com.github.juliusd.radiohitsplaylist.spotify.TrackFinder;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

  public static void main(String[] args) throws IOException, ParseException, SpotifyWebApiException {
    log("Start");
    log(LocalDateTime.now().toString());
    log("Version: " + PlaylistShuffel.class.getPackage().getImplementationVersion());

    var configuration = new ConfigLoader().loadConfig(System.getProperty("configFilePath"));

    var spotifyApi = buildSpotifyApi(configuration);
    var playlistShuffel = new PlaylistShuffel(spotifyApi);
    var berlinHitRadioLoader = new BerlinHitRadioClientConfiguration().berlinHitRadioLoader();
    var familyRadioLoader = new FamilyRadioClientConfiguration().familyRadioLoader();
    var playlistUpdater = new PlaylistUpdater(spotifyApi, new TrackFinder(spotifyApi));

    configuration.shuffleTasks().forEach(shuffleTaskConfiguration -> {
      playlistShuffel.moveFirst5TracksToTheEndOfThePlaylist(shuffleTaskConfiguration.playlistId());
    });

    configuration.reCreateFamilyRadioPlaylistTasks().forEach(task -> {
      refreshFamilyPlaylistFromSource(familyRadioLoader, playlistUpdater, task);
    });

    configuration.reCreateBerlinHitRadioPlaylistTasks().forEach(task -> {
      refreshPlaylistFromSource(berlinHitRadioLoader, playlistUpdater, task);
    });

    log("Done");
  }

  private static void refreshPlaylistFromSource(
    BerlinHitRadioLoader berlinHitRadioLoader,
    PlaylistUpdater playlistUpdater,
    ReCreateBerlinHitRadioPlaylistTaskConfiguration configuration
  ) {
    List<Track> tracks = berlinHitRadioLoader.load(configuration.streamName());
    playlistUpdater.update(tracks, configuration.playlistId(), configuration.descriptionPrefix());
    log("Refreshed " + configuration.streamName() + " with " + tracks.size() + " tracks");
  }

  private static void refreshFamilyPlaylistFromSource(
    FamilyRadioLoader familyRadioLoader,
    PlaylistUpdater playlistUpdater,
    ReCreateFamilyRadioPlaylistTaskConfiguration configuration
  ) {
    List<Track> tracks = familyRadioLoader.load(configuration.streamName());
    playlistUpdater.update(tracks, configuration.playlistId(), configuration.descriptionPrefix());
    log("Refreshed family radio " + configuration.streamName());
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

    log("New AccessToken expires in: " + authorizationCodeCredentials.getExpiresIn() + " seconds");
    return spotifyApi;
  }

  private static void log(String message) {
    System.out.println(message);
  }
}
