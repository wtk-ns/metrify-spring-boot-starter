package io.wouns.metrify.example;

import io.wouns.metrify.example.service.InventoryService;
import io.wouns.metrify.example.service.OrderService;
import io.wouns.metrify.example.service.PaymentService;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrafficGenerator {

  private static final Logger log =
      LoggerFactory.getLogger(TrafficGenerator.class);

  private static final List<String> PRODUCTS =
      List.of("laptop", "phone", "tablet", "headphones", "charger");
  private static final List<String> CURRENCIES =
      List.of("USD", "EUR", "GBP");

  private final OrderService orderService;
  private final PaymentService paymentService;
  private final InventoryService inventoryService;

  public TrafficGenerator(
      OrderService orderService,
      PaymentService paymentService,
      InventoryService inventoryService) {
    this.orderService = orderService;
    this.paymentService = paymentService;
    this.inventoryService = inventoryService;
  }

  @Scheduled(fixedRate = 2000)
  public void generateOrderTraffic() {
    try {
      String product = randomFrom(PRODUCTS);
      String currency = randomFrom(CURRENCIES);
      BigDecimal amount = randomAmount(10, 500);
      orderService.placeOrder(currency, product, amount);
    } catch (Exception e) {
      log.debug("Simulated order failure: {}", e.getMessage());
    }
  }

  @Scheduled(fixedRate = 5000)
  public void generateInventoryChecks() {
    String product = randomFrom(PRODUCTS);
    inventoryService.checkAvailability(product);
  }

  @Scheduled(fixedRate = 3000)
  public void generateGaugeReads() {
    orderService.getTotalOrderCount();
    orderService.getCompletedOrderCount();
    orderService.getActiveOrders();
    inventoryService.getTotalStock();
    inventoryService.getStockLevels();
  }

  @Scheduled(fixedRate = 4000)
  public void generatePaymentSummary() {
    paymentService.getLastPaymentAmount();
  }

  @Scheduled(fixedRate = 10000)
  public void generateRefunds() {
    try {
      paymentService.refund(randomAmount(5, 100));
    } catch (Exception e) {
      log.debug("Simulated refund failure: {}", e.getMessage());
    }
  }

  @Scheduled(fixedRate = 15000)
  public void restockInventory() {
    String product = randomFrom(PRODUCTS);
    inventoryService.restock(product, ThreadLocalRandom.current().nextInt(5, 20));
  }

  private static String randomFrom(List<String> items) {
    return items.get(ThreadLocalRandom.current().nextInt(items.size()));
  }

  private static BigDecimal randomAmount(int min, int max) {
    double value = min + ThreadLocalRandom.current().nextDouble() * (max - min);
    return BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP);
  }
}
