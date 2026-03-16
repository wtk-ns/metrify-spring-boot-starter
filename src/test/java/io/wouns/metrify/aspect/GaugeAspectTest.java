package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.wouns.metrify.annotation.MetricGauge;
import io.wouns.metrify.annotation.MetricTag;
import io.wouns.metrify.autoconfigure.MetrifyAutoConfiguration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@SpringBootTest(classes = GaugeAspectTest.TestConfig.class)
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

  @Configuration(proxyBeanMethods = false)
  @EnableAspectJAutoProxy
  @ImportAutoConfiguration(MetrifyAutoConfiguration.class)
  static class TestConfig {

    @Bean
    MeterRegistry meterRegistry() {
      return new SimpleMeterRegistry();
    }

    @Bean
    GaugeTestService gaugeTestService() {
      return new GaugeTestService();
    }
  }

  @Component
  static class GaugeTestService {

    @MetricGauge(
        value = "temperature.current",
        description = "Current body temperature",
        unit = "celsius")
    public double currentTemperature() {
      return 36.6;
    }

    @MetricGauge("dynamic.gauge")
    public double dynamicValue(double value) {
      return value;
    }

    @MetricGauge("active.users")
    public Collection<String> activeUsers() {
      return List.of("alice", "bob", "charlie");
    }

    @MetricGauge("cache.entries")
    public Map<String, Object> cacheEntries() {
      return Map.of("key1", "val1", "key2", "val2");
    }

    @MetricGauge(value = "tagged.gauge", tags = {"env", "test"})
    public double taggedGauge() {
      return 42.0;
    }

    @MetricGauge("region.gauge")
    public double spelTaggedGauge(@MetricTag(key = "region") String region) {
      return 99.0;
    }
  }
}
