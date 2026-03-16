package io.wouns.metrify.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.wouns.metrify.configuration.MetrifyProperties;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

class DefaultMetricNameResolverTest {

  @Test
  void usesAnnotationValueWhenProvided() throws Exception {
    DefaultMetricNameResolver resolver = createResolver("");
    JoinPoint joinPoint = mockJoinPoint();

    String name = resolver.resolve("custom.metric.name", joinPoint);

    assertThat(name).isEqualTo("custom.metric.name");
  }

  @Test
  void generatesDefaultNameFromClassAndMethod() throws Exception {
    DefaultMetricNameResolver resolver = createResolver("");
    JoinPoint joinPoint = mockJoinPoint();

    String name = resolver.resolve("", joinPoint);

    assertThat(name).isEqualTo("MetricNameResolverTestService.processOrder");
  }

  @Test
  void appliesPrefixWhenConfigured() throws Exception {
    DefaultMetricNameResolver resolver = createResolver("app");
    JoinPoint joinPoint = mockJoinPoint();

    String name = resolver.resolve("orders.count", joinPoint);

    assertThat(name).isEqualTo("app.orders.count");
  }

  @Test
  void appliesPrefixToGeneratedName() throws Exception {
    DefaultMetricNameResolver resolver = createResolver("myapp");
    JoinPoint joinPoint = mockJoinPoint();

    String name = resolver.resolve("", joinPoint);

    assertThat(name).isEqualTo("myapp.MetricNameResolverTestService.processOrder");
  }

  @Test
  void noPrefixWhenEmpty() throws Exception {
    DefaultMetricNameResolver resolver = createResolver("");
    JoinPoint joinPoint = mockJoinPoint();

    String name = resolver.resolve("metric.name", joinPoint);

    assertThat(name).isEqualTo("metric.name");
  }

  private DefaultMetricNameResolver createResolver(String prefix) {
    MetrifyProperties properties = new MetrifyProperties();
    properties.setPrefix(prefix);
    return new DefaultMetricNameResolver(properties);
  }

  private JoinPoint mockJoinPoint() throws Exception {
    Method method = MetricNameResolverTestService.class.getMethod("processOrder");
    MethodSignature signature = mock(MethodSignature.class);
    when(signature.getMethod()).thenReturn(method);
    when(signature.getDeclaringType()).thenReturn(MetricNameResolverTestService.class);

    JoinPoint joinPoint = mock(JoinPoint.class);
    when(joinPoint.getSignature()).thenReturn(signature);
    return joinPoint;
  }
}
