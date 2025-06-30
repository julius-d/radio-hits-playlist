package com.github.juliusd.radiohitsplaylist.spotify;

import static com.github.juliusd.radiohitsplaylist.Logger.log;
import static java.util.function.Predicate.not;

import com.github.juliusd.radiohitsplaylist.Track;
import com.neovisionaries.i18n.CountryCode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.BadGatewayException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

public class TrackFinder {

  private final SpotifyApi spotifyApi;
  private static final int MAX_RETRIES = 3;
  private static final long INITIAL_RETRY_DELAY_MS = 1000;

  public TrackFinder(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }

  public Optional<SpotifyTrack> findSpotifyTrack(Track track) {
    List<String> querries = new ArrayList<>();
    String quoteQuery = "artist:\"" + track.artist().trim() + "\" track:\"" + track.title() + "\"";
    querries.add(quoteQuery);

    if (track.artist().contains("&")) {
      var firstArtist =
          Arrays.stream(track.artist().split("&"))
              .filter(not(String::isBlank))
              .map(String::trim)
              .findFirst()
              .orElse(track.artist());
      String firstArtistQuoteQuery =
          "artist:\"" + firstArtist + "\" track:\"" + track.title() + "\"";
      querries.add(firstArtistQuoteQuery);
    }
    String unquotedQuery = "artist:" + track.artist() + " track:" + track.title();
    querries.add(unquotedQuery);
    String plainQuery = buildPlainQuery(track);
    querries.add(plainQuery);
    for (String query : querries) {
      var searchResult = execSearch(query, track);
      if (searchResult.isPresent()) {
        return searchResult.map(SpotifyTrackMapper::toSpotifyTrack);
      }
    }
    return Optional.empty();
  }

  private String buildPlainQuery(Track track) {
    if (!track.title().trim().contains(" ") && track.artist().trim().contains(" ")) {
      return track.title() + " - " + track.artist();
    } else {
      return track.artist() + " " + track.title();
    }
  }

  private Optional<se.michaelthelin.spotify.model_objects.specification.Track> execSearch(
      String q, Track originalTrack) {
    int attempt = 0;
    Exception lastException = null;
    while (attempt <= MAX_RETRIES) {
      try {
        var searchTracksRequest =
            spotifyApi.searchTracks(q).market(CountryCode.DE).limit(5).build();
        var trackPaging = searchTracksRequest.execute();

        for (var spotifyTrack : trackPaging.getItems()) {
          if (isExactMatch(spotifyTrack, originalTrack)) {
            return Optional.of(spotifyTrack);
          }
        }

        return Arrays.stream(trackPaging.getItems()).findFirst();
      } catch (BadGatewayException e) {
        if (attempt < MAX_RETRIES) {
          attempt++;
          lastException = e;
          long delayMs = INITIAL_RETRY_DELAY_MS * (1L << (attempt - 1)); // Exponential backoff
          log(
              "Bad Gateway error on attempt "
                  + attempt
                  + " for query: "
                  + q
                  + ". Retrying in "
                  + delayMs
                  + "ms.");
          try {
            Thread.sleep(delayMs);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new SpotifyException("Search interrupted during retry for " + q, ie);
          }
        } else {
          throw new SpotifyException("Failed to search for " + q, e);
        }
      } catch (IOException | SpotifyWebApiException | ParseException e) {
        throw new SpotifyException("Failed to search for " + q, e);
      }
    }
    throw new SpotifyException(
        attempt + " retries searching for " + q + " failed. Last exception: " + lastException,
        lastException);
  }

  private boolean isExactMatch(
      se.michaelthelin.spotify.model_objects.specification.Track spotifyTrack, Track wantedTrack) {
    String foundTitle = normalizeTitle(spotifyTrack.getName());
    String wantedTitle = normalizeTitle(wantedTrack.title());

    if (!foundTitle.equals(wantedTitle)) {
      return false;
    }

    String wantedArtists = normalizeArtist(wantedTrack.artist());
    String foundArtists =
        normalizeArtist(
            Arrays.stream(spotifyTrack.getArtists())
                .map(ArtistSimplified::getName)
                .collect(Collectors.joining(" ")));

    return foundArtists.equals(wantedArtists);
  }

  private String normalizeTitle(String title) {
    return title.toLowerCase().trim();
  }

  private String normalizeArtist(String artist) {
    return artist
        .toLowerCase()
        .replaceAll(",", " ")
        .replaceAll("&", " ")
        .replaceAll(" x ", " ")
        .replaceAll(" feat ", " ")
        .replaceAll(" featuring ", " ")
        .replaceAll(" ft ", " ")
        .replaceAll(" ft. ", " ")
        .replaceAll(" feat. ", " ")
        .replaceAll(" featuring. ", " ")
        .replaceAll("\\s+", " ")
        .trim();
  }
}
