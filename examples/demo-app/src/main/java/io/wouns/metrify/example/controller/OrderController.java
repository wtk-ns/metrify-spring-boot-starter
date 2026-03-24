package io.wouns.metrify.example.controller;

import io.wouns.metrify.example.model.Order;
import io.wouns.metrify.example.service.InventoryService;
import io.wouns.metrify.example.service.OrderService;
import io.wouns.metrify.example.service.PaymentService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {

  private final OrderService orderService;
  private final PaymentService paymentService;
  private final InventoryService inventoryService;

  public OrderController(
      OrderService orderService,
      PaymentService paymentService,
      InventoryService inventoryService) {
    this.orderService = orderService;
    this.paymentService = paymentService;
    this.inventoryService = inventoryService;
  }

  @PostMapping("/orders")
  public Order placeOrder(
      @RequestParam(defaultValue = "USD") String currency,
      @RequestParam(defaultValue = "laptop") String product,
      @RequestParam(defaultValue = "99.99") BigDecimal amount) {
    return orderService.placeOrder(currency, product, amount);
  }

  @GetMapping("/orders")
  public List<Order> getActiveOrders() {
    return orderService.getActiveOrders();
  }

  @GetMapping("/orders/count")
  public Map<String, Object> getOrderCounts() {
    return Map.of(
        "total", orderService.getTotalOrderCount(),
        "completed", orderService.getCompletedOrderCount());
  }

  @PostMapping("/orders/{orderId}/cancel")
  public void cancelOrder(@PathVariable String orderId) {
    orderService.cancelOrder(orderId);
  }

  @GetMapping("/inventory/{product}")
  public CompletableFuture<Boolean> checkInventory(
      @PathVariable String product) {
    return inventoryService.checkAvailability(product);
  }

  @GetMapping("/inventory")
  public Map<String, Integer> getInventory() {
    return inventoryService.getStockLevels();
  }

  @GetMapping("/payments/summary")
  public double getPaymentSummary() {
    return paymentService.getLastPaymentAmount();
  }

  @PostMapping("/payments/refund")
  public BigDecimal refund(
      @RequestParam(defaultValue = "50.00") BigDecimal amount) {
    return paymentService.refund(amount);
  }
}
