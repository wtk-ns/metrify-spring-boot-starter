package io.wouns.metrify.autoconfigure;

import io.wouns.metrify.annotation.BusinessMetric;
import io.wouns.metrify.annotation.MetricCounter;
import io.wouns.metrify.annotation.MetricGauge;

class GrafanaTestService {

  @MetricCounter("grafana.test.counter")
  public void counted() {
  }

  @MetricGauge("grafana.test.gauge")
  public double gauged() {
    return 42.0;
  }

  @BusinessMetric("grafana.test.business")
  public String business() {
    return "done";
  }
}
