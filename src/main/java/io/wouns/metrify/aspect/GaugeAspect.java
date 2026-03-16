package io.wouns.metrify.aspect;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.wouns.metrify.annotation.MetricGauge;
import io.wouns.metrify.service.MetricNameResolver;
import io.wouns.metrify.service.TagExtractor;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class GaugeAspect {

  private final MeterRegistry registry;
  private final MetricNameResolver nameResolver;
  private final TagExtractor tagExtractor;
  private final ConcurrentHashMap<String, AtomicReference<Double>> gaugeValues =
      new ConcurrentHashMap<>();

  public GaugeAspect(
      MeterRegistry registry,
      MetricNameResolver nameResolver,
      TagExtractor tagExtractor) {
    this.registry = registry;
    this.nameResolver = nameResolver;
    this.tagExtractor = tagExtractor;
  }

  @AfterReturning(
      pointcut = "@annotation(metricGauge)",
      returning = "result")
  public void afterGaugedMethod(JoinPoint joinPoint, MetricGauge metricGauge, Object result) {
    double value = extractDoubleValue(result);
    String name = nameResolver.resolve(metricGauge.value(), joinPoint);
    List<Tag> tags = tagExtractor.extract(metricGauge.tags(), joinPoint);
    String gaugeKey = buildGaugeKey(name, tags);

    gaugeValues.computeIfAbsent(gaugeKey, key -> {
      AtomicReference<Double> ref = new AtomicReference<>(value);
      Gauge.builder(name, ref, AtomicReference::get)
          .description(metricGauge.description())
          .baseUnit(metricGauge.unit().isEmpty() ? null : metricGauge.unit())
          .tags(tags)
          .register(registry);
      return ref;
    }).set(value);
  }

  private double extractDoubleValue(Object result) {
    if (result instanceof Number number) {
      return number.doubleValue();
    }
    if (result instanceof Collection<?> collection) {
      return collection.size();
    }
    if (result instanceof Map<?, ?> map) {
      return map.size();
    }
    throw new IllegalArgumentException(
        "@MetricGauge method must return Number, Collection, or Map, but returned: "
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
