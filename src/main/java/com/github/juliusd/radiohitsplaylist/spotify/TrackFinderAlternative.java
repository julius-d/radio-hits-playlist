package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
import com.neovisionaries.i18n.CountryCode;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.ArrayList;
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
    List<String> querries = new ArrayList<>();
    String quoteQuery = "artist:\"" + track.artist().trim() + "\" track:\"" + track.title() + "\"";
    querries.add(quoteQuery);

    if (track.artist().contains("&")) {
      var firstArtist = Arrays.stream(track.artist().split("&"))
        .filter(not(String::isBlank))
        .map(String::trim)
        .findFirst()
        .orElse(track.artist());
      String firstArtistQuoteQuery = "artist:\"" + firstArtist + "\" track:\"" + track.title() + "\"";
      querries.add(firstArtistQuoteQuery);
    }
    String unquotedQuery = "artist:" + track.artist() + " track:" + track.title();
    querries.add(unquotedQuery);
    String plainQuery = track.artist() + " " + track.title();
    querries.add(plainQuery);
      for (String query : querries) {
          var searchResult = execSearch(query);
          if (searchResult.isPresent()) {
            return  searchResult.map(SpotifyTrackMapper::toSpotifyTrack);
          }
      }
      return Optional.empty();
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
