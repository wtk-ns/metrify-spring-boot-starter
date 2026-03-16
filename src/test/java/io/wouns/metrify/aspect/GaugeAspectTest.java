package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = GaugeTestConfig.class)
class GaugeAspectTest {

  @Autowired
  private MeterRegistry registry;

  @Autowired
  private GaugeTestService gaugeTestService;

  @Test
  void registersGaugeOnMethodCall() {
    gaugeTestService.currentTemperature();

    Gauge gauge = registry.find("temperature.current").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(36.6);
  }

  @Test
  void updatesGaugeValueOnSubsequentCalls() {
    gaugeTestService.dynamicValue(10.0);
    Gauge gauge = registry.find("dynamic.gauge").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(10.0);

    gaugeTestService.dynamicValue(25.5);
    assertThat(gauge.value()).isEqualTo(25.5);
  }

  @Test
  void tracksCollectionSize() {
    gaugeTestService.activeUsers();

    Gauge gauge = registry.find("active.users").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(3.0);
  }

  @Test
  void tracksMapSize() {
    gaugeTestService.cacheEntries();

    Gauge gauge = registry.find("cache.entries").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(2.0);
  }

  @Test
  void appliesStaticTags() {
    gaugeTestService.taggedGauge();

    Gauge gauge = registry.find("tagged.gauge")
        .tag("env", "test")
        .gauge();
    assertThat(gauge).isNotNull();
  }

  @Test
  void appliesSpelTags() {
    gaugeTestService.spelTaggedGauge("us-east");

    Gauge gauge = registry.find("region.gauge")
        .tag("region", "us-east")
        .gauge();
    assertThat(gauge).isNotNull();
  }

  @Test
  void appliesDescription() {
    gaugeTestService.currentTemperature();

    Gauge gauge = registry.find("temperature.current").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.getId().getDescription()).isEqualTo("Current body temperature");
  }

  @Test
  void appliesUnit() {
    gaugeTestService.currentTemperature();

    Gauge gauge = registry.find("temperature.current").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.getId().getBaseUnit()).isEqualTo("celsius");
  }
}
