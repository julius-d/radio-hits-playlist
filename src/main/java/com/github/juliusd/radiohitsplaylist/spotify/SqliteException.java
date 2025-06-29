package com.github.juliusd.radiohitsplaylist.spotify;

/** Exception thrown when SQLite operations fail in the track cache. */
public class SqliteException extends RuntimeException {

  public SqliteException(String message) {
    super(message);
  }

  public SqliteException(String message, Throwable cause) {
    super(message, cause);
  }
}
