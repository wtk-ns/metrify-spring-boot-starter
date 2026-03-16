package io.wouns.metrify.model.dto;

public class CachedValue {

  private final double value;
  private final long timestamp;

  public CachedValue(double value, long timestamp) {
    this.value = value;
    this.timestamp = timestamp;
  }

  public double getValue() {
    return value;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
