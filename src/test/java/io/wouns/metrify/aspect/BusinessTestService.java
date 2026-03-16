package io.wouns.metrify.aspect;

import io.wouns.metrify.annotation.BusinessMetric;
import io.wouns.metrify.annotation.MetricTag;

class BusinessTestService {

  @BusinessMetric("order.process")
  public String processOrder() {
    return "done";
  }

  @BusinessMetric("failing.process")
  public void failingProcess() {
    throw new RuntimeException("fail");
  }

  @BusinessMetric(value = "tagged.process", tags = {"env", "test"})
  public void taggedProcess() {}

  @BusinessMetric("customer.process")
  public void spelTaggedProcess(@MetricTag(key = "tier") String tier) {}

  @BusinessMetric(value = "described.process", description = "Order processing metric")
  public void describedProcess() {}

  @BusinessMetric("multi.call")
  public void multiCallProcess() {}
}
