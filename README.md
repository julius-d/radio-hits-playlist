# radio-hits-playlist 📻

![music machine](img/music-machine.jpeg)

# Usage

Download via https://github.com/julius-d/radio-hits-playlist/releases/latest/download/radio-hits-playlist.jar

```shell
wget -P /tmp/ --backups=1 https://github.com/julius-d/radio-hits-playlist/releases/latest/download/radio-hits-playlist.jar 
java -jar -DconfigFilePath=./config.yaml /tmp/radio-hits-playlist.jar  
```
And the config.yaml shall look like
````yaml
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
````
