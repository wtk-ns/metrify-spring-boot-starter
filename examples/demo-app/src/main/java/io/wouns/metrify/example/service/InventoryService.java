package io.wouns.metrify.example.service;

import io.wouns.metrify.annotation.CachedGauge;
import io.wouns.metrify.annotation.MetricCounter;
import io.wouns.metrify.annotation.MetricGauge;
import io.wouns.metrify.annotation.MetricTag;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

  @MetricGauge("inventory.connections")
  private final AtomicInteger activeConnections = new AtomicInteger(0);

  private final Map<String, Integer> stock = new ConcurrentHashMap<>(Map.of(
      "laptop", 50,
      "phone", 120,
      "tablet", 30,
      "headphones", 200,
      "charger", 500
  ));

  @CachedGauge(
      value = "inventory.total.items",
      description = "Total items across all products",
      timeout = 15,
      timeoutUnit = TimeUnit.SECONDS)
  public int getTotalStock() {
    simulateSlowQuery();
    return stock.values().stream().mapToInt(Integer::intValue).sum();
  }

  @MetricGauge(
      value = "inventory.products",
      description = "Number of distinct products tracked")
  public Map<String, Integer> getStockLevels() {
    return stock;
  }

  @MetricCounter(
      value = "inventory.check",
      description = "Async inventory availability checks")
  public CompletableFuture<Boolean> checkAvailability(
      @MetricTag(key = "product") String product) {
    return CompletableFuture.supplyAsync(() -> {
      activeConnections.incrementAndGet();
      try {
        simulateSlowQuery();
        Integer quantity = stock.get(product);
        return quantity != null && quantity > 0;
      } finally {
        activeConnections.decrementAndGet();
      }
    });
  }

  public boolean reserve(String product, int quantity) {
    return stock.computeIfPresent(product, (key, current) -> {
      if (current >= quantity) {
        return current - quantity;
      }
      return current;
    }) != null;
  }

  public void restock(String product, int quantity) {
    stock.merge(product, quantity, Integer::sum);
  }

  private void simulateSlowQuery() {
    try {
      Thread.sleep(ThreadLocalRandom.current().nextLong(10, 50));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
