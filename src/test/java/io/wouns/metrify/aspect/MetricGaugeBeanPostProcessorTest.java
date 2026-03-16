package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.wouns.metrify.annotation.MetricGauge;
import io.wouns.metrify.autoconfigure.MetrifyAutoConfiguration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = MetricGaugeBeanPostProcessorTest.TestConfig.class)
class MetricGaugeBeanPostProcessorTest {

  @Autowired
  private MeterRegistry registry;

  @Autowired
  private FieldGaugeBean fieldGaugeBean;

  @Test
  void tracksAtomicIntegerField() {
    fieldGaugeBean.activeConnections.set(5);

    Gauge gauge = registry.find("connections.active").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(5.0);

    fieldGaugeBean.activeConnections.set(10);
    assertThat(gauge.value()).isEqualTo(10.0);
  }

  @Test
  void tracksAtomicLongField() {
    fieldGaugeBean.totalBytes.set(1024L);

    Gauge gauge = registry.find("bytes.total").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(1024.0);
  }

  @Test
  void tracksDoubleAdderField() {
    fieldGaugeBean.cpuUsage.add(0.75);

    Gauge gauge = registry.find("cpu.usage").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(0.75);
  }

  @Test
  void tracksCollectionSizeField() {
    fieldGaugeBean.pendingTasks.add("task1");
    fieldGaugeBean.pendingTasks.add("task2");

    Gauge gauge = registry.find("tasks.pending").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(2.0);

    fieldGaugeBean.pendingTasks.add("task3");
    assertThat(gauge.value()).isEqualTo(3.0);
  }

  @Test
  void tracksMapSizeField() {
    fieldGaugeBean.cacheEntries.put("a", "1");

    Gauge gauge = registry.find("cache.size").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(1.0);
  }

  @Test
  void appliesTagsToFieldGauge() {
    fieldGaugeBean.activeConnections.set(3);

    Gauge gauge = registry.find("connections.active")
        .tag("pool", "primary")
        .gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(3.0);
  }

  @Configuration(proxyBeanMethods = false)
  @ImportAutoConfiguration(MetrifyAutoConfiguration.class)
  static class TestConfig {

    @Bean
    MeterRegistry meterRegistry() {
      return new SimpleMeterRegistry();
    }

    @Bean
    FieldGaugeBean fieldGaugeBean() {
      return new FieldGaugeBean();
    }
  }

  static class FieldGaugeBean {

    @MetricGauge(value = "connections.active", tags = {"pool", "primary"})
    final AtomicInteger activeConnections = new AtomicInteger(0);

    @MetricGauge("bytes.total")
    final AtomicLong totalBytes = new AtomicLong(0);

    @MetricGauge("cpu.usage")
    final DoubleAdder cpuUsage = new DoubleAdder();

    @MetricGauge("tasks.pending")
    final Collection<String> pendingTasks = new ArrayList<>();

    @MetricGauge("cache.size")
    final Map<String, String> cacheEntries = new HashMap<>();
  }
}
