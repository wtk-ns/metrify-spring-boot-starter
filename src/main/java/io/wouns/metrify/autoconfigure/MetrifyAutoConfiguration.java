package io.wouns.metrify.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import io.wouns.metrify.aspect.GaugeAspect;
import io.wouns.metrify.aspect.MetricGaugeBeanPostProcessor;
import io.wouns.metrify.configuration.MetrifyProperties;
import io.wouns.metrify.service.MetricNameResolver;
import io.wouns.metrify.service.TagExtractor;
import io.wouns.metrify.service.TagResolver;
import io.wouns.metrify.service.impl.DefaultMetricNameResolver;
import io.wouns.metrify.service.impl.DefaultTagExtractor;
import io.wouns.metrify.service.impl.SpelTagResolver;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(
    afterName = "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration")
@ConditionalOnClass({MeterRegistry.class, Aspect.class})
@ConditionalOnBean(MeterRegistry.class)
@ConditionalOnProperty(
    prefix = "metrify",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(MetrifyProperties.class)
public class MetrifyAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public TagResolver metrifyTagResolver() {
    return new SpelTagResolver();
  }

  @Bean
  @ConditionalOnMissingBean
  public MetricNameResolver metrifyMetricNameResolver(MetrifyProperties properties) {
    return new DefaultMetricNameResolver(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public TagExtractor metrifyTagExtractor(TagResolver tagResolver) {
    return new DefaultTagExtractor(tagResolver);
  }

  @Bean
  @ConditionalOnMissingBean
  public GaugeAspect metrifyGaugeAspect(
      MeterRegistry registry,
      MetricNameResolver nameResolver,
      TagExtractor tagExtractor) {
    return new GaugeAspect(registry, nameResolver, tagExtractor);
  }

  @Bean
  @ConditionalOnMissingBean
  public static MetricGaugeBeanPostProcessor metrifyMetricGaugeBeanPostProcessor(
      MeterRegistry registry) {
    return new MetricGaugeBeanPostProcessor(registry);
  }
}
