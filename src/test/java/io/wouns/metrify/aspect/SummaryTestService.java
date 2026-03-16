package io.wouns.metrify.aspect;

import io.wouns.metrify.annotation.MetricSummary;
import io.wouns.metrify.annotation.MetricTag;

class SummaryTestService {

  @MetricSummary("payload.size")
  public double payloadSize() {
    return 256.0;
  }

  @MetricSummary("response.size")
  public int responseSize(int size) {
    return size;
  }

  @MetricSummary(value = "tagged.summary", tags = {"type", "request"})
  public double taggedSummary() {
    return 10.0;
  }

  @MetricSummary("operation.summary")
  public double spelTaggedSummary(@MetricTag(key = "operation") String operation) {
    return 50.0;
  }

  @MetricSummary(value = "described.summary", description = "Tracks payload sizes")
  public double describedSummary() {
    return 100.0;
  }

  @MetricSummary(value = "unit.summary", unit = "bytes")
  public double unitSummary() {
    return 512.0;
  }

  @MetricSummary("integer.summary")
  public int integerSummary() {
    return 42;
  }
}
