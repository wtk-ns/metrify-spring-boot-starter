package io.wouns.metrify.service.impl;

import io.wouns.metrify.configuration.MetrifyProperties;
import io.wouns.metrify.service.MetricNameResolver;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

public class DefaultMetricNameResolver implements MetricNameResolver {

  private final MetrifyProperties properties;

  public DefaultMetricNameResolver(MetrifyProperties properties) {
    this.properties = properties;
  }

  @Override
  public String resolve(String annotationValue, JoinPoint joinPoint) {
    String name = annotationValue.isEmpty()
        ? generateDefaultName(joinPoint)
        : annotationValue;

    String prefix = properties.getPrefix();
    if (prefix != null && !prefix.isEmpty()) {
      return prefix + "." + name;
    }

    return name;
  }

  private String generateDefaultName(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String className = signature.getDeclaringType().getSimpleName();
    String methodName = signature.getMethod().getName();
    return className + "." + methodName;
  }
}
