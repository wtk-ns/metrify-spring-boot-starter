package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest(classes = ReactiveTestConfig.class)
class ReactiveAspectTest {

  @Autowired
  private MeterRegistry registry;

  @Autowired
  private ReactiveTestService reactiveTestService;

  @Test
  void counterWithMonoSuccess() {
    var result = reactiveTestService.monoCount();
    StepVerifier.create(result).expectNext("done").verifyComplete();

    Counter counter = registry.find("mono.count")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void counterWithMonoError() {
    var result = reactiveTestService.monoCountFailing();
    StepVerifier.create(result).verifyError(RuntimeException.class);

    Counter counter = registry.find("mono.count.fail")
        .tag("result", "failure")
        .tag("exception", "RuntimeException")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void counterWithFluxSuccess() {
    var result = reactiveTestService.fluxCount();
    StepVerifier.create(result).expectNext("a", "b", "c").verifyComplete();

    Counter counter = registry.find("flux.count")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void counterWithFluxError() {
    var result = reactiveTestService.fluxCountFailing();
    StepVerifier.create(result).verifyError(IllegalStateException.class);

    Counter counter = registry.find("flux.count.fail")
        .tag("result", "failure")
        .tag("exception", "IllegalStateException")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void businessMetricWithMono() {
    var result = reactiveTestService.monoBusinessMetric();
    StepVerifier.create(result).expectNext("done").verifyComplete();

    Timer timer = registry.find("mono.business.timer")
        .tag("result", "success")
        .timer();
    assertThat(timer).isNotNull();

    Counter counter = registry.find("mono.business.count")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
  }

  @Test
  void businessMetricWithFlux() {
    var result = reactiveTestService.fluxBusinessMetric();
    StepVerifier.create(result).expectNext(1, 2, 3).verifyComplete();

    Timer timer = registry.find("flux.business.timer")
        .tag("result", "success")
        .timer();
    assertThat(timer).isNotNull();
  }
}
