# Metrify Spring Boot Starter

Annotation-based [Micrometer](https://micrometer.io/) metrics exporter for Spring Boot.

Fills the gap left by Micrometer's intentional refusal to provide `@Gauge` and other
annotation-driven metric types ([micrometer-metrics/micrometer#451](https://github.com/micrometer-metrics/micrometer/issues/451) — wontfix).

## Try It in 5 Minutes

See it in action with a complete demo app, Prometheus, and Grafana — all pre-configured:

```bash
cd examples/demo-app/docker
docker compose up --build
```

Open [http://localhost:3000](http://localhost:3000) (admin/admin) to see live metrics flowing into a pre-built dashboard.

See the full [demo app documentation](examples/demo-app/README.md) for details.

## Requirements

- Java 21+
- Spring Boot 4.0.x
- Micrometer (comes with Spring Boot Actuator)

## Quick Start

Add the dependency:

```xml
<dependency>
  <groupId>io.github.wtk-ns</groupId>
  <artifactId>metrify-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

Ensure your compiler is configured with `-parameters`:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <parameters>true</parameters>
  </configuration>
</plugin>
```

That's it. Metrify auto-configures when a `MeterRegistry` bean is present.

## Annotations

### `@MetricGauge`

Records the return value as a Micrometer Gauge. Supports `Number`, `Collection` (size), and `Map` (size).

```java
@MetricGauge(value = "cache.size", description = "Current cache size", unit = "entries")
public Map<String, Object> getCache() {
    return cache;
}
```

Can also be placed on fields (`AtomicInteger`, `AtomicLong`, `Number`, `Collection`, `Map`):

```java
@MetricGauge("active.connections")
private final AtomicInteger activeConnections = new AtomicInteger();
```

### `@MetricCounter`

Increments a counter on method completion. Auto-tags with `result` (success/failure) and `exception`.

```java
@MetricCounter(value = "orders.placed", tags = {"region", "us-east"})
public Order placeOrder(Order order) {
    return orderService.save(order);
}
```

Use `recordFailuresOnly = true` to only count errors:

```java
@MetricCounter(value = "auth.failures", recordFailuresOnly = true)
public User authenticate(String token) { return null; }
```

### `@MetricSummary`

Records the numeric return value in a `DistributionSummary`.

```java
@MetricSummary(
    value = "response.size",
    unit = "bytes",
    publishPercentiles = {0.5, 0.95, 0.99},
    publishPercentileHistogram = true)
public int getResponseSize() {
    return response.length();
}
```

### `@BusinessMetric`

Combines a `Timer` and `Counter` in a single annotation. Creates `<name>.timer` and `<name>.count`.
Auto-tags: `class`, `method`, `result`, `exception`.

```java
@BusinessMetric(value = "checkout", description = "Checkout flow")
public Receipt checkout(Cart cart) {
    return paymentService.process(cart);
}
```

### `@CachedGauge`

Like `@MetricGauge` but with TTL-based caching — the method is only invoked when the cache expires.

```java
@CachedGauge(value = "db.pool.active", timeout = 30, timeoutUnit = TimeUnit.SECONDS)
public int getActiveDbConnections() {
    return dataSource.getNumActive();
}
```

### `@MetricTag`

Dynamic tags via parameter annotations. Supports literal values, SpEL expressions, or `toString()` fallback.

```java
@MetricCounter("api.calls")
public Response handle(
    @MetricTag(key = "endpoint") String endpoint,
    @MetricTag(key = "version", value = "v2") String body,
    @MetricTag(key = "length", expression = "#body.length()") String body) {
    
}
```

## Async & Reactive Support

All annotations work with `CompletableFuture`, `Mono`, and `Flux`:

```java
@MetricCounter("async.process")
public CompletableFuture<Result> processAsync() { return null; }

@MetricCounter("reactive.stream")
public Mono<Result> processReactive() { return null; }

@BusinessMetric("reactive.business")
public Flux<Item> streamItems() { return null; }
```

- `@MetricCounter` / `@BusinessMetric`: metrics recorded on completion/error
- `@MetricGauge` / `@MetricSummary`: skipped for `Mono`/`Flux` (no synchronous value available)

## Startup Validation

Metrify validates all annotated methods at startup:

- Non-empty metric names
- Static tags must be key-value pairs (even count)
- No duplicate static tag keys
- Valid SpEL expressions in `@MetricTag`

Configure validation mode:

```yaml
metrify:
  validation:
    mode: WARN  # WARN (default) or FAIL
```

## Grafana Dashboard Generation

Auto-generate a Grafana dashboard JSON from your annotated methods:

```yaml
metrify:
  grafana:
    enabled: true
```

Access via actuator: `GET /actuator/metrify`

Returns a complete Grafana dashboard with PromQL queries:
- Timers → latency panels (p50/p95/p99)
- Counters → rate panels
- Gauges → current value panels
- Summaries → sum/count panels

## Configuration

| Property | Default | Description |
|---|---|---|
| `metrify.enabled` | `true` | Whether to enable metrify auto-configuration |
| `metrify.prefix` | `""` | Prefix prepended to all metric names |
| `metrify.validation.mode` | `WARN` | Startup validation mode: `WARN` or `FAIL` |
| `metrify.grafana.enabled` | `false` | Whether to enable the Grafana dashboard endpoint |

## Limitations

- `@MetricGauge` and `@MetricSummary` on `Mono`/`Flux` return types are silently skipped (no synchronous value)
- `@CachedGauge` does not support async/reactive return types
- SpEL tag expressions require the `-parameters` compiler flag
- No Kotlin coroutines support (planned for future release)
- Field-level `@MetricGauge` requires the field to be a `Number`, `AtomicInteger`, `AtomicLong`, `Collection`, or `Map`

## License

[Apache License 2.0](LICENSE)
