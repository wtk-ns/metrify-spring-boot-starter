package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CachedGaugeTestConfig.class)
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
}
