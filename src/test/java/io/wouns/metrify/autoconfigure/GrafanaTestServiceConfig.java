package io.wouns.metrify.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class GrafanaTestServiceConfig {

  @Bean
  GrafanaTestService grafanaTestService() {
    return new GrafanaTestService();
  }
}
