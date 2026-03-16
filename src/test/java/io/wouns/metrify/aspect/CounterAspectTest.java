package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CounterTestConfig.class)
class CounterAspectTest {

  @Autowired
  private MeterRegistry registry;

  @Autowired
  private CounterTestService counterTestService;

  @Test
  void incrementsCounterOnSuccess() {
    counterTestService.successfulOperation();

    Counter counter = registry.find("operation.count")
        .tag("result", "success")
        .tag("exception", "none")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void incrementsCounterOnFailure() {
    assertThatThrownBy(() -> counterTestService.failingOperation())
        .isInstanceOf(RuntimeException.class);

    Counter counter = registry.find("operation.failure")
        .tag("result", "failure")
        .tag("exception", "RuntimeException")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void recordsFailuresOnlyWhenConfigured() {
    counterTestService.failuresOnlySuccess();

    Counter successCounter = registry.find("failures.only")
        .tag("result", "success")
        .counter();
    assertThat(successCounter).isNull();

    assertThatThrownBy(() -> counterTestService.failuresOnlyFailure())
        .isInstanceOf(IllegalStateException.class);

    Counter failureCounter = registry.find("failures.only")
        .tag("result", "failure")
        .tag("exception", "IllegalStateException")
        .counter();
    assertThat(failureCounter).isNotNull();
    assertThat(failureCounter.count()).isEqualTo(1.0);
  }

  @Test
  void appliesStaticTags() {
    counterTestService.taggedCounter();

    Counter counter = registry.find("tagged.counter")
        .tag("env", "test")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
  }

  @Test
  void appliesSpelTags() {
    counterTestService.spelTaggedCounter("premium");

    Counter counter = registry.find("customer.counter")
        .tag("tier", "premium")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
  }

  @Test
  void incrementsMultipleTimes() {
    counterTestService.multiCallCounter();
    counterTestService.multiCallCounter();
    counterTestService.multiCallCounter();

    Counter counter = registry.find("multi.call")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(3.0);
  }

  @Test
  void appliesDescription() {
    counterTestService.describedCounter();

    Counter counter = registry.find("described.counter")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.getId().getDescription()).isEqualTo("Counts described operations");
  }
}
