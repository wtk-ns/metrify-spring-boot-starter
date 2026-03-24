package io.wouns.metrify.example.service;

import io.wouns.metrify.annotation.BusinessMetric;
import io.wouns.metrify.annotation.CachedGauge;
import io.wouns.metrify.annotation.MetricCounter;
import io.wouns.metrify.annotation.MetricGauge;
import io.wouns.metrify.annotation.MetricTag;
import io.wouns.metrify.example.model.Order;
import io.wouns.metrify.example.model.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  private final Map<String, Order> orders = new ConcurrentHashMap<>();
  private final PaymentService paymentService;

  public OrderService(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @BusinessMetric(
      value = "order.checkout",
      description = "Full order checkout flow including payment")
  public Order placeOrder(
      @MetricTag(key = "currency") String currency,
      @MetricTag(key = "product") String product,
      BigDecimal amount) {
    Order order = Order.create("customer-" + (orders.size() + 1), product, amount, currency);
    simulateProcessing(50, 150);

    BigDecimal charged = paymentService.charge(amount, currency);
    if (charged.compareTo(BigDecimal.ZERO) <= 0) {
      Order failed = order.withStatus(OrderStatus.FAILED);
      orders.put(failed.id(), failed);
      throw new IllegalStateException("Payment failed for order " + order.id());
    }

    Order completed = order.withStatus(OrderStatus.COMPLETED);
    orders.put(completed.id(), completed);
    return completed;
  }

  @MetricCounter(
      value = "order.cancelled",
      description = "Number of cancelled orders",
      tags = {"reason", "user_request"})
  public void cancelOrder(String orderId) {
    Order order = orders.get(orderId);
    if (order != null) {
      orders.put(orderId, order.withStatus(OrderStatus.FAILED));
    }
  }

  @MetricGauge(
      value = "orders.active",
      description = "Currently active orders in the system")
  public List<Order> getActiveOrders() {
    return orders.values().stream()
        .filter(o -> o.status() == OrderStatus.PENDING
            || o.status() == OrderStatus.PROCESSING)
        .toList();
  }

  @CachedGauge(
      value = "orders.total",
      description = "Total number of orders",
      timeout = 10,
      timeoutUnit = TimeUnit.SECONDS)
  public int getTotalOrderCount() {
    return orders.size();
  }

  @MetricGauge(
      value = "orders.completed",
      description = "Number of completed orders")
  public long getCompletedOrderCount() {
    return orders.values().stream()
        .filter(o -> o.status() == OrderStatus.COMPLETED)
        .count();
  }

  private void simulateProcessing(int minMs, int maxMs) {
    try {
      Thread.sleep(minMs + (long) (Math.random() * (maxMs - minMs)));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
