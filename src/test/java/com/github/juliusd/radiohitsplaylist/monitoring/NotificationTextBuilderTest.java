package com.github.juliusd.radiohitsplaylist.monitoring;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTextBuilderTest {

  @Nested
  class SuccessMessageTest {

    @Test
    void canCreateMessageText() {
      var statistic = new Statistic();
      statistic.recordPlaylistRefresh("myStream", 12);
      statistic.recordPlaylistRefresh("otherStream", 23);
      statistic.recordPlaylistShuffled("myPlaylist");

      String result = NotificationTextBuilder.createMessageText(statistic);

      String expected =
        """
        Run finished successfully after ???
        
        Shuffled playlists (1):
        - myPlaylist
        
        Refreshed playlists (2):
        - myStream: 12 tracks
        - otherStream: 23 tracks
        """;
      assertThat(result).startsWith(expected);
    }

    @Test
    void canCreateMessageTextWhenNoShuffledPlaylists() {
      var statistic = new Statistic();
      statistic.recordPlaylistRefresh("myStream", 12);
      statistic.recordPlaylistRefresh("otherStream", 23);

      String result = NotificationTextBuilder.createMessageText(statistic);

      String expected =
        """
        Run finished successfully after ???
        
        Refreshed playlists (2):
        - myStream: 12 tracks
        - otherStream: 23 tracks
        """;
      assertThat(result).isEqualTo(expected);
    }

    @Test
    void canCreateMessageTextWhenNoRefreshPlaylists() {
      var statistic = new Statistic();
      statistic.recordPlaylistShuffled("myPlaylist");

      String result = NotificationTextBuilder.createMessageText(statistic);

      String expected =
        """
        Run finished successfully after ???
        
        Shuffled playlists (1):
        - myPlaylist
        """;
      assertThat(result).isEqualTo(expected);
    }
  }

  @Nested
  class FailedMessageTest {

    @Test
    void canCreateFailedMessageText() {
      // Create an exception with null message
      Exception exception = new RuntimeException("Just a test");

      String result = NotificationTextBuilder.createFailedMessageText(exception);

      // Verify the result contains expected elements
      assertThat(result).startsWith("Run failed with RuntimeException: Just a test");
      assertThat(result).contains("at com.github.juliusd.radiohitsplaylist.monitoring.NotificationTextBuilderTest$FailedMessageTest");
    }

    @Test
    void limitedStackTraceElementsAreIncluded() {
      Exception exception = createDeepException();

      String result = NotificationTextBuilder.createFailedMessageText(exception);

      long stackTraceLines = result.lines()
        .filter(line -> line.trim().startsWith("at "))
        .count();

      assertThat(stackTraceLines).isEqualTo(3);
      assertThat(result).contains("... ");
    }

    private Exception createDeepException() {
      try {
        // Creating a nested exception with multiple stack frames
        return methodA();
      } catch (Exception e) {
        return e;
      }
    }

    private Exception methodA() {
      return methodB();
    }

    private Exception methodB() {
      return methodC();
    }

    private Exception methodC() {
      return methodD();
    }

    private Exception methodD() {
      return new RuntimeException("Deep nested exception");
    }
  }
}
