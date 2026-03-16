package io.wouns.metrify.validation;

import io.wouns.metrify.annotation.MetricCounter;
import io.wouns.metrify.annotation.MetricTag;

class InvalidSpelService {

  @MetricCounter("spel.counter")
  public void invalidSpelMethod(
      @MetricTag(key = "val", expression = "#{broken[") String value) {
  }
}
