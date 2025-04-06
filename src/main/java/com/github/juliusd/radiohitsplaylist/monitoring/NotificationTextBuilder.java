package com.github.juliusd.radiohitsplaylist.monitoring;

import com.github.juliusd.radiohitsplaylist.Statistic;

import java.util.List;


public class NotificationTextBuilder {

  static String createMessageText(Statistic statistic) {
    StringBuilder messageText = new StringBuilder("Run finished successfully\n\n");

    List<String> shuffledPlaylists = statistic.getShuffledPlaylists();
    if (!shuffledPlaylists.isEmpty()) {
      messageText.append("Shuffled playlists (").append(shuffledPlaylists.size()).append("):\n");
      shuffledPlaylists.forEach(playlist -> messageText.append("- ").append(playlist).append("\n"));

      if (!statistic.getRefreshedPlaylists().isEmpty()) {
        messageText.append("\n");
      }
    }

    if (!statistic.getRefreshedPlaylists().isEmpty()) {
      messageText.append("Refreshed playlists (").append(statistic.getRefreshedPlaylists().size()).append("):\n");
      statistic.getRefreshedPlaylists().forEach(result ->
        messageText.append("- ").append(result.streamName())
          .append(": ").append(result.amountOfTracks())
          .append(" tracks\n"));
    }

    return messageText.toString();
  }

  static String createFailedMessageText(Throwable throwable) {
    String exceptionClassName = throwable.getClass().getSimpleName();

    StackTraceElement[] stackTrace = throwable.getStackTrace();
    StringBuilder stackTraceStr = new StringBuilder();

    int traceLimit = Math.min(3, stackTrace.length);
    for (int i = 0; i < traceLimit; i++) {
      stackTraceStr.append("\n  at ").append(stackTrace[i]);
    }

    if (stackTrace.length > traceLimit) {
      stackTraceStr.append("\n  ... ").append(stackTrace.length - traceLimit).append(" more");
    }

    String messageText = String.format("Run failed with %s: %s%s",
      exceptionClassName,
      throwable.getMessage(),
      stackTraceStr);
    return messageText;
  }
}
