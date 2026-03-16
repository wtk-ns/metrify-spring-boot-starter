package io.wouns.metrify.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.wouns.metrify.autoconfigure.MetrifyAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(MetrifyAutoConfiguration.class)
class GaugeBeanPostProcessorTestConfig {

  @Bean
  MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry();
  }

  @Bean
  FieldGaugeBean fieldGaugeBean() {
    return new FieldGaugeBean();
  }
}
