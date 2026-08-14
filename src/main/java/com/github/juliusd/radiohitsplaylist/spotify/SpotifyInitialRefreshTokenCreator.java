package com.github.juliusd.radiohitsplaylist.spotify;

import java.io.IOException;
import java.net.URI;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

public class SpotifyInitialRefreshTokenCreator {
  public static final URI REDIRECT_URI =
      SpotifyHttpManager.makeUri("https://github.com/julius-d/radio-hits-playlist/redirected");
  private final SpotifyApi spotifyApi;

  public SpotifyInitialRefreshTokenCreator(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }

  public String buildAuthorizationUrl() {
    AuthorizationCodeUriRequest authorizationCodeUriRequest =
        spotifyApi
            .authorizationCodeUri()
            .scope(
                "playlist-modify-public,playlist-modify-private,playlist-read-private,playlist-read-collaborative")
            .build();
    return authorizationCodeUriRequest.execute().toString();
  }

  public String exchangeCodeForRefreshToken(String code)
      throws IOException, ParseException, SpotifyWebApiException {
    var authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
    var authorizationCodeCredentials = authorizationCodeRequest.execute();
    return authorizationCodeCredentials.getRefreshToken();
  }
}
