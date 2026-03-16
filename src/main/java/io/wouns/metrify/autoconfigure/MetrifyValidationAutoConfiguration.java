package io.wouns.metrify.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import io.wouns.metrify.configuration.MetrifyProperties;
import io.wouns.metrify.validation.MetricAnnotationValidator;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = MetrifyAutoConfiguration.class)
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnBean(MeterRegistry.class)
@ConditionalOnProperty(
    prefix = "metrify",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(MetrifyProperties.class)
public class MetrifyValidationAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public MetricAnnotationValidator metrifyMetricAnnotationValidator(
      ConfigurableListableBeanFactory beanFactory,
      MetrifyProperties properties) {
    return new MetricAnnotationValidator(beanFactory, properties);
  }
}
