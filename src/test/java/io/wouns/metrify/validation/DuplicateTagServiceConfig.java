package io.wouns.metrify.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class DuplicateTagServiceConfig {

  @Bean
  DuplicateTagService duplicateTagService() {
    return new DuplicateTagService();
  }
}
