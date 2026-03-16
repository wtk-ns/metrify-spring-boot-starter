package io.wouns.metrify.service.impl;

import io.micrometer.core.instrument.Tag;
import io.wouns.metrify.annotation.MetricTag;
import io.wouns.metrify.service.TagResolver;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpelTagResolver implements TagResolver {

  private final ExpressionParser parser = new SpelExpressionParser();

  @Override
  public List<Tag> resolve(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Parameter[] parameters = signature.getMethod().getParameters();
    Object[] args = joinPoint.getArgs();
    List<Tag> tags = new ArrayList<>();

    for (int i = 0; i < parameters.length; i++) {
      MetricTag[] metricTags = parameters[i].getAnnotationsByType(MetricTag.class);
      for (MetricTag metricTag : metricTags) {
        String tagValue = resolveTagValue(metricTag, args, signature.getParameterNames(), i);
        tags.add(Tag.of(metricTag.key(), tagValue));
      }
    }

    return tags;
  }

  private String resolveTagValue(
      MetricTag metricTag, Object[] args, String[] parameterNames, int paramIndex) {
    if (!metricTag.expression().isEmpty()) {
      return evaluateExpression(metricTag.expression(), args, parameterNames);
    }

    if (!metricTag.value().isEmpty()) {
      return metricTag.value();
    }

    Object arg = args[paramIndex];
    return arg != null ? arg.toString() : "null";
  }

  private String evaluateExpression(String expression, Object[] args, String[] parameterNames) {
    StandardEvaluationContext context = new StandardEvaluationContext();
    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], args[i]);
    }
    Object result = parser.parseExpression(expression).getValue(context);
    return result != null ? result.toString() : "null";
  }
}
