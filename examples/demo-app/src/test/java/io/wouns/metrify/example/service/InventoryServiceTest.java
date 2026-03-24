package io.wouns.metrify.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = InventoryServiceTestConfig.class,
    properties = "metrify.prefix=demo")
class InventoryServiceTest {

  @Autowired
  private MeterRegistry registry;

  @Autowired
  private InventoryService inventoryService;

  @Test
  void getTotalStockRegistersCachedGauge() {
    int stock = inventoryService.getTotalStock();

    Gauge gauge = registry.find("demo.inventory.total.items").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isGreaterThan(0);
    assertThat(stock).isGreaterThan(0);
  }

  @Test
  void cachedGaugeAppliesDescription() {
    inventoryService.getTotalStock();

    Gauge gauge = registry.find("demo.inventory.total.items").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.getId().getDescription())
        .isEqualTo("Total items across all products");
  }

  @Test
  void getStockLevelsRegistersGaugeWithMapSize() {
    inventoryService.getStockLevels();

    Gauge gauge = registry.find("demo.inventory.products").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(5.0);
  }

  @Test
  void checkAvailabilityAsyncIncrementsCounter()
      throws ExecutionException, InterruptedException {
    CompletableFuture<Boolean> result =
        inventoryService.checkAvailability("laptop");
    Boolean available = result.get();

    assertThat(available).isTrue();

    Counter counter = registry.find("demo.inventory.check")
        .tag("product", "laptop")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void checkAvailabilityAppliesProductTag()
      throws ExecutionException, InterruptedException {
    inventoryService.checkAvailability("phone").get();
    inventoryService.checkAvailability("tablet").get();

    Counter phoneCounter = registry.find("demo.inventory.check")
        .tag("product", "phone")
        .counter();
    assertThat(phoneCounter).isNotNull();

    Counter tabletCounter = registry.find("demo.inventory.check")
        .tag("product", "tablet")
        .counter();
    assertThat(tabletCounter).isNotNull();
  }

  @Test
  void checkAvailabilityReturnsFalseForUnknownProduct()
      throws ExecutionException, InterruptedException {
    CompletableFuture<Boolean> result =
        inventoryService.checkAvailability("nonexistent");
    Boolean available = result.get();

    assertThat(available).isFalse();

    Counter counter = registry.find("demo.inventory.check")
        .tag("product", "nonexistent")
        .tag("result", "success")
        .counter();
    assertThat(counter).isNotNull();
  }
}
