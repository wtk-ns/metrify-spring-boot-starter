package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.wouns.metrify.annotation.MetricSummary;
import io.wouns.metrify.annotation.MetricTag;
import io.wouns.metrify.autoconfigure.MetrifyAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootTest(classes = SummaryAspectTest.TestConfig.class)
class SummaryAspectTest {

  @Autowired
  private MeterRegistry registry;

  @Autowired
  private SummaryTestService summaryTestService;

  @Test
  void recordsReturnValue() {
    summaryTestService.payloadSize();

    DistributionSummary summary = registry.find("payload.size").summary();
    assertThat(summary).isNotNull();
    assertThat(summary.count()).isEqualTo(1);
    assertThat(summary.totalAmount()).isEqualTo(256.0);
  }

  @Test
  void recordsMultipleValues() {
    summaryTestService.responseSize(100);
    summaryTestService.responseSize(200);
    summaryTestService.responseSize(300);

    DistributionSummary summary = registry.find("response.size").summary();
    assertThat(summary).isNotNull();
    assertThat(summary.count()).isEqualTo(3);
    assertThat(summary.totalAmount()).isEqualTo(600.0);
  }

  @Test
  void appliesStaticTags() {
    summaryTestService.taggedSummary();

    DistributionSummary summary = registry.find("tagged.summary")
        .tag("type", "request")
        .summary();
    assertThat(summary).isNotNull();
  }

  @Test
  void appliesSpelTags() {
    summaryTestService.spelTaggedSummary("upload");

    DistributionSummary summary = registry.find("operation.summary")
        .tag("operation", "upload")
        .summary();
    assertThat(summary).isNotNull();
  }

  @Test
  void appliesDescription() {
    summaryTestService.describedSummary();

    DistributionSummary summary = registry.find("described.summary").summary();
    assertThat(summary).isNotNull();
    assertThat(summary.getId().getDescription()).isEqualTo("Tracks payload sizes");
  }

  @Test
  void appliesUnit() {
    summaryTestService.unitSummary();

    DistributionSummary summary = registry.find("unit.summary").summary();
    assertThat(summary).isNotNull();
    assertThat(summary.getId().getBaseUnit()).isEqualTo("bytes");
  }

  @Test
  void supportsIntegerReturnType() {
    summaryTestService.integerSummary();

    DistributionSummary summary = registry.find("integer.summary").summary();
    assertThat(summary).isNotNull();
    assertThat(summary.totalAmount()).isEqualTo(42.0);
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
    SummaryTestService summaryTestService() {
      return new SummaryTestService();
    }
  }

  static class SummaryTestService {

    @MetricSummary("payload.size")
    public double payloadSize() {
      return 256.0;
    }

    @MetricSummary("response.size")
    public int responseSize(int size) {
      return size;
    }

    @MetricSummary(value = "tagged.summary", tags = {"type", "request"})
    public double taggedSummary() {
      return 10.0;
    }

    @MetricSummary("operation.summary")
    public double spelTaggedSummary(@MetricTag(key = "operation") String operation) {
      return 50.0;
    }

    @MetricSummary(value = "described.summary", description = "Tracks payload sizes")
    public double describedSummary() {
      return 100.0;
    }

    @MetricSummary(value = "unit.summary", unit = "bytes")
    public double unitSummary() {
      return 512.0;
    }

    @MetricSummary("integer.summary")
    public int integerSummary() {
      return 42;
    }
  }
}
