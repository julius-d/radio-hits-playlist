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
spotify:
  refreshToken: myRefreshToken
  clientId: myClientId
  clientSecret: myClientSecret
shuffleTasks:
  - playlistId: myPlaylistId0001
  - playlistId: myPlaylistId0002
  - playlistId: myPlaylistId0003
````
