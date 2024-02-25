package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
import com.github.juliusd.radiohitsplaylist.config.ConfigLoader;
import com.github.juliusd.radiohitsplaylist.config.ReCreateFamilyRadioPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioClientConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.juliusd.radiohitsplaylist.Logger.log;

public class SearchAlgoComparer {

  public static void main(String[] args) {
    log("Start SearchAlgoComparer");
    log(LocalDateTime.now().toString());

    var configuration = new ConfigLoader().loadConfig(System.getProperty("configFilePath"));
    var spotifyApi = new SpotifyApiConfiguration().spotifyApi(configuration);
    var berlinHitRadioLoader = new BerlinHitRadioClientConfiguration().berlinHitRadioLoader();
    var familyRadioLoader = new FamilyRadioClientConfiguration().familyRadioLoader();
    var trackFinder = new TrackFinder(spotifyApi);

    String streamName = configuration.reCreateFamilyRadioPlaylistTasks().stream()
      .map(ReCreateFamilyRadioPlaylistTaskConfiguration::streamName)
      .findFirst()
      .orElseThrow();
    List<Track> tracks = familyRadioLoader.load(streamName);

    StringBuilder stringBuilder = new StringBuilder(
      """
        <html>
        <head>
            <title>Search algo comparer</title>
            <meta charset="UTF-8">
            <style>
            table, th, td {
              border: 1px solid black;
              border-collapse: collapse;
            }
            tr:nth-child(odd) {
              background-color: LightGray;
            }
            </style>
        </head>
        <body>
        <table>
        """);


    tracks.forEach(track -> {
      stringBuilder.append("""
        <tr>
          <td>%s</td>
          <td>%s</td>
          <td>%s</td>
        </tr>
        """.formatted(
        track.title(),
        track.artist(),
        "TODO"
      ));
    });

    stringBuilder.append(
      """
        </table>
        </body>
        </html>
        """);
    storeInFile(stringBuilder.toString());
  }

  private static void storeInFile(String string) {
    try {
      Path path = Path.of("target", "searchAlgoComparer.html");
      createFileIfNotExists(path);
      Files.write(path, string.getBytes());
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  private static void createFileIfNotExists(Path path) throws IOException {
    try {
      Files.createFile(path);
    } catch (FileAlreadyExistsException ignored) {
    }
  }

}
