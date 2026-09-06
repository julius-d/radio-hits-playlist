package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrackCache {
  private final String databasePath;

  public TrackCache(String databasePath) {
    this.databasePath = databasePath;
    initializeDatabase();
  }

  private void initializeDatabase() {
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {

      stmt.execute(
          """
          CREATE TABLE IF NOT EXISTS tracks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            artist TEXT NOT NULL,
            title TEXT NOT NULL,
            spotify_uri TEXT NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            lookup_key TEXT NOT NULL UNIQUE
          )
          """);

    } catch (SQLException e) {
      throw new SqliteException("Failed to initialize track cache database", e);
    }
  }

  public Optional<URI> findTrack(Track track) {
    String key = lookupKey(track.artist(), track.title());
    String sql = "SELECT spotify_uri FROM tracks WHERE lookup_key = ?";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, key);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(URI.create(rs.getString("spotify_uri")));
        }
      }
    } catch (SQLException e) {
      throw new SqliteException(
          "Failed to lookup track in cache for artist '"
              + track.artist()
              + "' and title '"
              + track.title()
              + "'",
          e);
    }
    return Optional.empty();
  }

  public void storeTrack(SpotifyTrack spotifyTrack) {
    String displayArtist = String.join(" & ", spotifyTrack.artists());
    String spotifyTitle = spotifyTrack.name();
    String key = lookupKeyFromList(spotifyTrack.artists(), spotifyTitle);
    String sql =
        "INSERT OR REPLACE INTO tracks (artist, title, spotify_uri, lookup_key) VALUES (?, ?, ?, ?)";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, displayArtist);
      pstmt.setString(2, spotifyTitle);
      pstmt.setString(3, spotifyTrack.uri().toString());
      pstmt.setString(4, key);

      pstmt.executeUpdate();

    } catch (SQLException e) {
      throw new SqliteException(
          "Failed to store track in cache for artist '"
              + displayArtist
              + "' and title '"
              + spotifyTitle
              + "'",
          e);
    }
  }

  public void clearCache() {
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.executeUpdate("DELETE FROM tracks");
    } catch (SQLException e) {
      throw new SqliteException("Failed to clear track cache", e);
    }
  }

  public long getCacheSize() {
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tracks")) {
      if (rs.next()) {
        return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new SqliteException("Failed to get cache size", e);
    }
    throw new SqliteException("Failed to get cache size");
  }

  // Used when looking up from a radio source artist string (separators unknown)
  static String lookupKey(String radioArtist, String title) {
    String artistKey =
        splitArtists(radioArtist).stream()
            .map(TrackCache::normalizeArtistName)
            .sorted()
            .collect(Collectors.joining(","));
    return artistKey + "|" + title.toLowerCase().trim();
  }

  // Used when storing from a Spotify artists list (already split by Spotify).
  // Each individual name is also passed through splitArtists so that band names
  // containing '&' (e.g. "Simon & Garfunkel") produce the same key as when a
  // radio source sends the full string "Simon & Garfunkel".
  static String lookupKeyFromList(List<String> artists, String title) {
    String artistKey =
        artists.stream()
            .flatMap(a -> splitArtists(a).stream())
            .map(TrackCache::normalizeArtistName)
            .sorted()
            .collect(Collectors.joining(","));
    return artistKey + "|" + title.toLowerCase().trim();
  }

  private static List<String> splitArtists(String artist) {
    return Arrays.stream(
            artist
                .replaceAll("(?i)\\s+featuring\\.?\\s*", ",") // before feat to avoid prefix match
                .replaceAll("(?i)\\s+feat\\.?\\s*", ",")
                .replaceAll("(?i)\\s+ft\\.?\\s*", ",")
                .replaceAll("(?i)\\s+x\\s+", ",")
                .replace("&", ",")
                .split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }

  private static String normalizeArtistName(String name) {
    return name.toLowerCase().trim();
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
  }
}
