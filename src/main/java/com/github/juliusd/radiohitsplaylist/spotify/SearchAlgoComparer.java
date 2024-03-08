package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
import com.github.juliusd.radiohitsplaylist.config.ConfigLoader;
import com.github.juliusd.radiohitsplaylist.config.ReCreateBerlinHitRadioPlaylistTaskConfiguration;
import com.github.juliusd.radiohitsplaylist.source.berlinhitradio.BerlinHitRadioClientConfiguration;
import com.github.juliusd.radiohitsplaylist.source.family.FamilyRadioClientConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    var trackFinderAlternative = new TrackFinderAlternative(spotifyApi);

    String streamName = configuration.reCreateBerlinHitRadioPlaylistTasks().stream()
      .map(ReCreateBerlinHitRadioPlaylistTaskConfiguration::streamName)
      .findFirst()
      .orElseThrow();
    List<Track> tracks = berlinHitRadioLoader.load(streamName);

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
            tr.different {
             background-color: Red;
            }    
            tr.only-uri-different {
             background-color: #FFCCCB;
            }
            </style>
        </head>
        <body>
        <table>
        <tr>
          <th>#</th>
          <th>Given</th>
          <th>Algo 1</th>
          <th>Algo 1 Cover</th>
          <th>Algo 2</th>
          <th>Algo 2 Cover</th>
        </tr>
        """);

    AtomicInteger algo1Rating = new AtomicInteger(0);
    AtomicInteger algo2Rating = new AtomicInteger(0);


    tracks.forEach(withCounter((i, track) -> {

      var givenTrackTitle = track.title();
      var givenArtist = track.artist();
      var algo1spotifyTrack = trackFinder.findSpotifyTrack(track);
      var algo1result = getAlgoResult(algo1spotifyTrack, givenTrackTitle, givenArtist);
      var algo2spotifyTrack = trackFinderAlternative.findSpotifyTrack(track);
      var algo2result = getAlgoResult(algo2spotifyTrack, givenTrackTitle, givenArtist);

      boolean uriSameResult = algo1spotifyTrack.map(it -> it.uri().toString())
        .equals(algo2spotifyTrack.map(it -> it.uri().toString()));
      boolean trackAndTitleSameResult = algo1spotifyTrack.map(SpotifyTrack::artists)
                                          .equals(algo2spotifyTrack.map(SpotifyTrack::artists)) &&
                                        algo1spotifyTrack.map(SpotifyTrack::name)
                                          .equals(algo2spotifyTrack.map(SpotifyTrack::name));
      algo1Rating.addAndGet(algo1result.starRatingValue());
      algo2Rating.addAndGet(algo2result.starRatingValue());

      final String trClass;
      if (uriSameResult) {
        trClass = "same";
      } else if (trackAndTitleSameResult) {
        trClass = "only-uri-different";
      } else {
        trClass = "different";
      }
      System.out.println("Track " + i + " " + trClass);

      stringBuilder.append(
        """
        <tr class="%s">
          <td>%s</td>
          <td><b>%s</b><br>%s</td>
          <td><b>%s</b><br>%s<br>%s (%s)</td>
          <td><img src="%s" height="60" alt="cover"></td>
          <td><b>%s</b><br>%s<br>%s (%s)</td>
          <td><img src="%s" height="60" alt="cover"></td>
        </tr>
        """.formatted(
        trClass,
        String.valueOf(i),
        givenTrackTitle,
        givenArtist,
        algo1result.trackTitle(),
        algo1result.artists(),
        algo1result.starRating(),
        algo1result.levenshteinDistance(),
        algo1result.albumCover(),
        algo2result.trackTitle(),
        algo2result.artists(),
        algo2result.starRating(),
        algo2result.levenshteinDistance(),
        algo2result.albumCover()
        ));
    }));

    stringBuilder.append(
      """
         <tr>
          <td></td>
          <td></td>
          <td><b>%s</b></td>
          <td></td>
          <td><b>%s</b></td>
          <td></td>
        </tr>
        </table>
        </body>
        </html>
        """.formatted(
        algo1Rating.get(),
        algo2Rating.get()
      ));
    storeInFile(stringBuilder.toString());
  }

  private static AlgoResult getAlgoResult(Optional<SpotifyTrack> spotifyTrack, String givenTrackTitle, String givenArtist) {
    String foundTrackTitle = spotifyTrack.map(SpotifyTrack::name).orElse("-");
    String foundArtists = spotifyTrack.stream().flatMap(it -> it.artists().stream()).collect(Collectors.joining(", "));
    URI albumCover = spotifyTrack.map(SpotifyTrack::albumCover).orElse(null);
    boolean completeMatch = spotifyTrack.isPresent() && givenTrackTitle.equals(foundTrackTitle) && givenArtist.equals(foundArtists);
    boolean nearCompleteMatch = spotifyTrack.isPresent() && givenTrackTitle.equalsIgnoreCase(foundTrackTitle)
                                && normalize(givenArtist).equals(normalize(foundArtists));
    int levenshteinDistance =
      calculateLevenshteinDistance(normalize(givenTrackTitle), normalize(foundTrackTitle)) +
      calculateLevenshteinDistance(normalize(givenArtist), normalize(foundArtists));
    final String starRating;
    final int starRatingValue;
    if (completeMatch) {
      starRatingValue = 5;
      starRating = "⭐⭐⭐⭐⭐";
    } else if (nearCompleteMatch) {
      starRatingValue = 4;
      starRating = "⭐⭐⭐⭐❌";
    } else if (levenshteinDistance < 2) {
      starRatingValue = 3;
      starRating = "⭐⭐⭐❌❌";
    } else if (levenshteinDistance < 5) {
      starRatingValue = 2;
      starRating = "⭐⭐❌❌❌";
    } else if (levenshteinDistance < 10) {
      starRatingValue = 1;
      starRating = "⭐❌❌❌❌";
    } else {
      starRatingValue = 0;
      starRating = "❌❌❌❌❌";
    }
    return new AlgoResult(foundTrackTitle, foundArtists, albumCover, starRating, starRatingValue, levenshteinDistance);
  }

  private record AlgoResult(
    String trackTitle,
    String artists,
    URI albumCover,
    String starRating,
    int starRatingValue,
    int levenshteinDistance) {
  }

  private static String normalize(String artist) {
    return artist.trim().toLowerCase(Locale.ROOT)
      .replace(",", " ")
      .replace("&", " ")
      .replaceAll("\\s+", " ");

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

  private static <T> Consumer<T> withCounter(BiConsumer<Integer, T> consumer) {
    AtomicInteger counter = new AtomicInteger(0);
    return item -> consumer.accept(counter.getAndIncrement(), item);
  }

  static int calculateLevenshteinDistance(String first, String second) {
    int[][] dp = new int[first.length() + 1][second.length() + 1];

    for (int i = 0; i <= first.length(); i++) {
      for (int j = 0; j <= second.length(); j++) {
        if (i == 0) {
          dp[i][j] = j;
        } else if (j == 0) {
          dp[i][j] = i;
        } else {
          dp[i][j] = min(dp[i - 1][j - 1]
                         + costOfSubstitution(first.charAt(i - 1), second.charAt(j - 1)),
            dp[i - 1][j] + 1,
            dp[i][j - 1] + 1);
        }
      }
    }

    return dp[first.length()][second.length()];
  }


  public static int costOfSubstitution(char a, char b) {
    return a == b ? 0 : 1;
  }

  public static int min(int... numbers) {
    return Arrays.stream(numbers)
      .min().orElse(Integer.MAX_VALUE);
  }

}
