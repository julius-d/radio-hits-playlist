package com.github.juliusd.radiohitsplaylist.spotify;

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
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

public class TrackFinderAlternative {

  private final SpotifyApi spotifyApi;

  public TrackFinderAlternative(SpotifyApi spotifyApi) {
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
    try {
      var searchTracksRequest = spotifyApi.searchTracks(q).market(CountryCode.DE).limit(5).build();
      var trackPaging = searchTracksRequest.execute();

      for (var spotifyTrack : trackPaging.getItems()) {
        if (isExactMatch(spotifyTrack, originalTrack)) {
          return Optional.of(spotifyTrack);
        }
      }

      return Arrays.stream(trackPaging.getItems()).findFirst();
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      throw new SpotifyException("Failed to search for " + q, e);
    }
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
        .replaceAll(" X ", " ")
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
