# radio-hits-playlist 📻

![music machine](img/music-machine.jpeg)

# Usage

Download via https://github.com/julius-d/radio-hits-playlist/releases/latest/download/radio-hits-playlist.jar

```shell
wget -P /tmp/ --backups=1 https://github.com/julius-d/radio-hits-playlist/releases/latest/download/radio-hits-playlist.jar 
java -jar -DconfigFilePath=./config.yaml /tmp/radio-hits-playlist.jar  
```
And the config.yaml shall look like
```yaml
---
spotify:
  refreshToken: myRefreshToken
  clientId: myClientId
  clientSecret: myClientSecret
shuffleTasks:
  - playlistId: myPlaylistId0001
  - playlistId: myPlaylistId0002
  - playlistId: myPlaylistId0003
familyRadioUrl: https://example.org/f
reCreateFamilyRadioPlaylistTasks:
  - playlistId: targetPlaylistId4
    streamName: myStream1
    descriptionPrefix: my prefix
    channelId: 3bb7d791-128a-424f-9ef8-378bd426d833
    earliestSongTime: "06:30"
    trackLimit: 200
  - playlistId: targetPlaylistId5
    streamName: myStream2
    descriptionPrefix: my other prefix
    channelId: 3176e7c3-d821-4554-ac10-eeb66941c256
    earliestSongTime: "08:00"
    trackLimit: 100
reCreateBerlinHitRadioPlaylistTasks:
  - playlistId: targetPlaylistId6
    streamName: myHitStream1
    descriptionPrefix: my prefix2
  - playlistId: targetPlaylistId7
    streamName: myHitStream2
    descriptionPrefix: my other prefix2
bundesmuxUrl: https://example.org/b
reCreateBundesmuxPlaylistTasks:
  - playlistId: targetPlaylistId8
    streamName: myBundesStream1
    descriptionPrefix: my prefix7
  - playlistId: targetPlaylistId9
    streamName: myBundesStream2
    descriptionPrefix: my other prefix7
youngPeopleUrl: https://www.example.org/youngpeople
reCreateYoungPeoplePlaylistTasks:
  - playlistId: targetPlaylistId12
    programName: program/name
    descriptionPrefix: Young People Prefix
soundgraphTasks:
  # Simple example with a single playlist
  - name: "Simple Playlist Mix"
    targetPlaylistId: 6mN4kL7pQ9rT2v5x8yB3hJ
    descriptionPrefix: "Simple mix updated on"
    pipe:
      steps:
        - type: loadPlaylist
          playlistId: source_playlist_1
          name: "My Playlist"
        - type: limit
          value: 100
        - type: shuffle

  # Complex example combining multiple sources
  - name: "Mixed Sources Playlist"
    targetPlaylistId: 9wZ4yB7mN2kL5pQ8tR3vX6
    descriptionPrefix: "Mixed sources updated on"
    pipe:
      steps:
        - type: combine
          sources:
            - steps:
                - type: loadPlaylist
                  playlistId: 4hJ8kM2nP5qR7tV9wX3yB6
                  name: "Source Playlist"
                - type: shuffle
                - type: limit
                  value: 100
            - steps:
                - type: loadAlbum
                  albumId: source_album_1
                  name: "Source Album"
                - type: shuffle
                - type: limit
                  value: 50
        - type: shuffle
        - type: limit
          value: 150

  # Comprehensive example showing all available steps
  - name: "Complete Playlist Mix"
    targetPlaylistId: 7xK9mNp2QrL5vY8tH3jF4d
    descriptionPrefix: "Complete mix updated on"
    pipe:
      steps:
        - type: combine
          sources:
            # First source: Load from playlist, remove duplicates, filter explicit content
            - steps:
                - type: loadPlaylist
                  playlistId: 2bM5nR8sT9vX4cL7hJ3kP6
                  name: "Main Playlist"
                - type: dedup
                - type: filterOutExplicit
                - type: limit
                  value: 50
            # Second source: Load from album, shuffle, limit
            - steps:
                - type: loadAlbum
                  albumId: source_album_1
                  name: "Featured Album"
                - type: shuffle
                - type: limit
                  value: 30
        # Filter out artists from a denylist
        - type: filterArtistsFrom
          denylist:
            steps:
              - type: loadPlaylist
                playlistId: artists_to_avoid_playlist
                name: "Artists to Avoid"
        # Final processing steps
        - type: shuffle
        - type: dedup
        - type: filterOutExplicit
        - type: artistSeparation
        - type: limit
          value: 100
```
# Available Soundgraph Tasks Steps Reference 🎵

