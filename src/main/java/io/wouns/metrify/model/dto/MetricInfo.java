package io.wouns.metrify.model.dto;

import io.wouns.metrify.model.enums.MetricType;

public record MetricInfo(
    String name,
    MetricType type,
    String description,
    String className,
    String methodName
) {}
