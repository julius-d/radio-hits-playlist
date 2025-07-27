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
import com.github.juliusd.radiohitsplaylist.spotify.SpotifyApiConfiguration;
import com.github.juliusd.radiohitsplaylist.spotify.TrackCache;
import com.github.juliusd.radiohitsplaylist.spotify.TrackFinder;
import java.util.List;

public class Main {

  public static void main(String[] args) {

    var configuration = new ConfigLoader().loadConfig(System.getProperty("configFilePath"));
    var notifier = determineNotifier(configuration);
    try {
      notifier.runStarted();

      var trackCache = new TrackCache("track_cache.db");
      notifier.recordInitialCacheSize(trackCache.getCacheSize());

      executePlaylistTasks(configuration, notifier, trackCache);

      notifier.recordFinalCacheSize(trackCache.getCacheSize());

      notifier.runFinished();
    } catch (Exception e) {
      notifier.runFailed(e);
      throw e;
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
