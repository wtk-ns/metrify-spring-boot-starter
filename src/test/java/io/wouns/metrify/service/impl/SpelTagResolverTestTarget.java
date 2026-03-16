package io.wouns.metrify.service.impl;

import io.wouns.metrify.annotation.MetricTag;

public class SpelTagResolverTestTarget {

  public void taggedByToString(@MetricTag(key = "param") String input) {}

  public void taggedByLiteral(@MetricTag(key = "region", value = "us-east-1") String input) {}

  public void taggedBySpel(
      @MetricTag(key = "upper", expression = "#input.toUpperCase()") String input) {}

  public void multipleParams(
      @MetricTag(key = "customerId") String customerId,
      @MetricTag(key = "region") String region) {}

  public void noTags(String input) {}
}
