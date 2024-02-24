package com.github.juliusd.radiohitsplaylist;

import com.github.juliusd.radiohitsplaylist.config.ConfigLoader;
import com.github.juliusd.radiohitsplaylist.config.ReCreateBerlinHitRadioPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.config.ReCreateFamilyRadioPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioLoader;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioLoader;
import com.github.juliusd.radiohitsplaylist.spotify.PlaylistShuffel;
import com.github.juliusd.radiohitsplaylist.spotify.PlaylistUpdater;
import com.github.juliusd.radiohitsplaylist.spotify.SpotifyApiConfiguration;
import com.github.juliusd.radiohitsplaylist.spotify.TrackFinder;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.juliusd.radiohitsplaylist.Logger.log;

public class Main {

  public static void main(String[] args) {
    log("Start");
    log(LocalDateTime.now().toString());
    log("Version: " + PlaylistShuffel.class.getPackage().getImplementationVersion());

    var configuration = new ConfigLoader().loadConfig(System.getProperty("configFilePath"));

    var spotifyApi = new SpotifyApiConfiguration().spotifyApi(configuration);
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

}
