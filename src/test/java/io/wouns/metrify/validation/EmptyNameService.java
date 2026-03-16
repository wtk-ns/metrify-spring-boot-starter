package io.wouns.metrify.validation;

import io.wouns.metrify.annotation.MetricCounter;

class EmptyNameService {

  @MetricCounter("")
  public void emptyNameMethod() {
  }
}
