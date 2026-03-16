package io.wouns.metrify.service;

import org.aspectj.lang.JoinPoint;

public interface MetricNameResolver {

  String resolve(String annotationValue, JoinPoint joinPoint);
}
