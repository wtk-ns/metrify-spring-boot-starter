package io.wouns.metrify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers a Micrometer Gauge from the method return value or field.
 *
 * <p>Supported return types: {@code Number}, {@code Collection} (size),
 * {@code Map} (size). Fields: {@code AtomicInteger}, {@code AtomicLong},
 * {@code Number}, {@code Collection}, {@code Map}.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetricGauge {

  String value();

  String description() default "";

  String[] tags() default {};

  String unit() default "";
}
