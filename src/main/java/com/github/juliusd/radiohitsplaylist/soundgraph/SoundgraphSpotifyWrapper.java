package com.github.juliusd.radiohitsplaylist.soundgraph;

import static com.github.juliusd.radiohitsplaylist.Logger.log;

import com.github.juliusd.radiohitsplaylist.spotify.SpotifyException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.i18n.CountryCode;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.AlbumGroup;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Episode;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

public class SoundgraphSpotifyWrapper {
  private final SpotifyApi spotifyApi;

  public SoundgraphSpotifyWrapper(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }

  public List<SoundgraphSong> getPlaylistTracks(String playlistId) throws SpotifyException {
    try {
      List<SoundgraphSong> tracks = new ArrayList<>();

      PlaylistTrack[] playlistTracks =
          spotifyApi
              .getPlaylistsItems(playlistId)
              .market(CountryCode.DE)
              .build()
              .execute()
              .getItems();

      for (PlaylistTrack playlistTrack : playlistTracks) {
        if (playlistTrack.getTrack() instanceof Track track) {
          List<String> artistNames =
              Arrays.stream(track.getArtists())
                  .map(ArtistSimplified::getName)
                  .collect(Collectors.toList());
          tracks.add(
              new SoundgraphSong(
                  URI.create(track.getUri()), track.getIsExplicit(), track.getName(), artistNames));
        } else if (playlistTrack.getTrack() instanceof Episode episode) {
          tracks.add(
              new SoundgraphSong(
                  URI.create(episode.getUri()),
                  episode.getExplicit(),
                  episode.getName(),
                  List.of(episode.getShow().getPublisher())));
        }
      }
      return tracks;
    } catch (Exception e) {
      log("Error loading tracks from playlist " + playlistId + ": " + e.getMessage());
      throw new SpotifyException("Error loading tracks from playlist " + playlistId, e);
    }
  }

  public List<SoundgraphSong> getAlbumTracks(String albumId) throws SpotifyException {
    try {
      List<SoundgraphSong> tracks = new ArrayList<>();

      TrackSimplified[] albumTracks =
          spotifyApi.getAlbumsTracks(albumId).market(CountryCode.DE).build().execute().getItems();

      for (TrackSimplified track : albumTracks) {
        List<String> artistNames =
            Arrays.stream(track.getArtists())
                .map(ArtistSimplified::getName)
                .collect(Collectors.toList());
        tracks.add(
            new SoundgraphSong(
                URI.create(track.getUri()), track.getIsExplicit(), track.getName(), artistNames));
      }

      return tracks;
    } catch (Exception e) {
      log("Error loading tracks from album " + albumId + ": " + e.getMessage());
      throw new SpotifyException("Error loading tracks from album " + albumId, e);
    }
  }

  public List<SoundgraphSong> getArtistTopTracks(String artistId) throws SpotifyException {
    try {
      List<SoundgraphSong> tracks = new ArrayList<>();

      Track[] topTracks =
          spotifyApi.getArtistsTopTracks(artistId, CountryCode.DE).build().execute();

      for (Track track : topTracks) {
        List<String> artistNames =
            Arrays.stream(track.getArtists())
                .map(ArtistSimplified::getName)
                .collect(Collectors.toList());
        tracks.add(
            new SoundgraphSong(
                URI.create(track.getUri()), track.getIsExplicit(), track.getName(), artistNames));
      }

      return tracks;
    } catch (Exception e) {
      log("Error loading top tracks for artist " + artistId + ": " + e.getMessage());
      throw new SpotifyException("Error loading top tracks for artist " + artistId, e);
    }
  }

