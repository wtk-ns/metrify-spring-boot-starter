package io.wouns.metrify.validation;

import io.wouns.metrify.annotation.MetricGauge;

class OddTagsService {

  @MetricGauge(value = "odd.gauge", tags = {"env", "test", "orphan"})
  public double oddTagsMethod() {
    return 1.0;
  }
}
