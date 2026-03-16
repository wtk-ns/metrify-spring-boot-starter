package io.wouns.metrify.validation;

import io.wouns.metrify.annotation.MetricCounter;

class ValidMetricService {

  @MetricCounter(value = "valid.counter", tags = {"env", "test"})
  public void validMethod() {
  }
}
