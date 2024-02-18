package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
import com.neovisionaries.i18n.CountryCode;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static java.util.function.Predicate.not;

public class TrackFinder {

  private final SpotifyApi spotifyApi;

  public TrackFinder(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }

  public Optional<String> findSpotifyTrack(Track track) {
    String quoteQuery = "artist:\"" + track.artist().trim() + "\" track:\"" + track.title() + "\"";

    String titleAndArtist = track.title() + " " + track.artist();
    String unquotedQuery;
    if (titleAndArtist.length() < 100) {
      unquotedQuery = titleAndArtist + " artist: " + track.artist() + " track: " + track.title();
    } else {
      unquotedQuery = titleAndArtist;
    }

    Optional<se.michaelthelin.spotify.model_objects.specification.Track> song;
    if (track.artist().contains("&")) {
      var firstArtist = Arrays.stream(track.artist().split("&"))
        .filter(not(String::isBlank))
        .map(String::trim)
        .findFirst()
        .orElse(track.artist());
      String firstArtistQuoteQuery = "artist:\"" + firstArtist + "\" track:\"" + track.title() + "\"";
      song = execSearch(quoteQuery).or(() -> execSearch(firstArtistQuoteQuery)).or(() -> execSearch(unquotedQuery));
    } else {
      song = execSearch(quoteQuery).or(() -> execSearch(unquotedQuery));
    }

    return song
      .map(se.michaelthelin.spotify.model_objects.specification.Track::getUri);
  }

  private Optional<se.michaelthelin.spotify.model_objects.specification.Track> execSearch(String q) {
    try {
      var searchTracksRequest = spotifyApi.searchTracks(q)
        .market(CountryCode.DE)
        .limit(2).build();
      var trackPaging = searchTracksRequest.execute();
      return Arrays.stream(trackPaging.getItems()).findFirst();
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      throw new SpotifyException("Failed to search for " + q, e);
    }
  }

}