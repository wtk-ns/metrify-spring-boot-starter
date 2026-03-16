package io.wouns.metrify.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.wouns.metrify.annotation.BusinessMetric;
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
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class BusinessMetricAspect {

  private final MeterRegistry registry;
  private final MetricNameResolver nameResolver;
  private final TagExtractor tagExtractor;

  public BusinessMetricAspect(
      MeterRegistry registry,
      MetricNameResolver nameResolver,
      TagExtractor tagExtractor) {
    this.registry = registry;
    this.nameResolver = nameResolver;
    this.tagExtractor = tagExtractor;
  }

  @Around("@annotation(businessMetric)")
  public Object aroundBusinessMetric(ProceedingJoinPoint joinPoint, BusinessMetric businessMetric)
      throws Throwable {
    String baseName = nameResolver.resolve(businessMetric.value(), joinPoint);
    List<Tag> baseTags = tagExtractor.extract(businessMetric.tags(), joinPoint);
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String description = businessMetric.description();

    List<Tag> autoTags = new ArrayList<>(baseTags);
    autoTags.add(Tag.of("class", signature.getDeclaringType().getSimpleName()));
    autoTags.add(Tag.of("method", signature.getMethod().getName()));

    Timer.Sample sample = Timer.start(registry);

    try {
      Object result = joinPoint.proceed();

      if (result instanceof CompletionStage<?> completionStage) {
        return completionStage.whenComplete((value, throwable) -> {
          if (throwable != null) {
            record(baseName, description, autoTags, sample,
                "failure", throwable.getClass().getSimpleName());
          } else {
            record(baseName, description, autoTags, sample, "success", "none");
          }
        });
      }

      if (AsyncTypeDetector.isMono(result)) {
        return ReactiveMetricHelper.wrapCounterMono(result,
            () -> record(baseName, description, autoTags, sample, "success", "none"),
            ex -> record(baseName, description, autoTags, sample,
                "failure", ex.getClass().getSimpleName()));
      }

      if (AsyncTypeDetector.isFlux(result)) {
        return ReactiveMetricHelper.wrapCounterFlux(result,
            () -> record(baseName, description, autoTags, sample, "success", "none"),
            ex -> record(baseName, description, autoTags, sample,
                "failure", ex.getClass().getSimpleName()));
      }

      record(baseName, description, autoTags, sample, "success", "none");
      return result;
    } catch (Throwable ex) {
      record(baseName, description, autoTags, sample, "failure",
          ex.getClass().getSimpleName());
      throw ex;
    }
  }

  private void record(
      String baseName,
      String description,
      List<Tag> baseTags,
      Timer.Sample sample,
      String result,
      String exception) {
    List<Tag> tags = new ArrayList<>(baseTags);
    tags.add(Tag.of("result", result));
    tags.add(Tag.of("exception", exception));

    Timer timer = Timer.builder(baseName + ".timer")
        .description(description)
        .tags(tags)
        .register(registry);
    sample.stop(timer);

    Counter.builder(baseName + ".count")
        .description(description)
        .tags(tags)
        .register(registry)
        .increment();
  }
}
