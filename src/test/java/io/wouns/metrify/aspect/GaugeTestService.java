package io.wouns.metrify.aspect;

import io.wouns.metrify.annotation.MetricGauge;
import io.wouns.metrify.annotation.MetricTag;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class GaugeTestService {

  @MetricGauge(
      value = "temperature.current",
      description = "Current body temperature",
      unit = "celsius")
  public double currentTemperature() {
    return 36.6;
  }

  @MetricGauge("dynamic.gauge")
  public double dynamicValue(double value) {
    return value;
  }

  @MetricGauge("active.users")
  public Collection<String> activeUsers() {
    return List.of("alice", "bob", "charlie");
  }

  @MetricGauge("cache.entries")
  public Map<String, Object> cacheEntries() {
    return Map.of("key1", "val1", "key2", "val2");
  }

  @MetricGauge(value = "tagged.gauge", tags = {"env", "test"})
  public double taggedGauge() {
    return 42.0;
  }

  @MetricGauge("region.gauge")
  public double spelTaggedGauge(@MetricTag(key = "region") String region) {
    return 99.0;
  }
}
