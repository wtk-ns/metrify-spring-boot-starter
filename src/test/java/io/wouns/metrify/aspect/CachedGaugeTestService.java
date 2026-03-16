package io.wouns.metrify.aspect;

import io.wouns.metrify.annotation.CachedGauge;
import io.wouns.metrify.annotation.MetricTag;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class CachedGaugeTestService {

  private final AtomicInteger callCounter = new AtomicInteger(0);

  @CachedGauge(value = "cached.temperature", timeout = 60, timeoutUnit = TimeUnit.SECONDS)
  public double temperature() {
    return 36.6;
  }

  @CachedGauge(value = "incrementing.value", timeout = 60, timeoutUnit = TimeUnit.SECONDS)
  public int incrementingValue() {
    return callCounter.incrementAndGet();
  }

  public int getCallCount() {
    return callCounter.get();
  }

  @CachedGauge(value = "short.ttl", timeout = 100, timeoutUnit = TimeUnit.MILLISECONDS)
  public double shortTtlValue() {
    return callCounter.incrementAndGet();
  }

  @CachedGauge(value = "tagged.cached", tags = {"env", "test"})
  public double taggedCachedGauge() {
    return 1.0;
  }

  @CachedGauge("region.cached")
  public double spelTaggedCachedGauge(@MetricTag(key = "region") String region) {
    return 2.0;
  }

  @CachedGauge(value = "described.cached", description = "Cached temperature reading")
  public double describedCachedGauge() {
    return 3.0;
  }
}
