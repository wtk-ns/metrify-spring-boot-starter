package io.wouns.metrify.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.wouns.metrify.example.model.Order;
import io.wouns.metrify.example.model.OrderStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = OrderServiceTestConfig.class,
    properties = "metrify.prefix=demo")
class OrderServiceTest {

  @Autowired
  private MeterRegistry registry;

  @Autowired
  private OrderService orderService;

  @Test
  void placeOrderCreatesTimerAndCounter() {
    Order order = placeOrderWithRetry("USD", "laptop", 99.99);

    assertThat(order).isNotNull();
    assertThat(order.status()).isEqualTo(OrderStatus.COMPLETED);

    Timer timer = registry.find("demo.order.checkout.timer")
        .tag("result", "success")
        .tag("exception", "none")
        .timer();
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isGreaterThanOrEqualTo(1);

    Counter counter = registry.find("demo.order.checkout.count")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isGreaterThanOrEqualTo(1.0);
  }

  @Test
  void placeOrderAppliesCurrencyTag() {
    placeOrderWithRetry("EUR", "phone", 50.00);

    Timer timer = registry.find("demo.order.checkout.timer")
        .tag("currency", "EUR")
        .tag("product", "phone")
        .timer();
    assertThat(timer).isNotNull();
  }

  @Test
  void placeOrderAppliesDescription() {
    placeOrderWithRetry("GBP", "tablet", 30.00);

    Timer timer = registry.find("demo.order.checkout.timer")
        .tag("currency", "GBP")
        .timer();
    assertThat(timer).isNotNull();
    assertThat(timer.getId().getDescription())
        .isEqualTo("Full order checkout flow including payment");
  }

  @Test
  void placeOrderIncrementsOnMultipleCalls() {
    placeOrderWithRetry("USD", "charger", 10.00);
    placeOrderWithRetry("USD", "charger", 20.00);

    Counter counter = registry.find("demo.order.checkout.count")
        .tag("currency", "USD")
        .tag("product", "charger")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isGreaterThanOrEqualTo(2.0);
  }

  @Test
  void cancelOrderIncrementsCounter() {
    Order order = placeOrderWithRetry("USD", "headphones", 15.00);
    orderService.cancelOrder(order.id());

    Counter counter = registry.find("demo.order.cancelled")
        .tag("reason", "user_request")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isGreaterThanOrEqualTo(1.0);
  }

  @Test
  void getActiveOrdersRegistersGauge() {
    orderService.getActiveOrders();

    Gauge gauge = registry.find("demo.orders.active").gauge();
    assertThat(gauge).isNotNull();
  }

  @Test
  void getTotalOrderCountRegistersCachedGauge() {
    int count = orderService.getTotalOrderCount();

    Gauge gauge = registry.find("demo.orders.total").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isGreaterThanOrEqualTo(0);
  }

  @Test
  void getCompletedOrderCountRegistersGauge() {
    placeOrderWithRetry("USD", "laptop", 99.99);
    orderService.getCompletedOrderCount();

    Gauge gauge = registry.find("demo.orders.completed").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isGreaterThanOrEqualTo(1.0);
  }

  private Order placeOrderWithRetry(
      String currency, String product, double amount) {
    for (int i = 0; i < 10; i++) {
      try {
        return orderService.placeOrder(
            currency, product, BigDecimal.valueOf(amount));
      } catch (RuntimeException ignored) {
      }
    }
    throw new AssertionError(
        "placeOrder failed after 10 retries due to simulated payment failures");
  }
}
