package com.github.juliusd.radiohitsplaylist.spotify;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.TooManyRequestsException;

public class CacheMigrator {

  private static final int MAX_RETRIES = 3;
  private static final String SOURCE_PATH = "track_cache.db";
  private static final String DEST_PATH = "track_cache_v2.db";

  private final SpotifyApi spotifyApi;

  public CacheMigrator(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }

  public void migrate() {
    List<OldEntry> entries = readOldCache();
    int total = entries.size();
    System.out.println("Found " + total + " entries in " + SOURCE_PATH);

    TrackCache newCache = new TrackCache(DEST_PATH);
    Set<String> alreadyMigrated = readMigratedUris();
    int migrated = 0;
    int resumed = 0;
    int skipped = 0;

    for (int i = 0; i < total; i++) {
      OldEntry entry = entries.get(i);
      System.out.print("\rMigrating " + (i + 1) + "/" + total + " ...");

      if (alreadyMigrated.contains(entry.spotifyUri())) {
        resumed++;
        continue;
      }

      try {
        String trackId = extractTrackId(entry.spotifyUri());
        var spotifyLibTrack = fetchTrackWithRetry(trackId);
        if (spotifyLibTrack == null) {
          skipped++;
          continue;
        }
        newCache.storeTrack(SpotifyTrackMapper.toSpotifyTrack(spotifyLibTrack), entry.createdAt());
        migrated++;
      } catch (Exception e) {
        System.out.println(
            "\nSkipping \"" + entry.title() + "\" / \"" + entry.artist() + "\": " + e.getMessage());
        skipped++;
      }
    }

    System.out.println("\nDone. Migrated: " + migrated + ", Already done: " + resumed + ", Skipped: " + skipped);
  }

  private se.michaelthelin.spotify.model_objects.specification.Track fetchTrackWithRetry(
      String trackId) {
    for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      try {
        return spotifyApi.getTrack(trackId).build().execute();
      } catch (TooManyRequestsException e) {
        if (attempt == MAX_RETRIES) return null;
        int waitSeconds = e.getRetryAfter() + 1;
        System.out.println("\nRate limited — waiting " + waitSeconds + "s...");
        try {
          Thread.sleep(waitSeconds * 1000L);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          return null;
        }
      } catch (IOException | SpotifyWebApiException | ParseException e) {
        return null;
      }
    }
    return null;
  }

  private static String extractTrackId(String spotifyUri) {
    String[] parts = spotifyUri.split(":");
    return parts[parts.length - 1];
  }

  private Set<String> readMigratedUris() {
    Set<String> uris = new HashSet<>();
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DEST_PATH);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT spotify_uri FROM tracks")) {
      while (rs.next()) {
        uris.add(rs.getString("spotify_uri"));
      }
    } catch (SQLException e) {
      // dest DB doesn't exist yet — no entries migrated
    }
    return uris;
  }

  private List<OldEntry> readOldCache() {
    List<OldEntry> entries = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + SOURCE_PATH);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT artist, title, spotify_uri, created_at FROM tracks")) {
      while (rs.next()) {
        entries.add(
            new OldEntry(
                rs.getString("artist"),
                rs.getString("title"),
                rs.getString("spotify_uri"),
                rs.getString("created_at")));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to read old cache from " + SOURCE_PATH, e);
    }
    return entries;
  }

  private record OldEntry(String artist, String title, String spotifyUri, String createdAt) {}
}
