package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.wouns.metrify.annotation.CachedGauge;
import io.wouns.metrify.annotation.MetricTag;
import io.wouns.metrify.autoconfigure.MetrifyAutoConfiguration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootTest(classes = CachedGaugeAspectTest.TestConfig.class)
class CachedGaugeAspectTest {

  @Autowired
  private MeterRegistry registry;

  @Autowired
  private CachedGaugeTestService cachedGaugeTestService;

  @Test
  void registersGaugeOnFirstCall() {
    cachedGaugeTestService.temperature();

    Gauge gauge = registry.find("cached.temperature").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(36.6);
  }

  @Test
  void cachesValueWithinTtl() {
    cachedGaugeTestService.incrementingValue();
    int firstCallCount = cachedGaugeTestService.getCallCount();

    cachedGaugeTestService.incrementingValue();

    Gauge gauge = registry.find("incrementing.value").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(firstCallCount);
  }

  @Test
  void refreshesValueAfterTtlExpires() throws InterruptedException {
    cachedGaugeTestService.shortTtlValue();

    Gauge gauge = registry.find("short.ttl").gauge();
    assertThat(gauge).isNotNull();
    double firstValue = gauge.value();

    Thread.sleep(150);

    cachedGaugeTestService.shortTtlValue();
    assertThat(gauge.value()).isGreaterThan(firstValue);
  }

  @Test
  void appliesStaticTags() {
    cachedGaugeTestService.taggedCachedGauge();

    Gauge gauge = registry.find("tagged.cached")
        .tag("env", "test")
        .gauge();
    assertThat(gauge).isNotNull();
  }

  @Test
  void appliesSpelTags() {
    cachedGaugeTestService.spelTaggedCachedGauge("us-west");

    Gauge gauge = registry.find("region.cached")
        .tag("region", "us-west")
        .gauge();
    assertThat(gauge).isNotNull();
  }

  @Test
  void appliesDescription() {
    cachedGaugeTestService.describedCachedGauge();

    Gauge gauge = registry.find("described.cached").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.getId().getDescription()).isEqualTo("Cached temperature reading");
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
    CachedGaugeTestService cachedGaugeTestService() {
      return new CachedGaugeTestService();
    }
  }

  static class CachedGaugeTestService {

    private final AtomicInteger callCounter = new AtomicInteger(0);

    @CachedGauge(value = "cached.temperature", timeout = 60, timeoutUnit = TimeUnit.SECONDS)
    public double temperature() {
      return 36.6;
    }

    @CachedGauge(value = "incrementing.value", timeout = 60, timeoutUnit = TimeUnit.SECONDS)
    public int incrementingValue() {
      return callCounter.incrementAndGet();
    }

    public int getCallCount() {
      return callCounter.get();
    }

    @CachedGauge(value = "short.ttl", timeout = 100, timeoutUnit = TimeUnit.MILLISECONDS)
    public double shortTtlValue() {
      return callCounter.incrementAndGet();
    }

    @CachedGauge(value = "tagged.cached", tags = {"env", "test"})
    public double taggedCachedGauge() {
      return 1.0;
    }

    @CachedGauge("region.cached")
    public double spelTaggedCachedGauge(@MetricTag(key = "region") String region) {
      return 2.0;
    }

    @CachedGauge(value = "described.cached", description = "Cached temperature reading")
    public double describedCachedGauge() {
      return 3.0;
    }
  }
}
