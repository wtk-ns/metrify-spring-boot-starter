package io.wouns.metrify.aspect;

import io.wouns.metrify.annotation.BusinessMetric;
import io.wouns.metrify.annotation.MetricCounter;
import io.wouns.metrify.annotation.MetricGauge;
import io.wouns.metrify.annotation.MetricSummary;
import java.util.concurrent.CompletableFuture;

class AsyncTestService {

  @MetricCounter("async.count")
  public CompletableFuture<String> asyncCount() {
    return CompletableFuture.completedFuture("done");
  }

  @MetricCounter("async.count.fail")
  public CompletableFuture<String> asyncCountFailing() {
    return CompletableFuture.failedFuture(new RuntimeException("fail"));
  }

  @BusinessMetric("async.business")
  public CompletableFuture<String> asyncBusinessMetric() {
    return CompletableFuture.completedFuture("done");
  }

  @MetricGauge("async.gauge")
  public CompletableFuture<Double> asyncGauge() {
    return CompletableFuture.completedFuture(42.0);
  }

  @MetricSummary("async.summary")
  public CompletableFuture<Double> asyncSummary() {
    return CompletableFuture.completedFuture(100.0);
  }
}
