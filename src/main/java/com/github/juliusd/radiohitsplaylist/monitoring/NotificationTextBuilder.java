package com.github.juliusd.radiohitsplaylist.monitoring;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

class NotificationTextBuilder {

  static String createMessageText(Statistic statistic) {
    var durationText = getDurationText(statistic);

    StringBuilder messageText =
        new StringBuilder("Run finished successfully after ").append(durationText).append("\n\n");

    List<String> shuffledPlaylists = statistic.getShuffledPlaylists();
    if (!shuffledPlaylists.isEmpty()) {
      messageText.append("Shuffled playlists (").append(shuffledPlaylists.size()).append("):\n");
      shuffledPlaylists.forEach(playlist -> messageText.append("- ").append(playlist).append("\n"));

      if (!statistic.getRefreshedPlaylists().isEmpty()
          || !statistic.getSoundgraphResults().isEmpty()) {
        messageText.append("\n");
      }
    }

    if (!statistic.getRefreshedPlaylists().isEmpty()) {
      messageText
          .append("Refreshed playlists (")
          .append(statistic.getRefreshedPlaylists().size())
          .append("):\n");
      statistic
          .getRefreshedPlaylists()
          .forEach(
              result ->
                  messageText
                      .append("- ")
                      .append(result.streamName())
                      .append(": ")
                      .append(result.amountOfTracks())
                      .append(" tracks\n"));

      if (!statistic.getSoundgraphResults().isEmpty()) {
        messageText.append("\n");
      }
    }

    if (!statistic.getSoundgraphResults().isEmpty()) {
      messageText
          .append("Soundgraph playlists (")
          .append(statistic.getSoundgraphResults().size())
          .append("):\n");
      statistic
          .getSoundgraphResults()
          .forEach(
              result ->
                  messageText
                      .append("- ")
                      .append(result.name())
                      .append(": ")
                      .append(result.amountOfTracks())
                      .append(" tracks\n"));
    }

    // Add cache statistics
    long finalCacheSize = statistic.getFinalCacheSize();
    long newTracksAdded = statistic.getNewTracksAdded();
    long cacheHits = statistic.getCacheHits();

    if (finalCacheSize > 0 || newTracksAdded > 0 || cacheHits > 0) {
      if (!shuffledPlaylists.isEmpty()
          || !statistic.getRefreshedPlaylists().isEmpty()
          || !statistic.getSoundgraphResults().isEmpty()) {
        messageText.append("\n");
      }

      messageText.append("Track cache: ").append(finalCacheSize).append(" total tracks");
      messageText.append(" (+").append(newTracksAdded).append(" new)");
      if (cacheHits > 0) {
        messageText.append(", ").append(cacheHits).append(" cache hits");
      }
      messageText.append("\n");
    }

    return messageText.toString();
  }

  private static String getDurationText(Statistic statistic) {
    if (statistic.getStartTime() != null) {
      LocalDateTime endTime = LocalDateTime.now();
      Duration duration = Duration.between(statistic.getStartTime(), endTime);
      return formatDuration(duration);
    }
    return "???";
  }

  private static String formatDuration(Duration duration) {
    long minutes = duration.toMinutes();
    long seconds = duration.minusMinutes(minutes).getSeconds();

    if (minutes > 0) {
      return String.format("%d min %d sec", minutes, seconds);
    } else {
      return String.format("%d sec", seconds);
    }
  }

  static String createFailedMessageText(Throwable throwable) {
    StringBuilder messageText = new StringBuilder();

    messageText.append(
        String.format(
            "Run failed with %s: %s",
            throwable.getClass().getSimpleName(), throwable.getMessage()));

    StackTraceElement[] stackTrace = throwable.getStackTrace();
    int traceLimit = Math.min(2, stackTrace.length);
    for (int i = 0; i < traceLimit; i++) {
      messageText.append("\n  at ").append(stackTrace[i]);
    }

    if (stackTrace.length > traceLimit) {
      messageText.append("\n  ... ").append(stackTrace.length - traceLimit).append(" more");
    }

    Throwable cause = throwable.getCause();
    while (cause != null) {
      messageText
          .append("\nCaused by: ")
          .append(cause.getClass().getSimpleName())
          .append(": ")
          .append(cause.getMessage());

      stackTrace = cause.getStackTrace();
      traceLimit = Math.min(2, stackTrace.length);
      for (int i = 0; i < traceLimit; i++) {
        messageText.append("\n  at ").append(stackTrace[i]);
      }

      if (stackTrace.length > traceLimit) {
        messageText.append("\n  ... ").append(stackTrace.length - traceLimit).append(" more");
      }

      cause = cause.getCause();
    }

    return messageText.toString();
  }
}
