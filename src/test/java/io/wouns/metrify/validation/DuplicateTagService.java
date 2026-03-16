package io.wouns.metrify.validation;

import io.wouns.metrify.annotation.MetricCounter;

class DuplicateTagService {

  @MetricCounter(value = "dup.counter", tags = {"env", "test", "env", "prod"})
  public void duplicateTagMethod() {
  }
}
