package io.wouns.metrify.example.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Order(
    String id,
    String customerId,
    String product,
    BigDecimal amount,
    String currency,
    OrderStatus status,
    Instant createdAt
) {

  public static Order create(
      String customerId, String product, BigDecimal amount, String currency) {
    return new Order(
        UUID.randomUUID().toString(),
        customerId,
        product,
        amount,
        currency,
        OrderStatus.PENDING,
        Instant.now());
  }

  public Order withStatus(OrderStatus newStatus) {
    return new Order(id, customerId, product, amount, currency, newStatus, createdAt);
  }
}
