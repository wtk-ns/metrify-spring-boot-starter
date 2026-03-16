package io.wouns.metrify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Increments a Micrometer Counter on method completion.
 *
 * <p>Auto-tags: {@code result} (success/failure), {@code exception}
 * (class name or "none"). Supports {@code CompletableFuture},
 * {@code Mono}, and {@code Flux}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetricCounter {

  String value();

  String description() default "";

  String[] tags() default {};

  boolean recordFailuresOnly() default false;
}
