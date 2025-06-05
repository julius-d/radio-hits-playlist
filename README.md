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
reCreateFamilyRadioPlaylistTasks:
  - playlistId: targetPlaylistId4
    streamName: myStream1
    descriptionPrefix: my prefix
  - playlistId: targetPlaylistId5
    streamName: myStream2
    descriptionPrefix: my other prefix
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
soundgraphTasks:
  # Simple example with a single playlist
  - targetPlaylist: targetPlaylistId10
    pipe:
      steps:
        - type: loadPlaylist
          playlistId: source_playlist_1
        - type: limit
          value: 100
        - type: shuffle

  # Complex example combining multiple sources
  - targetPlaylist: targetPlaylistId11
    pipe:
      steps:
        - type: combine
          sources:
            - steps:
                - type: loadPlaylist
                  playlistId: source_playlist_1
                - type: shuffle
                - type: limit
                  value: 100
            - steps:
                - type: loadAlbum
                  albumId: source_album_1
                - type: shuffle
                - type: limit
                  value: 50
        - type: shuffle
        - type: limit
          value: 150

  # Comprehensive example showing all available steps
  - targetPlaylist: targetPlaylistId12
    pipe:
      steps:
        - type: combine
          sources:
            # First source: Load from playlist, remove duplicates, filter explicit content
            - steps:
                - type: loadPlaylist
                  playlistId: source_playlist_1
                - type: dedup
                - type: filterOutExplicit
                - type: limit
                  value: 50
            # Second source: Load from album, shuffle, limit
            - steps:
                - type: loadAlbum
                  albumId: source_album_1
                - type: shuffle
                - type: limit
                  value: 30
        # Final processing steps
        - type: shuffle
        - type: dedup
        - type: filterOutExplicit
        - type: limit
          value: 100
```
# Available Steps Reference

| Step                | Description                                | Required Parameters                    |
|---------------------|--------------------------------------------|----------------------------------------|
| `loadPlaylist`      | Loads tracks from a Spotify playlist       | `playlistId` (string)                  |
| `loadAlbum`         | Loads tracks from a Spotify album          | `albumId` (string)                     |
| `combine`           | Combines multiple sources of tracks        | `sources` (array of pipe configurations)|
| `shuffle`           | Randomizes the order of tracks             | None                                    |
| `limit`             | Limits the number of tracks                | `value` (integer)                       |
| `dedup`             | Removes duplicate tracks                   | None                                    |
| `filterOutExplicit` | Removes tracks marked as explicit          | None                                    |
