package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AsyncTestConfig.class)
class AsyncAspectTest {

  @Autowired
  private MeterRegistry registry;

  @Autowired
  private AsyncTestService asyncTestService;

  @Test
  void counterWithCompletableFutureSuccess() throws Exception {
    asyncTestService.asyncCount().get();

    Counter counter = registry.find("async.count")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void counterWithCompletableFutureFailure() {
    try {
      asyncTestService.asyncCountFailing().get();
    } catch (Exception ignored) {}

    Counter counter = registry.find("async.count.fail")
        .tag("result", "failure")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void businessMetricWithCompletableFuture() throws Exception {
    asyncTestService.asyncBusinessMetric().get();

    Timer timer = registry.find("async.business.timer")
        .tag("result", "success")
        .timer();
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1);

    Counter counter = registry.find("async.business.count")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void gaugeWithCompletableFuture() throws Exception {
    asyncTestService.asyncGauge().get();

    Gauge gauge = registry.find("async.gauge").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(42.0);
  }

  @Test
  void summaryWithCompletableFuture() throws Exception {
    asyncTestService.asyncSummary().get();

    var summary = registry.find("async.summary").summary();
    assertThat(summary).isNotNull();
    assertThat(summary.totalAmount()).isEqualTo(100.0);
  }
}
