package com.github.juliusd;

import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.io.IOException;
import java.net.URI;

public class SpotifyAuthorization {
  private static final String clientId = "***REMOVED***";
  private static final String clientSecret = System.getProperty("spotifyClientSecret");
  private static final URI redirectUri = SpotifyHttpManager.makeUri("https://github.com/julius-d/radio-hits-playlist/redirected");

  private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
    .setClientId(clientId)
    .setClientSecret(clientSecret)
    .setRedirectUri(redirectUri)
    .build();
  private static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
    .scope("playlist-modify-public,playlist-modify-private,playlist-read-private,playlist-modify-public")
    .build();

  public static void authorizationCodeUri() {
    var uri = authorizationCodeUriRequest.execute();

    System.out.println("URI: " + uri.toString());
  }

  public static void createAccessToken(String code) throws IOException, ParseException, SpotifyWebApiException {
    var authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
    var authorizationCodeCredentials = authorizationCodeRequest.execute();
    System.out.println("AccessToken: " + authorizationCodeCredentials.getAccessToken());
    System.out.println("RefreshToken: " + authorizationCodeCredentials.getRefreshToken());
    System.out.println("Expires: " + authorizationCodeCredentials.getExpiresIn());
  }

  public static void main(String[] args) throws IOException, ParseException, SpotifyWebApiException {
    authorizationCodeUri();
    createAccessToken("code from url");
  }

}
