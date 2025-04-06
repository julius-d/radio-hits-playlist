package com.github.juliusd.radiohitsplaylist.monitoring;

import com.github.juliusd.radiohitsplaylist.Statistic;

public class NoOpNotifier implements Notifier {
  @Override
  public void runFinishedSuccessfully(Statistic statistic) {
  }

  @Override
  public void runFailed(Throwable throwable) {
  }
}
