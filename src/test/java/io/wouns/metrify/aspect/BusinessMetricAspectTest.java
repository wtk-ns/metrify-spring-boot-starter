package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.wouns.metrify.annotation.BusinessMetric;
import io.wouns.metrify.annotation.MetricTag;
import io.wouns.metrify.autoconfigure.MetrifyAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootTest(classes = BusinessMetricAspectTest.TestConfig.class)
class BusinessMetricAspectTest {

  @Autowired
  private MeterRegistry registry;

  @Autowired
  private BusinessTestService businessTestService;

  @Test
  void createsTimerAndCounterOnSuccess() {
    businessTestService.processOrder();

    Timer timer = registry.find("order.process.timer")
        .tag("result", "success")
        .tag("exception", "none")
        .timer();
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1);

    Counter counter = registry.find("order.process.count")
        .tag("result", "success")
        .tag("exception", "none")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void createsTimerAndCounterOnFailure() {
    assertThatThrownBy(() -> businessTestService.failingProcess())
        .isInstanceOf(RuntimeException.class);

    Timer timer = registry.find("failing.process.timer")
        .tag("result", "failure")
        .tag("exception", "RuntimeException")
        .timer();
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1);

    Counter counter = registry.find("failing.process.count")
        .tag("result", "failure")
        .tag("exception", "RuntimeException")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void addsClassAndMethodAutoTags() {
    businessTestService.processOrder();

    Timer timer = registry.find("order.process.timer")
        .tag("class", "BusinessTestService")
        .tag("method", "processOrder")
        .timer();
    assertThat(timer).isNotNull();
  }

  @Test
  void appliesStaticTags() {
    businessTestService.taggedProcess();

    Timer timer = registry.find("tagged.process.timer")
        .tag("env", "test")
        .tag("result", "success")
        .timer();
    assertThat(timer).isNotNull();
  }

  @Test
  void appliesSpelTags() {
    businessTestService.spelTaggedProcess("vip");

    Counter counter = registry.find("customer.process.count")
        .tag("tier", "vip")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
  }

  @Test
  void appliesDescription() {
    businessTestService.describedProcess();

    Timer timer = registry.find("described.process.timer")
        .tag("result", "success")
        .timer();
    assertThat(timer).isNotNull();
    assertThat(timer.getId().getDescription()).isEqualTo("Order processing metric");
  }

  @Test
  void incrementsOnMultipleCalls() {
    businessTestService.multiCallProcess();
    businessTestService.multiCallProcess();

    Timer timer = registry.find("multi.call.timer")
        .tag("result", "success")
        .timer();
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(2);

    Counter counter = registry.find("multi.call.count")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(2.0);
  }

  @Configuration(proxyBeanMethods = false)
  @EnableAspectJAutoProxy
  @ImportAutoConfiguration(MetrifyAutoConfiguration.class)
  static class TestConfig {

    @Bean
    MeterRegistry meterRegistry() {
      return new SimpleMeterRegistry();
    }

    @Bean
    BusinessTestService businessTestService() {
      return new BusinessTestService();
    }
  }

  static class BusinessTestService {

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
}
