package io.wouns.metrify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Records the numeric return value in a Micrometer DistributionSummary.
 *
 * <p>Method must return a {@code Number}. Supports {@code CompletableFuture}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetricSummary {

  String value();

  String description() default "";

  String[] tags() default {};

  String unit() default "";

  double[] publishPercentiles() default {};

  boolean publishPercentileHistogram() default false;
}
