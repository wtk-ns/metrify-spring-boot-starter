package io.wouns.metrify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Combined Timer and Counter for business-critical methods.
 *
 * <p>Creates {@code {name}.timer} and {@code {name}.count} metrics.
 * Auto-tags: {@code class}, {@code method}, {@code result}, {@code exception}.
 * Supports {@code CompletableFuture}, {@code Mono}, and {@code Flux}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessMetric {

  String value();

  String description() default "";

  String[] tags() default {};
}
