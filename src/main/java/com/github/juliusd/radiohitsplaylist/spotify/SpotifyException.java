package com.github.juliusd.radiohitsplaylist.spotify;

public class SpotifyException extends RuntimeException {
  public SpotifyException(Throwable cause) {
    super(cause);
  }

  public SpotifyException(String message, Throwable cause) {
    super(message, cause);
  }
}
