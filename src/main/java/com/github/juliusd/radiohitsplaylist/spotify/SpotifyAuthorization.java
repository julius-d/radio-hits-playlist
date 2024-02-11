package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.config.ConfigLoader;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.io.IOException;
import java.net.URI;

public class SpotifyAuthorization {
  private static final URI redirectUri = SpotifyHttpManager.makeUri("https://github.com/julius-d/radio-hits-playlist/redirected");
  private final SpotifyApi spotifyApi;

  public SpotifyAuthorization(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }

  public void authorizationCodeUri() {
    AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
      .scope("playlist-modify-public,playlist-modify-private,playlist-read-private,playlist-modify-public")
      .build();
    var uri = authorizationCodeUriRequest.execute();

    System.out.println("URI: " + uri.toString());
  }

  public void createAccessToken(String code) throws IOException, ParseException, SpotifyWebApiException {
    var authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
    var authorizationCodeCredentials = authorizationCodeRequest.execute();
    System.out.println("AccessToken: " + authorizationCodeCredentials.getAccessToken());
    System.out.println("RefreshToken: " + authorizationCodeCredentials.getRefreshToken());
    System.out.println("Expires: " + authorizationCodeCredentials.getExpiresIn());
  }

  public static void main(String[] args) throws IOException, ParseException, SpotifyWebApiException {
    var configuration = new ConfigLoader().loadConfig(System.getProperty("configFilePath"));
    var spotifyApi = new SpotifyApi.Builder()
      .setClientId(configuration.spotify().clientId())
      .setClientSecret(configuration.spotify().clientSecret())
      .setRedirectUri(redirectUri)
      .build();

    var spotifyAuthorization = new SpotifyAuthorization(spotifyApi);
    spotifyAuthorization.authorizationCodeUri();
    spotifyAuthorization.createAccessToken("code from url");
  }

}
