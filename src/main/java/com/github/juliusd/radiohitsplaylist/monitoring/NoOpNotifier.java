package com.github.juliusd.radiohitsplaylist.monitoring;

public class NoOpNotifier implements Notifier {
  @Override
  public void runFinishedSuccessfully() {
  }

  @Override
  public void runFailed(Throwable throwable) {
  }
}
