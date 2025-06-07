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
    spotifyWrapper.updatePlaylist(config.targetPlaylistId(), tracks);
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
      if (step instanceof SoundgraphConfig.LoadPlaylistStep) {
        tracks =
            spotifyWrapper.getPlaylistTracks(
                ((SoundgraphConfig.LoadPlaylistStep) step).playlistId());
      } else if (step instanceof SoundgraphConfig.LoadAlbumStep) {
        tracks = spotifyWrapper.getAlbumTracks(((SoundgraphConfig.LoadAlbumStep) step).albumId());
      } else if (step instanceof SoundgraphConfig.CombineStep) {
        tracks = processCombineStep((SoundgraphConfig.CombineStep) step);
      } else if (step instanceof SoundgraphConfig.ShuffleStep) {
        tracks = processShuffleStep(tracks);
      } else if (step instanceof SoundgraphConfig.LimitStep) {
        tracks = processLimitStep(tracks, ((SoundgraphConfig.LimitStep) step).value());
      } else if (step instanceof SoundgraphConfig.DedupStep) {
        tracks = processDedupStep(tracks);
      } else if (step instanceof SoundgraphConfig.FilterOutExplicitStep) {
        tracks = processFilterOutExplicitStep(tracks);
      } else if (step instanceof SoundgraphConfig.LoadArtistTopTracksStep) {
        tracks =
            spotifyWrapper.getArtistTopTracks(
                ((SoundgraphConfig.LoadArtistTopTracksStep) step).artistId());
      } else if (step instanceof SoundgraphConfig.FilterArtistsFromStep) {
        tracks =
            processFilterArtistsFromStep(tracks, (SoundgraphConfig.FilterArtistsFromStep) step);
      } else if (step instanceof SoundgraphConfig.ArtistSeparationStep) {
        tracks = processArtistSeparationStep(tracks);
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
