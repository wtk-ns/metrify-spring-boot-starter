package io.wouns.metrify.aspect;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SummaryTestConfig.class)
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
}
