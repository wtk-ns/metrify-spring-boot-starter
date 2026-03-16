package io.wouns.metrify.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.wouns.metrify.annotation.MetricCounter;
import io.wouns.metrify.service.MetricNameResolver;
import io.wouns.metrify.service.TagExtractor;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class CounterAspect {

  private final MeterRegistry registry;
  private final MetricNameResolver nameResolver;
  private final TagExtractor tagExtractor;

  public CounterAspect(
      MeterRegistry registry,
      MetricNameResolver nameResolver,
      TagExtractor tagExtractor) {
    this.registry = registry;
    this.nameResolver = nameResolver;
    this.tagExtractor = tagExtractor;
  }

  @Around("@annotation(metricCounter)")
  public Object aroundCountedMethod(ProceedingJoinPoint joinPoint, MetricCounter metricCounter)
      throws Throwable {
    String name = nameResolver.resolve(metricCounter.value(), joinPoint);
    List<Tag> baseTags = tagExtractor.extract(metricCounter.tags(), joinPoint);

    try {
      Object result = joinPoint.proceed();
      if (!metricCounter.recordFailuresOnly()) {
        recordCounter(name, metricCounter.description(), baseTags, "success", "none");
      }
      return result;
    } catch (Throwable ex) {
      recordCounter(name, metricCounter.description(), baseTags, "failure", ex.getClass().getSimpleName());
      throw ex;
    }
  }

  private void recordCounter(
      String name, String description, List<Tag> baseTags, String result, String exception) {
    List<Tag> tags = new ArrayList<>(baseTags);
    tags.add(Tag.of("result", result));
    tags.add(Tag.of("exception", exception));

    Counter.builder(name)
        .description(description)
        .tags(tags)
        .register(registry)
        .increment();
  }
}
