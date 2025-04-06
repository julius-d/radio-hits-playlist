package com.github.juliusd.radiohitsplaylist.monitoring;

import com.github.juliusd.radiohitsplaylist.Statistic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationTextBuilderTest {

  @Test
  void canCreateMessageText() {
    var statistic = new Statistic();
    statistic.recordPlaylistRefresh("myStream", 12);
    statistic.recordPlaylistRefresh("otherStream", 23);
    statistic.recordPlaylistShuffled("myPlaylist");

    String result = NotificationTextBuilder.createMessageText(statistic);

    String expected =
      """
      Run finished successfully
      
      Shuffled playlists (1):
      - myPlaylist
      
      Refreshed playlists (2):
      - myStream: 12 tracks
      - otherStream: 23 tracks
      """;
    assertEquals(expected, result);
  }

  @Test
  void canCreateMessageTextWhenNoShuffledPlaylists() {
    var statistic = new Statistic();
    statistic.recordPlaylistRefresh("myStream", 12);
    statistic.recordPlaylistRefresh("otherStream", 23);

    String result = NotificationTextBuilder.createMessageText(statistic);

    String expected =
      """
      Run finished successfully
      
      Refreshed playlists (2):
      - myStream: 12 tracks
      - otherStream: 23 tracks
      """;
    assertEquals(expected, result);
  }

  @Test
  void canCreateMessageTextWhenNoRefreshPlaylists() {
    var statistic = new Statistic();
    statistic.recordPlaylistShuffled("myPlaylist");

    String result = NotificationTextBuilder.createMessageText(statistic);

    String expected =
      """
      Run finished successfully
      
      Shuffled playlists (1):
      - myPlaylist
      """;
    assertEquals(expected, result);
  }
}
