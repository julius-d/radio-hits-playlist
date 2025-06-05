package com.github.juliusd.radiohitsplaylist;

import com.github.juliusd.radiohitsplaylist.config.ConfigLoader;
import com.github.juliusd.radiohitsplaylist.config.Configuration;
import com.github.juliusd.radiohitsplaylist.config.ReCreateBerlinHitRadioPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.config.ReCreateBundesmuxPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.config.ReCreateFamilyRadioPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.monitoring.GotifyClientConfiguration;
import com.github.juliusd.radiohitsplaylist.monitoring.NoOpNotifier;
import com.github.juliusd.radiohitsplaylist.monitoring.Notifier;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioLoader;
import com.github.juliusd.radiohitsplaylist.source.bundesmux.BundesmuxClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.bundesmux.BundesmuxLoader;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioLoader;
import com.github.juliusd.radiohitsplaylist.spotify.PlaylistShuffel;
import com.github.juliusd.radiohitsplaylist.spotify.PlaylistUpdater;
import com.github.juliusd.radiohitsplaylist.spotify.SpotifyApiConfiguration;
import com.github.juliusd.radiohitsplaylist.spotify.TrackFinder;
import com.github.juliusd.radiohitsplaylist.soundgraph.SoundgraphService;
import com.github.juliusd.radiohitsplaylist.soundgraph.SoundgraphSpotifyWrapper;
import com.github.juliusd.radiohitsplaylist.soundgraph.SoundgraphSong;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.juliusd.radiohitsplaylist.Logger.log;

public class Main {

  public static void main(String[] args) {
    log("Start");
    log(LocalDateTime.now().toString());
    log("Version: " + PlaylistShuffel.class.getPackage().getImplementationVersion());

    var configuration = new ConfigLoader().loadConfig(System.getProperty("configFilePath"));
    var notifier = determineNotifier(configuration);
    try {
      notifier.runStarted();
      executePlaylistTasks(configuration, notifier);
      notifier.runFinishedSuccessfully();
      log("Done");
    } catch (Exception e) {
      notifier.runFailed(e);
      throw e;
    }
  }

  private static void executePlaylistTasks(Configuration configuration, Notifier notifier) {
    var spotifyApi = new SpotifyApiConfiguration().spotifyApi(configuration);
    var playlistShuffel = new PlaylistShuffel(spotifyApi);
    var berlinHitRadioLoader = new BerlinHitRadioClientConfiguration().berlinHitRadioLoader();
    var familyRadioLoader = new FamilyRadioClientConfiguration().familyRadioLoader();
    var playlistUpdater = new PlaylistUpdater(spotifyApi, new TrackFinder(spotifyApi));
    var soundgraphSpotifyWrapper = new SoundgraphSpotifyWrapper(spotifyApi);
    var soundgraphService = new SoundgraphService(soundgraphSpotifyWrapper);

    configuration.shuffleTasks().forEach(shuffleTaskConfiguration -> {
      playlistShuffel.moveFirst5TracksToTheEndOfThePlaylist(shuffleTaskConfiguration.playlistId(), notifier);
    });

    configuration.reCreateFamilyRadioPlaylistTasks().forEach(task -> {
      refreshFamilyPlaylistFromSource(familyRadioLoader, playlistUpdater, task, notifier);
    });

    configuration.reCreateBerlinHitRadioPlaylistTasks().forEach(task -> {
      refreshPlaylistFromSource(berlinHitRadioLoader, playlistUpdater, task, notifier);
    });

    if (!configuration.reCreateBundesmuxPlaylistTasks().isEmpty()) {
      var bundesmuxLoader = new BundesmuxClientConfiguration(configuration).bundesmuxLoader();

      configuration.reCreateBundesmuxPlaylistTasks().forEach(task -> {
        refreshBundesmuxPlaylistFromSource(bundesmuxLoader, playlistUpdater, task, notifier);
      });
    }

    configuration.soundgraphTasks().forEach(task -> {
      try {
        List<SoundgraphSong> tracks = soundgraphService.processSoundgraphConfig(task);
        notifier.recordSoundgraphExecuted(task.targetPlaylistId(), tracks.size());
        log("Processed Soundgraph task for playlist " + task.targetPlaylistId() + " with " + tracks.size() + " tracks");
      } catch (Exception e) {
        throw new RuntimeException("Failed to process Soundgraph task for playlist " + task.targetPlaylistId(), e);
      }
    });
  }

  private static Notifier determineNotifier(Configuration configuration) {
    if (configuration.gotify() != null) {
      return new GotifyClientConfiguration().notifier(configuration.gotify());
    } else {
      return new NoOpNotifier();
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
    List<Track> tracks = familyRadioLoader.load(configuration.streamName());
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
}
