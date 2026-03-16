package io.wouns.metrify.aspect;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.wouns.metrify.annotation.CachedGauge;
import io.wouns.metrify.model.dto.CachedValue;
import io.wouns.metrify.model.dto.GaugeEntry;
import io.wouns.metrify.service.MetricNameResolver;
import io.wouns.metrify.service.TagExtractor;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class CachedGaugeAspect {

  private final MeterRegistry registry;
  private final MetricNameResolver nameResolver;
  private final TagExtractor tagExtractor;
  private final ConcurrentHashMap<String, GaugeEntry> gaugeEntries = new ConcurrentHashMap<>();

  public CachedGaugeAspect(
      MeterRegistry registry,
      MetricNameResolver nameResolver,
      TagExtractor tagExtractor) {
    this.registry = registry;
    this.nameResolver = nameResolver;
    this.tagExtractor = tagExtractor;
  }

  @Around("@annotation(cachedGauge)")
  public Object aroundCachedGauge(ProceedingJoinPoint joinPoint, CachedGauge cachedGauge)
      throws Throwable {
    String name = nameResolver.resolve(cachedGauge.value(), joinPoint);
    List<Tag> tags = tagExtractor.extract(cachedGauge.tags(), joinPoint);
    String gaugeKey = buildGaugeKey(name, tags);
    long ttlMillis = cachedGauge.timeoutUnit().toMillis(cachedGauge.timeout());

    GaugeEntry entry = gaugeEntries.computeIfAbsent(gaugeKey, key -> {
      AtomicReference<CachedValue> ref =
          new AtomicReference<>(new CachedValue(Double.NaN, 0));
      Gauge.builder(name, ref, r -> r.get().getValue())
          .description(cachedGauge.description())
          .tags(tags)
          .register(registry);
      return new GaugeEntry(ref, ttlMillis);
    });

    CachedValue current = entry.valueRef().get();
    long now = System.currentTimeMillis();

    if (now - current.getTimestamp() >= entry.ttlMillis()) {
      Object result = joinPoint.proceed();
      double value = extractDoubleValue(result);
      entry.valueRef().set(new CachedValue(value, now));
      return result;
    }

    return joinPoint.proceed();
  }

  private double extractDoubleValue(Object result) {
    if (result instanceof Number number) {
      return number.doubleValue();
    }
    throw new IllegalArgumentException(
        "@CachedGauge method must return a Number, but returned: "
            + (result != null ? result.getClass().getName() : "null"));
  }

  private String buildGaugeKey(String name, List<Tag> tags) {
    StringBuilder key = new StringBuilder(name);
    for (Tag tag : tags) {
      key.append('.').append(tag.getKey()).append('.').append(tag.getValue());
    }
    return key.toString();
  }

}
