package io.wouns.metrify.aspect;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.wouns.metrify.annotation.MetricSummary;
import io.wouns.metrify.service.MetricNameResolver;
import io.wouns.metrify.service.TagExtractor;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class SummaryAspect {

  private final MeterRegistry registry;
  private final MetricNameResolver nameResolver;
  private final TagExtractor tagExtractor;

  public SummaryAspect(
      MeterRegistry registry,
      MetricNameResolver nameResolver,
      TagExtractor tagExtractor) {
    this.registry = registry;
    this.nameResolver = nameResolver;
    this.tagExtractor = tagExtractor;
  }

  @AfterReturning(
      pointcut = "@annotation(metricSummary)",
      returning = "result")
  public void afterSummaryMethod(JoinPoint joinPoint, MetricSummary metricSummary, Object result) {
    if (!(result instanceof Number number)) {
      throw new IllegalArgumentException(
          "@MetricSummary method must return a Number, but returned: "
              + (result != null ? result.getClass().getName() : "null"));
    }

    String name = nameResolver.resolve(metricSummary.value(), joinPoint);
    List<Tag> tags = tagExtractor.extract(metricSummary.tags(), joinPoint);

    DistributionSummary.Builder builder = DistributionSummary.builder(name)
        .description(metricSummary.description())
        .baseUnit(metricSummary.unit().isEmpty() ? null : metricSummary.unit())
        .tags(tags);

    if (metricSummary.publishPercentiles().length > 0) {
      builder.publishPercentiles(metricSummary.publishPercentiles());
    }

    if (metricSummary.publishPercentileHistogram()) {
      builder.publishPercentileHistogram();
    }

    builder.register(registry).record(number.doubleValue());
  }
}
