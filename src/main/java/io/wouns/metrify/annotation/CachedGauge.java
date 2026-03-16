package io.wouns.metrify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CachedGauge {

  String value();

  String description() default "";

  String[] tags() default {};

  long timeout() default 30;

  TimeUnit timeoutUnit() default TimeUnit.SECONDS;
}
