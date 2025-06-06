package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.config.ConfigLoader;
import java.io.IOException;
import java.net.URI;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

public class SpotifyInitialRefreshTokenCreator {
  private static final URI redirectUri =
      SpotifyHttpManager.makeUri("https://github.com/julius-d/radio-hits-playlist/redirected");
  private final SpotifyApi spotifyApi;

  public SpotifyInitialRefreshTokenCreator(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }

  public void authorizationCodeUri() {
    AuthorizationCodeUriRequest authorizationCodeUriRequest =
        spotifyApi
            .authorizationCodeUri()
            .scope(
                "playlist-modify-public,playlist-modify-private,playlist-read-private,playlist-modify-public,playlist-read-collaborative")
            .build();
    var uri = authorizationCodeUriRequest.execute();

    System.out.println("URI: " + uri.toString());
  }

  public void createAccessToken(String code)
      throws IOException, ParseException, SpotifyWebApiException {
    var authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
    var authorizationCodeCredentials = authorizationCodeRequest.execute();
    System.out.println("AccessToken: " + authorizationCodeCredentials.getAccessToken());
    System.out.println("RefreshToken: " + authorizationCodeCredentials.getRefreshToken());
    System.out.println("Expires: " + authorizationCodeCredentials.getExpiresIn());
  }

  public static void main(String[] args)
      throws IOException, ParseException, SpotifyWebApiException {
    var configuration = new ConfigLoader().loadConfig(System.getProperty("configFilePath"));
    var spotifyApi =
        new SpotifyApi.Builder()
            .setClientId(configuration.spotify().clientId())
            .setClientSecret(configuration.spotify().clientSecret())
            .setRedirectUri(redirectUri)
            .build();

    var spotifyAuthorization = new SpotifyInitialRefreshTokenCreator(spotifyApi);
    spotifyAuthorization.authorizationCodeUri();
    spotifyAuthorization.createAccessToken(
        "AQBKIno7PMV1_ECR94DNxXxkPWgBU-7inX4YShy331cPbOlap1xSCQCFOEC-dkPU5S24mFafq2-qD8R_FJ64DCmlCEPtcpwjwZHcO7aDHxKv2yjTF4VrVE0borHXlfRURfKf8TsNojxljnEUv8Xd2xNCLnjb0xH0fXYFRggYMAnJMm8QAHdTObv_d8NyOMzyGszfF7XEUCqAg6EYE1GAIKfC2fGohNVVbyXL0s9sWaY2HnAjcWPj3l9f61H9XxBPs3DXkus86BY0IxwcLwVLtQP5kRxOP61YJwFNcl8yyXVDYt1jDzrmIFrFqU14P-5AGmoF3zLJIriCQDM8sWEG9eUQ9w");
  }
}
