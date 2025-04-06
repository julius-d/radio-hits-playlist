package com.github.juliusd.radiohitsplaylist.monitoring;

import com.github.juliusd.radiohitsplaylist.Statistic;

public interface Notifier {
  void runFinishedSuccessfully(Statistic statistic);
  void runFailed(Throwable throwable);
}