  public List<SoundgraphSong> getArtistNewestAlbumTracks(
      String artistId, List<AlbumType> albumTypes, List<String> excludingAlbumsWithTitleContaining)
      throws SpotifyException {
    try {
      // Default to "album" if no album types provided
      List<AlbumType> types =
          (albumTypes == null || albumTypes.isEmpty()) ? List.of(AlbumType.ALBUM) : albumTypes;

      var wantedSpotifyAlbumGroups =
          types.stream().map(this::mapToSpotifyAlbumGroup).collect(Collectors.toList());

      // Get all albums using pagination
      var response =
          spotifyApi
              .getArtistsAlbums(artistId)
              .market(CountryCode.DE)
              .limit(50)
              .offset(0)
              .build()
              .execute();

      var allAlbums = new ArrayList<AlbumSimplified>();
      allAlbums.addAll(Arrays.asList(response.getItems()));

      while (response.getNext() != null) {
        response =
            spotifyApi
                .getArtistsAlbums(artistId)
                .market(CountryCode.DE)
                .limit(50)
                .offset(allAlbums.size())
                .build()
                .execute();

        allAlbums.addAll(Arrays.asList(response.getItems()));
      }

      if (allAlbums.isEmpty()) {
        log("No albums found for artist " + artistId + " with types " + types);
        return List.of();
      }

      AlbumSimplified newestAlbum =
          allAlbums.stream()
              .filter(album -> wantedSpotifyAlbumGroups.contains(album.getAlbumGroup()))
              .filter(
                  album -> {
                    if (excludingAlbumsWithTitleContaining == null
                        || excludingAlbumsWithTitleContaining.isEmpty()) {
                      return true;
                    }
                    String albumName = album.getName().toLowerCase();
                    return excludingAlbumsWithTitleContaining.stream()
                        .map(String::toLowerCase)
                        .noneMatch(albumName::contains);
                  })
              .max((a1, a2) -> a1.getReleaseDate().compareTo(a2.getReleaseDate()))
              .orElseThrow(() -> new IllegalStateException("Could not determine newest album"));

      // Get tracks from the newest album
      String newestAlbumId = newestAlbum.getId();
      log(
          "Loading tracks from newest album: "
              + newestAlbum.getName()
              + " (ID: "
              + newestAlbumId
              + ", Type: "
              + newestAlbum.getAlbumGroup().name()
              + ")");

      return getAlbumTracks(newestAlbumId);

    } catch (Exception e) {
      log("Error loading newest album tracks for artist " + artistId + ": " + e.getMessage());
      throw new SpotifyException("Error loading newest album tracks for artist " + artistId, e);
    }
  }

  private AlbumGroup mapToSpotifyAlbumGroup(AlbumType it) {
    return switch (it) {
      case ALBUM -> AlbumGroup.ALBUM;
      case SINGLE -> AlbumGroup.SINGLE;
      case COMPILATION -> AlbumGroup.COMPILATION;
      case APPEARS_ON -> AlbumGroup.APPEARS_ON;
    };
  }

  public void updatePlaylist(String playlistId, List<SoundgraphSong> tracks)
      throws SpotifyException {
    updatePlaylist(playlistId, tracks, null);
  }

  public void updatePlaylist(
      String playlistId, List<SoundgraphSong> tracks, String descriptionPrefix)
      throws SpotifyException {
    try {
      // Only clear and add if there are tracks
      if (tracks.isEmpty()) {
        return;
      }

      // Convert SoundgraphSongs to URIs for the Spotify API
      List<String> trackUris =
          tracks.stream().map(song -> song.uri().toString()).collect(Collectors.toList());

      // Handle first 100 tracks with replacePlaylistsItems
      List<String> firstChunk = trackUris.subList(0, Math.min(100, trackUris.size()));
      JsonArray uris =
          new Gson()
              .toJsonTree(firstChunk, new TypeToken<List<String>>() {}.getType())
              .getAsJsonArray();

      spotifyApi.replacePlaylistsItems(playlistId, uris).build().execute();

      // Add remaining tracks in chunks of 100
      if (trackUris.size() > 100) {
        for (int i = 100; i < trackUris.size(); i += 100) {
          List<String> chunk = trackUris.subList(i, Math.min(i + 100, trackUris.size()));
          JsonArray chunkUris =
              new Gson()
                  .toJsonTree(chunk, new TypeToken<List<String>>() {}.getType())
                  .getAsJsonArray();

          spotifyApi.addItemsToPlaylist(playlistId, chunkUris).build().execute();
        }
      }

      // Update description if prefix is provided
      if (descriptionPrefix != null && !descriptionPrefix.trim().isEmpty()) {
        updateDescription(playlistId, descriptionPrefix);
      }
    } catch (Exception e) {
      log("Error updating playlist " + playlistId + ": " + e.getMessage());
      throw new SpotifyException("Error updating playlist " + playlistId, e);
    }
  }

  private void updateDescription(String playlistId, String descriptionPrefix) {
    try {
      String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
      spotifyApi
          .changePlaylistsDetails(playlistId)
          .description(descriptionPrefix.trim() + " " + today)
          .build()
          .execute();
    } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
      throw new SpotifyException(e);
    }
  }
}
