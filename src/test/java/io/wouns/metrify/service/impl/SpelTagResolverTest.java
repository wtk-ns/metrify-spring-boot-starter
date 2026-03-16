package io.wouns.metrify.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Tag;
import java.lang.reflect.Method;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

class SpelTagResolverTest {

  private final SpelTagResolver resolver = new SpelTagResolver();

  @Test
  void resolvesToStringWhenNoValueOrExpression() throws Exception {
    JoinPoint joinPoint = mockJoinPoint("taggedByToString", new Object[]{"hello"});

    List<Tag> tags = resolver.resolve(joinPoint);

    assertThat(tags).containsExactly(Tag.of("param", "hello"));
  }

  @Test
  void resolvesLiteralValue() throws Exception {
    JoinPoint joinPoint = mockJoinPoint("taggedByLiteral", new Object[]{"ignored"});

    List<Tag> tags = resolver.resolve(joinPoint);

    assertThat(tags).containsExactly(Tag.of("region", "us-east-1"));
  }

  @Test
  void resolvesSpelExpression() throws Exception {
    JoinPoint joinPoint = mockJoinPoint("taggedBySpel", new Object[]{"hello world"});

    List<Tag> tags = resolver.resolve(joinPoint);

    assertThat(tags).containsExactly(Tag.of("upper", "HELLO WORLD"));
  }

  @Test
  void resolvesNullParameterAsNullString() throws Exception {
    JoinPoint joinPoint = mockJoinPoint("taggedByToString", new Object[]{null});

    List<Tag> tags = resolver.resolve(joinPoint);

    assertThat(tags).containsExactly(Tag.of("param", "null"));
  }

  @Test
  void resolvesMultipleTagsFromDifferentParameters() throws Exception {
    JoinPoint joinPoint = mockJoinPoint("multipleParams",
        new Object[]{"customer-1", "us-west-2"});

    List<Tag> tags = resolver.resolve(joinPoint);

    assertThat(tags).containsExactly(
        Tag.of("customerId", "customer-1"),
        Tag.of("region", "us-west-2"));
  }

  @Test
  void returnsEmptyListWhenNoAnnotatedParameters() throws Exception {
    JoinPoint joinPoint = mockJoinPoint("noTags", new Object[]{"value"});

    List<Tag> tags = resolver.resolve(joinPoint);

    assertThat(tags).isEmpty();
  }

  private JoinPoint mockJoinPoint(String methodName, Object[] args) throws Exception {
    Method method =
        SpelTagResolverTestTarget.class.getMethod(methodName, getParameterTypes(methodName));
    MethodSignature signature = mock(MethodSignature.class);
    when(signature.getMethod()).thenReturn(method);
    when(signature.getParameterNames()).thenReturn(getParameterNames(method));

    JoinPoint joinPoint = mock(JoinPoint.class);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(joinPoint.getArgs()).thenReturn(args);
    return joinPoint;
  }

  private Class<?>[] getParameterTypes(String methodName) {
    return switch (methodName) {
      case "multipleParams" -> new Class[]{String.class, String.class};
      default -> new Class[]{String.class};
    };
  }

  private String[] getParameterNames(Method method) {
    java.lang.reflect.Parameter[] params = method.getParameters();
    String[] names = new String[params.length];
    for (int i = 0; i < params.length; i++) {
      names[i] = params[i].getName();
    }
    return names;
  }
}
