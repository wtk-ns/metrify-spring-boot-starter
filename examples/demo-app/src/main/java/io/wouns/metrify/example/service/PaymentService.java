package io.wouns.metrify.example.service;

import io.wouns.metrify.annotation.MetricCounter;
import io.wouns.metrify.annotation.MetricSummary;
import io.wouns.metrify.annotation.MetricTag;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

  @MetricCounter(
      value = "payment.processed",
      description = "Total payments processed")
  public BigDecimal charge(
      BigDecimal amount,
      @MetricTag(key = "currency") String currency) {
    simulateLatency(20, 100);

    if (ThreadLocalRandom.current().nextDouble() < 0.05) {
      throw new RuntimeException("Payment gateway timeout");
    }

    return amount;
  }

  @MetricSummary(
      value = "payment.amount",
      description = "Distribution of payment amounts",
      unit = "USD",
      publishPercentiles = {0.5, 0.95, 0.99})
  public double getLastPaymentAmount() {
    return ThreadLocalRandom.current().nextDouble(10.0, 500.0);
  }

  @MetricCounter(
      value = "payment.refund",
      description = "Payment refunds",
      recordFailuresOnly = true)
  public BigDecimal refund(BigDecimal amount) {
    simulateLatency(30, 80);

    if (ThreadLocalRandom.current().nextDouble() < 0.1) {
      throw new RuntimeException("Refund service unavailable");
    }

    return amount.multiply(BigDecimal.valueOf(0.95))
        .setScale(2, RoundingMode.HALF_UP);
  }

  private void simulateLatency(int minMs, int maxMs) {
    try {
      Thread.sleep(minMs + (long) (Math.random() * (maxMs - minMs)));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
