package com.github.juliusd.radiohitsplaylist.config;

public record NotifierConfiguration(
  boolean notifyOnSuccess,
  boolean notifyOnFailure,
  String gotifyUrl,
  String gotifyApiToken
  ) {
}
