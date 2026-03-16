package io.wouns.metrify.validation;

import io.wouns.metrify.annotation.BusinessMetric;
import io.wouns.metrify.annotation.CachedGauge;
import io.wouns.metrify.annotation.MetricCounter;
import io.wouns.metrify.annotation.MetricGauge;
import io.wouns.metrify.annotation.MetricSummary;
import io.wouns.metrify.annotation.MetricTag;
import io.wouns.metrify.configuration.MetrifyProperties;
import io.wouns.metrify.model.enums.ValidationMode;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;

public class MetricAnnotationValidator implements SmartInitializingSingleton {

  private static final Log LOG =
      LogFactory.getLog(MetricAnnotationValidator.class);

  private static final Set<Class<? extends Annotation>> METRIC_ANNOTATIONS =
      Set.of(
          MetricGauge.class,
          MetricCounter.class,
          MetricSummary.class,
          BusinessMetric.class,
          CachedGauge.class);

  private final ConfigurableListableBeanFactory beanFactory;
  private final MetrifyProperties properties;
  private final SpelExpressionParser parser = new SpelExpressionParser();

  public MetricAnnotationValidator(
      ConfigurableListableBeanFactory beanFactory,
      MetrifyProperties properties) {
    this.beanFactory = beanFactory;
    this.properties = properties;
  }

  @Override
  public void afterSingletonsInstantiated() {
    List<String> violations = new ArrayList<>();
    Map<String, Set<String>> metricTagKeys = new HashMap<>();

    for (String beanName : beanFactory.getBeanDefinitionNames()) {
      Class<?> beanClass = beanFactory.getType(beanName);
      if (beanClass == null) {
        continue;
      }

      ReflectionUtils.doWithMethods(beanClass, method -> {
        for (Class<? extends Annotation> annotationType : METRIC_ANNOTATIONS) {
          Annotation annotation = method.getAnnotation(annotationType);
          if (annotation != null) {
            validateAnnotation(
                method, annotation, violations, metricTagKeys);
          }
        }
      });
    }

    reportViolations(violations);
  }

  private void validateAnnotation(
      Method method,
      Annotation annotation,
      List<String> violations,
      Map<String, Set<String>> metricTagKeys) {
    String metricName = extractMetricName(annotation);
    String[] staticTags = extractStaticTags(annotation);
    String location = method.getDeclaringClass().getSimpleName()
        + "." + method.getName();

    validateMetricName(metricName, location, violations);
    validateStaticTags(staticTags, location, violations);

    Set<String> tagKeys = collectTagKeys(staticTags, method);
    metricTagKeys.computeIfAbsent(metricName, k -> new HashSet<>());
    if (!tagKeys.isEmpty()) {
      metricTagKeys.get(metricName).addAll(tagKeys);
    }

    validateSpelExpressions(method, location, violations);
  }

  private void validateMetricName(
      String metricName, String location, List<String> violations) {
    if (metricName == null || metricName.isBlank()) {
      violations.add(location + ": metric name must not be empty");
    }
  }

  private void validateStaticTags(
      String[] staticTags, String location, List<String> violations) {
    if (staticTags.length % 2 != 0) {
      violations.add(location
          + ": static tags must be key-value pairs (even number of elements)");
    }

    Set<String> seenKeys = new HashSet<>();
    for (int i = 0; i < staticTags.length - 1; i += 2) {
      String key = staticTags[i];
      if (key.isBlank()) {
        violations.add(location + ": static tag key must not be blank");
      }
      if (!seenKeys.add(key)) {
        violations.add(location + ": duplicate static tag key '" + key + "'");
      }
    }
  }

  private Set<String> collectTagKeys(String[] staticTags, Method method) {
    Set<String> keys = new HashSet<>();
    for (int i = 0; i < staticTags.length - 1; i += 2) {
      keys.add(staticTags[i]);
    }
    for (Parameter parameter : method.getParameters()) {
      MetricTag[] metricTags =
          parameter.getAnnotationsByType(MetricTag.class);
      for (MetricTag metricTag : metricTags) {
        keys.add(metricTag.key());
      }
    }
    return keys;
  }

  private void validateSpelExpressions(
      Method method, String location, List<String> violations) {
    for (Parameter parameter : method.getParameters()) {
      MetricTag[] metricTags =
          parameter.getAnnotationsByType(MetricTag.class);
      for (MetricTag metricTag : metricTags) {
        if (metricTag.key().isBlank()) {
          violations.add(location + ": @MetricTag key must not be blank");
        }
        if (!metricTag.expression().isEmpty()) {
          try {
            parser.parseExpression(metricTag.expression());
          } catch (Exception e) {
            violations.add(location + ": invalid SpEL expression '"
                + metricTag.expression() + "': " + e.getMessage());
          }
        }
      }
    }
  }

  private void reportViolations(List<String> violations) {
    if (violations.isEmpty()) {
      return;
    }

    String message = "Metrify validation found " + violations.size()
        + " issue(s):\n  - " + String.join("\n  - ", violations);

    ValidationMode mode = properties.getValidation().getMode();
    if (mode == ValidationMode.FAIL) {
      throw new IllegalStateException(message);
    }
    LOG.warn(message);
  }

  private String extractMetricName(Annotation annotation) {
    if (annotation instanceof MetricGauge a) {
      return a.value();
    }
    if (annotation instanceof MetricCounter a) {
      return a.value();
    }
    if (annotation instanceof MetricSummary a) {
      return a.value();
    }
    if (annotation instanceof BusinessMetric a) {
      return a.value();
    }
    if (annotation instanceof CachedGauge a) {
      return a.value();
    }
    return "";
  }

  private String[] extractStaticTags(Annotation annotation) {
    if (annotation instanceof MetricGauge a) {
      return a.tags();
    }
    if (annotation instanceof MetricCounter a) {
      return a.tags();
    }
    if (annotation instanceof MetricSummary a) {
      return a.tags();
    }
    if (annotation instanceof BusinessMetric a) {
      return a.tags();
    }
    if (annotation instanceof CachedGauge a) {
      return a.tags();
    }
    return new String[0];
  }
}
