package io.wouns.metrify.aspect;

import io.wouns.metrify.annotation.MetricCounter;
import io.wouns.metrify.annotation.MetricTag;

class CounterTestService {

  @MetricCounter("operation.count")
  public void successfulOperation() {}

  @MetricCounter("operation.failure")
  public void failingOperation() {
    throw new RuntimeException("fail");
  }

  @MetricCounter(value = "failures.only", recordFailuresOnly = true)
  public void failuresOnlySuccess() {}

  @MetricCounter(value = "failures.only", recordFailuresOnly = true)
  public void failuresOnlyFailure() {
    throw new IllegalStateException("fail");
  }

  @MetricCounter(value = "tagged.counter", tags = {"env", "test"})
  public void taggedCounter() {}

  @MetricCounter("customer.counter")
  public void spelTaggedCounter(@MetricTag(key = "tier") String tier) {}

  @MetricCounter("multi.call")
  public void multiCallCounter() {}

  @MetricCounter(value = "described.counter", description = "Counts described operations")
  public void describedCounter() {}
}
