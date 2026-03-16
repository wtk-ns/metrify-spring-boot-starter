package io.wouns.metrify.aspect;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.wouns.metrify.annotation.MetricGauge;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class MetricGaugeBeanPostProcessor implements BeanPostProcessor {

  private final MeterRegistry registry;

  public MetricGaugeBeanPostProcessor(MeterRegistry registry) {
    this.registry = registry;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, @Nullable String beanName)
      throws BeansException {
    for (Field field : bean.getClass().getDeclaredFields()) {
      MetricGauge annotation = field.getAnnotation(MetricGauge.class);
      if (annotation != null) {
        registerFieldGauge(bean, field, annotation);
      }
    }
    return bean;
  }

  private void registerFieldGauge(Object bean, Field field, MetricGauge annotation) {
    field.setAccessible(true);
    List<Tag> tags = parseStaticTags(annotation.tags());

    Gauge.builder(annotation.value(), bean, b -> readFieldValue(b, field))
        .description(annotation.description())
        .baseUnit(annotation.unit().isEmpty() ? null : annotation.unit())
        .tags(tags)
        .register(registry);
  }

  private double readFieldValue(Object bean, Field field) {
    try {
      Object value = field.get(bean);
      if (value instanceof AtomicInteger atomicInt) {
        return atomicInt.get();
      }
      if (value instanceof AtomicLong atomicLong) {
        return atomicLong.get();
      }
      if (value instanceof Number number) {
        return number.doubleValue();
      }
      if (value instanceof Collection<?> collection) {
        return collection.size();
      }
      if (value instanceof Map<?, ?> map) {
        return map.size();
      }
      return Double.NaN;
    } catch (IllegalAccessException e) {
      return Double.NaN;
    }
  }

  private List<Tag> parseStaticTags(String[] staticTags) {
    if (staticTags.length % 2 != 0) {
      throw new IllegalArgumentException(
          "Static tags must be key-value pairs (even number of elements)");
    }

    List<Tag> tags = new ArrayList<>(staticTags.length / 2);
    for (int i = 0; i < staticTags.length; i += 2) {
      tags.add(Tag.of(staticTags[i], staticTags[i + 1]));
    }
    return tags;
  }
}
