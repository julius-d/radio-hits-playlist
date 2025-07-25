package com.github.juliusd.radiohitsplaylist.soundgraph;

import com.github.juliusd.radiohitsplaylist.config.SoundgraphConfig;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

public class SoundgraphService {
  private final SoundgraphSpotifyWrapper spotifyWrapper;

  public SoundgraphService(SoundgraphSpotifyWrapper soundgraphSpotifyWrapper) {
    this.spotifyWrapper = soundgraphSpotifyWrapper;
  }

  public List<SoundgraphSong> processSoundgraphConfig(SoundgraphConfig config)
      throws IOException, SpotifyWebApiException, ParseException {
    List<SoundgraphSong> tracks = processPipe(config.pipe());
    spotifyWrapper.updatePlaylist(config.targetPlaylistId(), tracks, config.descriptionPrefix());
    return tracks;
  }

  private List<SoundgraphSong> processCombineStep(SoundgraphConfig.CombineStep combineStep)
      throws IOException, SpotifyWebApiException, ParseException {
    List<List<SoundgraphSong>> sourceTracksLists = new ArrayList<>();

    // First collect all source tracks
    for (SoundgraphConfig.Pipe pipe : combineStep.sources()) {
      List<SoundgraphSong> sourceTracks = processPipe(pipe);
      sourceTracksLists.add(sourceTracks);
    }

    // Interleave the tracks
    List<SoundgraphSong> combinedTracks = new ArrayList<>();
    int maxSize = sourceTracksLists.stream().mapToInt(List::size).max().orElse(0);

    for (int i = 0; i < maxSize; i++) {
      for (List<SoundgraphSong> sourceTracks : sourceTracksLists) {
        if (i < sourceTracks.size()) {
          combinedTracks.add(sourceTracks.get(i));
        }
      }
    }

    return combinedTracks;
  }

  private List<SoundgraphSong> processPipe(SoundgraphConfig.Pipe pipe)
      throws IOException, SpotifyWebApiException, ParseException {
    List<SoundgraphSong> tracks = new ArrayList<>();

    for (SoundgraphConfig.Step step : pipe.steps()) {
      if (step instanceof SoundgraphConfig.LoadPlaylistStep loadPlaylistStep) {
        tracks = spotifyWrapper.getPlaylistTracks(loadPlaylistStep.playlistId());
      } else if (step instanceof SoundgraphConfig.LoadAlbumStep loadAlbumStep) {
        tracks = spotifyWrapper.getAlbumTracks(loadAlbumStep.albumId());
      } else if (step instanceof SoundgraphConfig.CombineStep combineStep) {
        tracks = processCombineStep(combineStep);
      } else if (step instanceof SoundgraphConfig.ShuffleStep) {
        tracks = processShuffleStep(tracks);
      } else if (step instanceof SoundgraphConfig.LimitStep limitStep) {
        tracks = processLimitStep(tracks, limitStep.value());
      } else if (step instanceof SoundgraphConfig.DedupStep) {
        tracks = processDedupStep(tracks);
      } else if (step instanceof SoundgraphConfig.FilterOutExplicitStep) {
        tracks = processFilterOutExplicitStep(tracks);
      } else if (step instanceof SoundgraphConfig.LoadArtistTopTracksStep loadArtistTopTracksStep) {
        tracks = spotifyWrapper.getArtistTopTracks(loadArtistTopTracksStep.artistId());
      } else if (step instanceof SoundgraphConfig.FilterArtistsFromStep filterArtistsFromStep) {
        tracks = processFilterArtistsFromStep(tracks, filterArtistsFromStep);
      } else if (step instanceof SoundgraphConfig.ArtistSeparationStep) {
        tracks = processArtistSeparationStep(tracks);
      } else if (step
          instanceof SoundgraphConfig.LoadArtistNewestAlbumStep loadArtistNewestAlbumStep) {
        tracks =
            spotifyWrapper.getArtistNewestAlbumTracks(
                loadArtistNewestAlbumStep.artistId(),
                loadArtistNewestAlbumStep.albumTypes(),
                loadArtistNewestAlbumStep.excludingAlbumsWithTitleContaining());
      }
    }

    return tracks;
  }

  private List<SoundgraphSong> processShuffleStep(List<SoundgraphSong> tracks) {
    List<SoundgraphSong> shuffledTracks = new ArrayList<>(tracks);
    Collections.shuffle(shuffledTracks);
    return shuffledTracks;
  }

  private List<SoundgraphSong> processLimitStep(List<SoundgraphSong> tracks, int limit) {
    return tracks.stream().limit(limit).collect(Collectors.toList());
  }

  private List<SoundgraphSong> processDedupStep(List<SoundgraphSong> tracks) {
    return tracks.stream().distinct().collect(Collectors.toList());
  }

  private List<SoundgraphSong> processFilterOutExplicitStep(List<SoundgraphSong> tracks) {
    return tracks.stream().filter(song -> !song.explicit()).collect(Collectors.toList());
  }

  private List<SoundgraphSong> processFilterArtistsFromStep(
      List<SoundgraphSong> tracks, SoundgraphConfig.FilterArtistsFromStep step)
      throws IOException, SpotifyWebApiException, ParseException {
    // Get the denylist tracks to extract artist names
    List<SoundgraphSong> denylistTracks = processPipe(step.denylist());

    // Extract all artist names from the denylist
    Set<String> denylistArtists =
        denylistTracks.stream()
            .flatMap(song -> song.artists().stream())
            .collect(Collectors.toSet());

    // Filter out tracks that have any artist in the denylist
    return tracks.stream()
        .filter(song -> song.artists().stream().noneMatch(denylistArtists::contains))
        .collect(Collectors.toList());
  }

  private List<SoundgraphSong> processArtistSeparationStep(List<SoundgraphSong> tracks) {
    if (tracks.size() <= 1) {
      return tracks;
    }

    // Phase 1: Forward pass - standard greedy algorithm
    List<SoundgraphSong> result = greedyArtistSeparation(tracks);
    // Phase 2: Backward pass - resolve conflicts by working from the end
    return reverse(greedyArtistSeparation(reverse(result)));
  }

  private List<SoundgraphSong> reverse(List<SoundgraphSong> tracks) {
    List<SoundgraphSong> reversed = new ArrayList<>(tracks);
    Collections.reverse(reversed);
    return reversed;
  }

  private List<SoundgraphSong> greedyArtistSeparation(List<SoundgraphSong> tracks) {
    List<SoundgraphSong> result = new ArrayList<>();
    List<SoundgraphSong> remaining = new ArrayList<>(tracks);

    // Add the first track
    result.add(remaining.remove(0));

    while (!remaining.isEmpty()) {
      SoundgraphSong lastTrack = result.get(result.size() - 1);
      Set<String> lastArtists = new HashSet<>(lastTrack.artists());

      // Find the first track that doesn't share any artist with the last track
      SoundgraphSong nextTrack = null;
      int nextIndex = -1;

      for (int i = 0; i < remaining.size(); i++) {
        SoundgraphSong candidate = remaining.get(i);
        boolean sharesArtist = candidate.artists().stream().anyMatch(lastArtists::contains);

        if (!sharesArtist) {
          nextTrack = candidate;
          nextIndex = i;
          break;
        }
      }

      // If we found a track with different artists, use it
      if (nextTrack != null) {
        result.add(nextTrack);
        remaining.remove(nextIndex);
      } else {
        // If all remaining tracks share artists with the last track,
        // just take the next one in line to avoid infinite loop
        result.add(remaining.remove(0));
      }
    }

    return result;
  }
}