| Step                | Description                                | Required Parameters                    |
|---------------------|--------------------------------------------|----------------------------------------|
| `loadPlaylist`      | Loads tracks from a Spotify playlist       | `playlistId`, `name`                   |
| `loadAlbum`         | Loads tracks from a Spotify album          | `albumId`, `name`                       |
| `loadArtistTopTracks` | Loads top tracks from a Spotify artist   | `artistId`, `name`                     |
| `loadArtistNewestAlbum` | Loads tracks from an artist's newest album/release | `artistId`, `name`; Optional: `albumTypes` |
| `combine`           | Combines multiple sources of tracks        | `sources` (array of pipe configurations)|
| `shuffle`           | Randomizes the order of tracks             | None                                    |
| `limit`             | Limits the number of tracks                | `value` (integer)                       |
| `dedup`             | Removes duplicate tracks                   | None                                    |
| `filterOutExplicit` | Removes tracks marked as explicit          | None                                    |
| `filterArtistsFrom` | Filters out tracks from artists in denylist| `denylist` (pipe configuration)         |
| `artistSeparation`  | Ensures no consecutive tracks share artists | None                                    |

## Step Details

### `combine`
The `combine` step merges tracks from multiple sources using an interleaving strategy. This step allows you to create sophisticated playlists by blending content from different playlists, albums, and artists. The step works by:

- **Interleaving strategy**: Takes one track from each source in sequence, then repeats the cycle
- **Flexible sources**: Each source can be a complete pipe with its own processing steps
- **Handles unequal source sizes**: Continues adding tracks from remaining sources when shorter sources are exhausted

#### Interleaving Pattern:
If you have 3 sources with tracks:
- Source 1: [A1, A2, A3]
- Source 2: [B1, B2]
- Source 3: [C1, C2, C3, C4]

The result will be: [A1, B1, C1, A2, B2, C2, A3, C3, C4]


### `artistSeparation`
The `artistSeparation` step reorders tracks to ensure that no two consecutive songs are from the same artist(s). This creates a better listening experience by providing variety and preventing artist clustering in the playlist. The step works by:

- Maintaining all original tracks (no tracks are removed)
- Intelligently reordering to separate consecutive tracks from the same artist
- Handling multi-artist tracks (if any artist is shared between consecutive tracks, they will be separated)
- When all remaining tracks are from the same artist, it continues placement to avoid infinite loops

This step is particularly useful after shuffle operations or when combining multiple sources that might result in artist clustering.

### `loadArtistNewestAlbum`
The `loadArtistNewestAlbum` step loads all tracks from an artist's newest album or release. 
This step allows you to automatically include the latest content from your favorite artists without manually tracking their new releases.

#### Parameters:
- `artistId` (required): The Spotify artist ID
- `name` (required): A descriptive name for the step (used for logging)
- `albumTypes` (optional): Array of album types to consider. Defaults to `["album"]`
- `excludingAlbumsWithTitleContaining` (optional): Array of strings. If an album's title contains any of these strings, it will be excluded from consideration. Useful for excluding "Deluxe Edition", "Remastered", etc.

#### Supported Album Types:
- `"album"` - Full-length albums
- `"single"` - Singles and individual tracks  
- `"compilation"` - Compilation albums
- `"appears_on"` - Tracks that appear on compilations and various artist collections

#### Notes:
- Returns empty list if no matching albums are found
- Newest release is determined by Spotify's release date sorting
- When excluding albums by title, the step will select the newest non-excluded album
