package com.github.juliusd.radiohitsplaylist.spotify;

import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Image;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SpotifyTrackMapper {

  public static SpotifyTrack toSpotifyTrack(se.michaelthelin.spotify.model_objects.specification.Track track) {
    List<String> artists = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).toList();
    URI albumCover = Arrays.stream(track.getAlbum().getImages())
      .min(Comparator.comparingInt(image -> Math.abs(300 - image.getWidth())))
      .map(Image::getUrl).map(URI::create)
      .orElse(null);
    return new SpotifyTrack(track.getName(), artists, URI.create(track.getUri()), albumCover);
  }
}
