package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.Track;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * SQLite-based cache for storing and retrieving Spotify track URIs based on artist and title. This
 * cache provides exact match lookup to avoid redundant Spotify API calls.
 */
public class TrackCache {
  private final String databasePath;

  public TrackCache(String databasePath) {
    this.databasePath = databasePath;
    initializeDatabase();
  }

  /** Initializes the SQLite database and creates the tracks table if it doesn't exist. */
  private void initializeDatabase() {
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {

      // Create tracks table
      String createTableSql =
          """
          CREATE TABLE IF NOT EXISTS tracks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            artist TEXT NOT NULL,
            title TEXT NOT NULL,
            spotify_uri TEXT NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            UNIQUE(artist, title)
          )
          """;
      stmt.execute(createTableSql);

      // Create index for fast lookups
      String createIndexSql =
          """
          CREATE INDEX IF NOT EXISTS idx_artist_title ON tracks(artist, title)
          """;
      stmt.execute(createIndexSql);

    } catch (SQLException e) {
      throw new SqliteException("Failed to initialize track cache database", e);
    }
  }

  /**
   * Checks if a track with the exact artist and title exists in the cache.
   *
   * @param track The track to search for
   * @return Optional containing the Spotify URI if found, empty otherwise
   */
  public Optional<URI> findTrack(Track track) {
    String sql = "SELECT spotify_uri FROM tracks WHERE artist = ? AND title = ?";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, track.artist());
      pstmt.setString(2, track.title());

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String spotifyUri = rs.getString("spotify_uri");
          return Optional.of(URI.create(spotifyUri));
        } else {
          return Optional.empty();
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
  }

  /**
   * Stores a track mapping in the cache.
   *
   * @param track The original track
   * @param spotifyUri The Spotify URI found for this track
   */
  public void storeTrack(Track track, URI spotifyUri) {
    String sql = "INSERT OR REPLACE INTO tracks (artist, title, spotify_uri) VALUES (?, ?, ?)";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, track.artist());
      pstmt.setString(2, track.title());
      pstmt.setString(3, spotifyUri.toString());

      pstmt.executeUpdate();

    } catch (SQLException e) {
      throw new SqliteException(
          "Failed to store track in cache for artist '"
              + track.artist()
              + "' and title '"
              + track.title()
              + "'",
          e);
    }
  }

  /** Clears all entries from the cache. */
  public void clearCache() {
    String sql = "DELETE FROM tracks";

    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {

      stmt.executeUpdate(sql);

    } catch (SQLException e) {
      throw new SqliteException("Failed to clear track cache", e);
    }
  }

  /**
   * Gets the current size of the cache.
   *
   * @return Number of cached tracks
   */
  public long getCacheSize() {
    String sql = "SELECT COUNT(*) FROM tracks";

    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      if (rs.next()) {
        return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new SqliteException("Failed to get cache size", e);
    }

    throw new SqliteException("Failed to get cache size");
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
  }
}
