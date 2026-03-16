package io.wouns.metrify.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.wouns.metrify.annotation.MetricCounter;
import io.wouns.metrify.service.MetricNameResolver;
import io.wouns.metrify.service.TagExtractor;
import io.wouns.metrify.utility.AsyncTypeDetector;
import io.wouns.metrify.utility.ReactiveMetricHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
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
    String description = metricCounter.description();
    boolean failuresOnly = metricCounter.recordFailuresOnly();

    try {
      Object result = joinPoint.proceed();

      if (result instanceof CompletionStage<?> completionStage) {
        return completionStage.whenComplete((value, throwable) -> {
          if (throwable != null) {
            recordCounter(name, description, baseTags,
                "failure", throwable.getClass().getSimpleName());
          } else if (!failuresOnly) {
            recordCounter(name, description, baseTags, "success", "none");
          }
        });
      }

      if (AsyncTypeDetector.isMono(result)) {
        return ReactiveMetricHelper.wrapCounterMono(result,
            () -> {
              if (!failuresOnly) {
                recordCounter(name, description, baseTags, "success", "none");
              }
            },
            ex -> recordCounter(
                name, description, baseTags,
                "failure", ex.getClass().getSimpleName()));
      }

      if (AsyncTypeDetector.isFlux(result)) {
        return ReactiveMetricHelper.wrapCounterFlux(result,
            () -> {
              if (!failuresOnly) {
                recordCounter(name, description, baseTags, "success", "none");
              }
            },
            ex -> recordCounter(
                name, description, baseTags,
                "failure", ex.getClass().getSimpleName()));
      }

      if (!failuresOnly) {
        recordCounter(name, description, baseTags, "success", "none");
      }
      return result;
    } catch (Throwable ex) {
      recordCounter(name, description, baseTags, "failure", ex.getClass().getSimpleName());
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
