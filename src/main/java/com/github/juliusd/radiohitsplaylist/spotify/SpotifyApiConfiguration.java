package com.github.juliusd.radiohitsplaylist.spotify;

import com.github.juliusd.radiohitsplaylist.config.Configuration;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

import static com.github.juliusd.radiohitsplaylist.Logger.log;

public class SpotifyApiConfiguration {

  public SpotifyApi spotifyApi(Configuration configuration) {
    String spotifyRefreshToken = configuration.spotify().refreshToken();
    if (spotifyRefreshToken == null || spotifyRefreshToken.isBlank()) {
      throw new RuntimeException("spotifyRefreshToken is needed");
    }

    String clientSecret = configuration.spotify().clientSecret();
    if (clientSecret == null || clientSecret.isBlank()) {
      throw new RuntimeException("clientSecret is needed");
    }

    try {
      SpotifyApi spotifyApi = new SpotifyApi.Builder()
        .setRefreshToken(spotifyRefreshToken)
        .setClientId(configuration.spotify().clientId())
        .setClientSecret(clientSecret)
        .build();

      var authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
      var authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

      spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

      log("New AccessToken expires in: " + authorizationCodeCredentials.getExpiresIn() + " seconds");
      return spotifyApi;
    } catch (IOException | ParseException | SpotifyWebApiException e) {
      throw new SpotifyException("Could not create access token", e);
    }
  }
}
