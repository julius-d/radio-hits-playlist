package com.github.juliusd.radiohitsplaylist;

import static com.github.juliusd.radiohitsplaylist.Logger.log;

import com.github.juliusd.radiohitsplaylist.config.ConfigLoader;
import com.github.juliusd.radiohitsplaylist.config.Configuration;
import com.github.juliusd.radiohitsplaylist.config.ReCreateBerlinHitRadioPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.config.ReCreateBundesmuxPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.config.ReCreateFamilyRadioPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.config.ReCreateYoungPeoplePlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.monitoring.CompositeNotifier;
import com.github.juliusd.radiohitsplaylist.monitoring.GotifyClientConfiguration;
import com.github.juliusd.radiohitsplaylist.monitoring.LoggingNotifier;
import com.github.juliusd.radiohitsplaylist.monitoring.Notifier;
import com.github.juliusd.radiohitsplaylist.soundgraph.SoundgraphService;
import com.github.juliusd.radiohitsplaylist.soundgraph.SoundgraphSong;
import com.github.juliusd.radiohitsplaylist.soundgraph.SoundgraphSpotifyWrapper;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioLoader;
import com.github.juliusd.radiohitsplaylist.source.bundesmux.BundesmuxClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.bundesmux.BundesmuxLoader;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioLoader;
import com.github.juliusd.radiohitsplaylist.source.youngpeople.YoungPeopleClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.youngpeople.YoungPeopleLoader;
import com.github.juliusd.radiohitsplaylist.spotify.PlaylistShuffel;
import com.github.juliusd.radiohitsplaylist.spotify.PlaylistUpdater;
import com.github.juliusd.radiohitsplaylist.spotify.RefreshTokenWriter;
import com.github.juliusd.radiohitsplaylist.spotify.SpotifyApiConfiguration;
import com.github.juliusd.radiohitsplaylist.spotify.SpotifyInitialRefreshTokenCreator;
import com.github.juliusd.radiohitsplaylist.spotify.TrackCache;
import com.github.juliusd.radiohitsplaylist.spotify.TrackFinder;
import java.util.List;
import se.michaelthelin.spotify.SpotifyApi;

public class Main {

  public static void main(String[] args) {
    if (args.length > 0) {
      handleCliCommand(args);
      return;
    }

    var configuration = new ConfigLoader().loadConfig(System.getProperty("configFilePath"));
    var notifier = determineNotifier(configuration);
    try {
      notifier.runStarted();

      var trackCache = new TrackCache("track_cache_v2.db");
      notifier.recordInitialCacheSize(trackCache.getCacheSize());

      executePlaylistTasks(configuration, notifier, trackCache);

      notifier.recordFinalCacheSize(trackCache.getCacheSize());

      notifier.runFinished();
    } catch (Exception e) {
      notifier.runFailed(e);
      throw e;
    }
  }

  private static void handleCliCommand(String[] args) {
    var configFilePath = System.getProperty("configFilePath");
    if (configFilePath == null || configFilePath.isBlank()) {
      System.err.println("Error: -DconfigFilePath=<path> is required");
      System.exit(1);
    }
    var configuration = new ConfigLoader().loadConfig(configFilePath);
    var spotify = configuration.spotify();

    var spotifyApi =
        new SpotifyApi.Builder()
            .setClientId(spotify.clientId())
            .setClientSecret(spotify.clientSecret())
            .setRedirectUri(SpotifyInitialRefreshTokenCreator.REDIRECT_URI)
            .build();
    var tokenCreator = new SpotifyInitialRefreshTokenCreator(spotifyApi);

    switch (args[0]) {
      case "auth-url" -> {
        String url = tokenCreator.buildAuthorizationUrl();
        System.out.println("Open this URL in your browser to authorize:");
        System.out.println(url);
        System.out.println();
        System.out.println(
            "After authorizing, copy the 'code' parameter from the redirect URL, then run:");
        System.out.println(
            "  java -jar -DconfigFilePath=<path> radio-hits-playlist.jar update-token <code>");
      }
      case "update-token" -> {
        if (args.length < 2 || args[1].isBlank()) {
          System.err.println("Usage: update-token <authorization-code>");
          System.exit(1);
        }
        try {
          String newRefreshToken = tokenCreator.exchangeCodeForRefreshToken(args[1]);
          new RefreshTokenWriter().updateRefreshToken(configFilePath, newRefreshToken);
          System.out.println("Success. New refresh token written to " + configFilePath);
        } catch (Exception e) {
          throw new RuntimeException("Failed to exchange authorization code for refresh token", e);
        }
      }
      default -> {
        System.err.println("Unknown command: " + args[0]);
        System.err.println("Available commands: auth-url, update-token <code>");
        System.exit(1);
      }
    }
  }

