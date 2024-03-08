package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
import com.neovisionaries.i18n.CountryCode;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.function.Predicate.not;

public class TrackFinderAlternative {

  private final SpotifyApi spotifyApi;

  public TrackFinderAlternative(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }

  public Optional<SpotifyTrack> findSpotifyTrack(Track track) {
    String quoteQuery =
      "artist:\"" + track.artist().trim()
      + "\" track:\"" + track.title() + "\"";

//    String titleAndArtist = track.title() + " " + track.artist();
    String unquotedQuery = "artist:" + track.artist() + " track:" + track.title();
    String plainQuery = track.artist() + " " + track.title();
//    String unquotedQuery;
//    if (titleAndArtist.length() < 200) {
//      unquotedQuery = titleAndArtist + " artist:" + track.artist() + " track:" + track.title();
//    } else {
//      unquotedQuery = titleAndArtist;
//    }

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
      song = execSearch(quoteQuery).or(() -> execSearch(unquotedQuery)).or(() -> execSearch(plainQuery));
    }

    return song
      .map(this::toSpotifyTrack);
  }

  private SpotifyTrack toSpotifyTrack(se.michaelthelin.spotify.model_objects.specification.Track track) {
    List<String> artists = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).toList();
    return new SpotifyTrack(track.getName(), artists, URI.create(track.getUri()));
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
