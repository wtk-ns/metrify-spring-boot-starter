package io.wouns.metrify.service.impl;

import io.wouns.metrify.annotation.BusinessMetric;
import io.wouns.metrify.annotation.CachedGauge;
import io.wouns.metrify.annotation.MetricCounter;
import io.wouns.metrify.annotation.MetricGauge;
import io.wouns.metrify.annotation.MetricSummary;
import io.wouns.metrify.model.dto.MetricInfo;
import io.wouns.metrify.model.enums.MetricType;
import io.wouns.metrify.service.MetricScanner;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;

public class DefaultMetricScanner implements MetricScanner {

  private final ConfigurableListableBeanFactory beanFactory;

  public DefaultMetricScanner(ConfigurableListableBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
  public List<MetricInfo> scan() {
    List<MetricInfo> metrics = new ArrayList<>();

    for (String beanName : beanFactory.getBeanDefinitionNames()) {
      Class<?> beanClass = beanFactory.getType(beanName);
      if (beanClass == null) {
        continue;
      }

      ReflectionUtils.doWithMethods(beanClass, method -> {
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        MetricCounter counter = method.getAnnotation(MetricCounter.class);
        if (counter != null) {
          metrics.add(new MetricInfo(
              counter.value(), MetricType.COUNTER,
              counter.description(), className, methodName));
        }

        MetricGauge gauge = method.getAnnotation(MetricGauge.class);
        if (gauge != null) {
          metrics.add(new MetricInfo(
              gauge.value(), MetricType.GAUGE,
              gauge.description(), className, methodName));
        }

        CachedGauge cachedGauge =
            method.getAnnotation(CachedGauge.class);
        if (cachedGauge != null) {
          metrics.add(new MetricInfo(
              cachedGauge.value(), MetricType.GAUGE,
              cachedGauge.description(), className, methodName));
        }

        MetricSummary summary =
            method.getAnnotation(MetricSummary.class);
        if (summary != null) {
          metrics.add(new MetricInfo(
              summary.value(), MetricType.SUMMARY,
              summary.description(), className, methodName));
        }

        BusinessMetric business =
            method.getAnnotation(BusinessMetric.class);
        if (business != null) {
          metrics.add(new MetricInfo(
              business.value() + ".timer", MetricType.TIMER,
              business.description(), className, methodName));
          metrics.add(new MetricInfo(
              business.value() + ".count", MetricType.COUNTER,
              business.description(), className, methodName));
        }
      });
    }

    return metrics;
  }
}
