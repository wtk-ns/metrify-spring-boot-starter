package io.wouns.metrify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MetricTags.class)
public @interface MetricTag {

  String key();

  String value() default "";

  String expression() default "";
}
