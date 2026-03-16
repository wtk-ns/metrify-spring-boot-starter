package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = GaugeBeanPostProcessorTestConfig.class)
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
}