  private static void executePlaylistTasks(
      Configuration configuration, Notifier notifier, TrackCache trackCache) {
    var spotifyApi = new SpotifyApiConfiguration().spotifyApi(configuration);
    var playlistShuffel = new PlaylistShuffel(spotifyApi);
    var playlistUpdater =
        new PlaylistUpdater(spotifyApi, new TrackFinder(spotifyApi), trackCache, notifier);
    var soundgraphSpotifyWrapper = new SoundgraphSpotifyWrapper(spotifyApi);
    var soundgraphService = new SoundgraphService(soundgraphSpotifyWrapper);

    configuration
        .shuffleTasks()
        .forEach(
            shuffleTaskConfiguration -> {
              playlistShuffel.moveFirst5TracksToTheEndOfThePlaylist(
                  shuffleTaskConfiguration.playlistId(), notifier);
            });

    // Family Radio tasks
    try {
      if (!configuration.reCreateFamilyRadioPlaylistTasks().isEmpty()) {
        var familyRadioLoader =
            new FamilyRadioClientConfiguration(configuration).familyRadioLoader();
        configuration
            .reCreateFamilyRadioPlaylistTasks()
            .forEach(
                task -> {
                  refreshFamilyPlaylistFromSource(
                      familyRadioLoader, playlistUpdater, task, notifier);
                });
      }
    } catch (Exception e) {
      notifier.runFailed("Family Radio tasks", e);
    }

    // Berlin Hit Radio tasks
    try {
      if (!configuration.reCreateBerlinHitRadioPlaylistTasks().isEmpty()) {
        var berlinHitRadioLoader = new BerlinHitRadioClientConfiguration().berlinHitRadioLoader();
        configuration
            .reCreateBerlinHitRadioPlaylistTasks()
            .forEach(
                task -> {
                  refreshPlaylistFromSource(berlinHitRadioLoader, playlistUpdater, task, notifier);
                });
      }
    } catch (Exception e) {
      notifier.runFailed("Berlin Hit Radio tasks", e);
    }

    // Young People tasks
    try {
      if (!configuration.reCreateYoungPeoplePlaylistTasks().isEmpty()) {
        var youngPeopleLoader =
            new YoungPeopleClientConfiguration(configuration).youngPeopleLoader();
        configuration
            .reCreateYoungPeoplePlaylistTasks()
            .forEach(
                task -> {
                  refreshYoungPeoplePlaylistFromSource(
                      youngPeopleLoader, playlistUpdater, task, notifier);
                });
      }
    } catch (Exception e) {
      notifier.runFailed("Young People tasks", e);
    }

    // Bundesmux tasks
    try {
      if (!configuration.reCreateBundesmuxPlaylistTasks().isEmpty()) {
        var bundesmuxLoader = new BundesmuxClientConfiguration(configuration).bundesmuxLoader();
        configuration
            .reCreateBundesmuxPlaylistTasks()
            .forEach(
                task -> {
                  refreshBundesmuxPlaylistFromSource(
                      bundesmuxLoader, playlistUpdater, task, notifier);
                });
      }
    } catch (Exception e) {
      notifier.runFailed("Bundesmux tasks", e);
    }

    // Soundgraph tasks
    try {
      configuration
          .soundgraphTasks()
          .forEach(
              task -> {
                try {
                  List<SoundgraphSong> tracks = soundgraphService.processSoundgraphConfig(task);
                  notifier.recordSoundgraphExecuted(task.name(), tracks.size());
                  log(
                      "Processed Soundgraph task for playlist "
                          + task.name()
                          + " with "
                          + tracks.size()
                          + " tracks");
                } catch (Exception e) {
                  throw new RuntimeException(
                      "Failed to process Soundgraph task for playlist " + task.name(), e);
                }
              });
    } catch (Exception e) {
      notifier.runFailed("Soundgraph tasks", e);
    }
  }

  private static Notifier determineNotifier(Configuration configuration) {
    if (configuration.gotify() != null) {
      Notifier gotifyNotifier = new GotifyClientConfiguration().notifier(configuration.gotify());
      return new CompositeNotifier(List.of(new LoggingNotifier(), gotifyNotifier));
    } else {
      return new LoggingNotifier();
    }
  }

  private static void refreshPlaylistFromSource(
      BerlinHitRadioLoader berlinHitRadioLoader,
      PlaylistUpdater playlistUpdater,
      ReCreateBerlinHitRadioPlaylistTaskConfiguration configuration,
      Notifier notifier) {
    List<Track> tracks = berlinHitRadioLoader.load(configuration.streamName());
    playlistUpdater.update(tracks, configuration.playlistId(), configuration.descriptionPrefix());
    notifier.recordPlaylistRefresh(configuration.streamName(), tracks.size());
    log("Refreshed " + configuration.streamName() + " with " + tracks.size() + " tracks");
  }

  private static void refreshFamilyPlaylistFromSource(
      FamilyRadioLoader familyRadioLoader,
      PlaylistUpdater playlistUpdater,
      ReCreateFamilyRadioPlaylistTaskConfiguration configuration,
      Notifier notifier) {
    List<Track> tracks =
        familyRadioLoader.load(
            configuration.channelId(),
            configuration.earliestSongTime(),
            configuration.trackLimit());
    playlistUpdater.update(tracks, configuration.playlistId(), configuration.descriptionPrefix());
    notifier.recordPlaylistRefresh(configuration.streamName(), tracks.size());
    log("Refreshed family radio " + configuration.streamName());
  }

  private static void refreshBundesmuxPlaylistFromSource(
      BundesmuxLoader bundesmuxLoader,
      PlaylistUpdater playlistUpdater,
      ReCreateBundesmuxPlaylistTaskConfiguration configuration,
      Notifier notifier) {
    List<Track> tracks = bundesmuxLoader.load(configuration.streamName());
    playlistUpdater.update(tracks, configuration.playlistId(), configuration.descriptionPrefix());
    notifier.recordPlaylistRefresh(configuration.streamName(), tracks.size());
    log("Refreshed bundesmux " + configuration.streamName());
  }

  private static void refreshYoungPeoplePlaylistFromSource(
      YoungPeopleLoader youngPeopleLoader,
      PlaylistUpdater playlistUpdater,
      ReCreateYoungPeoplePlaylistTaskConfiguration configuration,
      Notifier notifier) {
    List<Track> tracks = youngPeopleLoader.load(configuration.programName());
    playlistUpdater.update(tracks, configuration.playlistId(), configuration.descriptionPrefix());
    notifier.recordPlaylistRefresh(configuration.programName(), tracks.size());
    log(
        "Refreshed YoungPeople "
            + configuration.programName()
            + " with "
            + tracks.size()
            + " tracks");
  }
}
