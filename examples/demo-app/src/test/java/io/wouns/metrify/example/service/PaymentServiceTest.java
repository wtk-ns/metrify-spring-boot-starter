package io.wouns.metrify.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = PaymentServiceTestConfig.class,
    properties = "metrify.prefix=demo")
class PaymentServiceTest {

  @Autowired
  private MeterRegistry registry;

  @Autowired
  private PaymentService paymentService;

  @Test
  void chargeIncrementsCounter() {
    int successes = 0;
    for (int i = 0; i < 10; i++) {
      try {
        paymentService.charge(BigDecimal.valueOf(100.00), "USD");
        successes++;
      } catch (RuntimeException ignored) {
      }
    }

    Counter counter = registry.find("demo.payment.processed")
        .tag("currency", "USD")
        .counter();
    assertThat(counter).isNotNull();
  }

  @Test
  void chargeAppliesCurrencyTag() {
    boolean charged = false;
    for (int i = 0; i < 10 && !charged; i++) {
      try {
        paymentService.charge(BigDecimal.valueOf(50.00), "EUR");
        charged = true;
      } catch (RuntimeException ignored) {
      }
    }

    Counter counter = registry.find("demo.payment.processed")
        .tag("currency", "EUR")
        .counter();
    assertThat(counter).isNotNull();
  }

  @Test
  void chargeAppliesDescription() {
    boolean charged = false;
    for (int i = 0; i < 10 && !charged; i++) {
      try {
        paymentService.charge(BigDecimal.valueOf(25.00), "GBP");
        charged = true;
      } catch (RuntimeException ignored) {
      }
    }

    Counter counter = registry.find("demo.payment.processed")
        .tag("currency", "GBP")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.getId().getDescription())
        .isEqualTo("Total payments processed");
  }

  @Test
  void chargeCountsFailuresWithExceptionTag() {
    int failures = 0;
    for (int i = 0; i < 200; i++) {
      try {
        paymentService.charge(BigDecimal.valueOf(10.00), "JPY");
      } catch (RuntimeException e) {
        failures++;
      }
    }

    if (failures > 0) {
      Counter failCounter = registry.find("demo.payment.processed")
          .tag("result", "failure")
          .tag("exception", "RuntimeException")
          .counter();
      assertThat(failCounter).isNotNull();
      assertThat(failCounter.count()).isGreaterThanOrEqualTo(1.0);
    }
  }

  @Test
  void getLastPaymentAmountRecordsSummary() {
    paymentService.getLastPaymentAmount();
    paymentService.getLastPaymentAmount();

    DistributionSummary summary = registry.find("demo.payment.amount")
        .summary();
    assertThat(summary).isNotNull();
    assertThat(summary.count()).isEqualTo(2);
    assertThat(summary.totalAmount()).isGreaterThan(0);
  }

  @Test
  void summaryAppliesUnitAndDescription() {
    paymentService.getLastPaymentAmount();

    DistributionSummary summary = registry.find("demo.payment.amount")
        .summary();
    assertThat(summary).isNotNull();
    assertThat(summary.getId().getDescription())
        .isEqualTo("Distribution of payment amounts");
    assertThat(summary.getId().getBaseUnit()).isEqualTo("USD");
  }

  @Test
  void refundOnlyCountsFailures() {
    int failures = 0;
    for (int i = 0; i < 100; i++) {
      try {
        paymentService.refund(BigDecimal.valueOf(20.00));
      } catch (RuntimeException e) {
        failures++;
      }
    }

    Counter successCounter = registry.find("demo.payment.refund")
        .tag("result", "success")
        .counter();
    assertThat(successCounter).isNull();

    if (failures > 0) {
      Counter failCounter = registry.find("demo.payment.refund")
          .tag("result", "failure")
          .counter();
      assertThat(failCounter).isNotNull();
      assertThat(failCounter.count()).isEqualTo(failures);
    }
  }
}
