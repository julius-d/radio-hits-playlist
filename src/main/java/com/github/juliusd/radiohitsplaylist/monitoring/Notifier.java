package com.github.juliusd.radiohitsplaylist.monitoring;

public interface Notifier {
  void runFinishedSuccessfully();
  void runFailed(Throwable throwable);
}
