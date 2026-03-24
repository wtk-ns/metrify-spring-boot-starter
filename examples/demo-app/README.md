# Metrify Demo App

A complete example showcasing all Metrify annotations with live metrics in Prometheus and Grafana.

## What's Inside

This demo app uses all 7 Metrify annotations in realistic e-commerce scenarios:

| Annotation | Where | What it does |
|---|---|---|
| `@BusinessMetric` | `OrderService.placeOrder()` | Timer + counter for the full checkout flow |
| `@MetricCounter` | `OrderService.cancelOrder()` | Counts order cancellations with static tags |
| `@MetricCounter` | `PaymentService.charge()` | Counts payments with `@MetricTag` for currency |
| `@MetricCounter(recordFailuresOnly)` | `PaymentService.refund()` | Only counts failed refunds |
| `@MetricSummary` | `PaymentService.getLastPaymentAmount()` | Distribution of payment amounts with percentiles |
| `@MetricGauge` (method) | `OrderService.getActiveOrders()` | Tracks active order count (collection size) |
| `@MetricGauge` (method) | `OrderService.getCompletedOrderCount()` | Tracks completed orders |
| `@MetricGauge` (field) | `InventoryService.activeConnections` | `AtomicInteger` field gauge |
| `@MetricGauge` (method) | `InventoryService.getStockLevels()` | Tracks product count (map size) |
| `@CachedGauge` | `OrderService.getTotalOrderCount()` | Cached total orders (10s TTL) |
| `@CachedGauge` | `InventoryService.getTotalStock()` | Cached inventory total (15s TTL) |
| `@MetricCounter` + async | `InventoryService.checkAvailability()` | Async `CompletableFuture` counter |
| `@MetricTag` | Multiple methods | Dynamic tags via SpEL and parameter values |

A built-in traffic generator (`TrafficGenerator.java`) automatically creates realistic load so you see
live data in Grafana immediately.

## Try It in 5 Minutes

### Option 1: Docker Compose (recommended)

```bash
cd examples/demo-app/docker
docker compose up --build
```

Then open:
- **Grafana**: [http://localhost:3000](http://localhost:3000) (admin/admin) — dashboard is pre-provisioned
- **Prometheus**: [http://localhost:9090](http://localhost:9090) — query metrics directly
- **App**: [http://localhost:8080](http://localhost:8080) — REST API

### Option 2: Run locally + Docker for infra

Start Prometheus and Grafana:

```bash
cd examples/demo-app/docker
docker compose up prometheus grafana
```

Run the app locally (requires Java 21+):

```bash
cd examples/demo-app
mvn spring-boot:run
```

## REST API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/orders?currency=USD&product=laptop&amount=99.99` | Place an order |
| `GET` | `/api/orders` | List active orders |
| `GET` | `/api/orders/count` | Get order counts |
| `POST` | `/api/orders/{id}/cancel` | Cancel an order |
| `GET` | `/api/inventory/{product}` | Check availability (async) |
| `GET` | `/api/inventory` | Get all stock levels |
| `GET` | `/api/payments/summary` | Get payment amount sample |
| `POST` | `/api/payments/refund?amount=50.00` | Process a refund |

## Metrics Endpoints

| Path | Description |
|---|---|
| `/actuator/prometheus` | Prometheus scrape endpoint |
| `/actuator/metrify` | Auto-generated Grafana dashboard JSON |
| `/actuator/health` | Health check |

## Configuration

All Metrify settings in `application.yml`:

```yaml
metrify:
  enabled: true         # Master switch
  prefix: demo          # All metrics get "demo." prefix
  validation:
    mode: WARN           # WARN or FAIL on invalid annotations
  grafana:
    enabled: true        # Enable /actuator/metrify endpoint
```

## Project Structure

```
demo-app/
├── src/main/java/.../example/
│   ├── DemoApplication.java          # @SpringBootApplication + @EnableScheduling
│   ├── TrafficGenerator.java         # Auto-generates realistic load
│   ├── controller/
│   │   └── OrderController.java      # REST endpoints
│   ├── model/
│   │   ├── Order.java                # Order record
│   │   └── OrderStatus.java          # Order status enum
│   └── service/
│       ├── OrderService.java         # @BusinessMetric, @MetricGauge, @CachedGauge, @MetricCounter
│       ├── PaymentService.java       # @MetricCounter, @MetricSummary, @MetricTag
│       └── InventoryService.java     # @MetricGauge (field), @CachedGauge, @MetricCounter (async)
├── src/main/resources/
│   └── application.yml
├── docker/
│   ├── docker-compose.yml            # App + Prometheus + Grafana
│   ├── prometheus/prometheus.yml     # Scrape config
│   └── grafana/provisioning/         # Pre-provisioned datasource + dashboard
├── Dockerfile
└── pom.xml
```
