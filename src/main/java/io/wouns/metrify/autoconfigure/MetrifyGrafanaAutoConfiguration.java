package io.wouns.metrify.autoconfigure;

import io.wouns.metrify.endpoint.MetrifyEndpoint;
import io.wouns.metrify.service.GrafanaDashboardGenerator;
import io.wouns.metrify.service.MetricScanner;
import io.wouns.metrify.service.impl.DefaultGrafanaDashboardGenerator;
import io.wouns.metrify.service.impl.DefaultMetricScanner;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = MetrifyAutoConfiguration.class)
@ConditionalOnClass(Endpoint.class)
@ConditionalOnProperty(
    prefix = "metrify.grafana",
    name = "enabled",
    havingValue = "true")
public class MetrifyGrafanaAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public DefaultMetricScanner metrifyMetricScanner(
      ConfigurableListableBeanFactory beanFactory) {
    return new DefaultMetricScanner(beanFactory);
  }

  @Bean
  @ConditionalOnMissingBean
  public DefaultGrafanaDashboardGenerator metrifyGrafanaDashboardGenerator(
      MetricScanner metricScanner) {
    return new DefaultGrafanaDashboardGenerator(metricScanner);
  }

  @Bean
  @ConditionalOnMissingBean
  public MetrifyEndpoint metrifyEndpoint(
      GrafanaDashboardGenerator dashboardGenerator) {
    return new MetrifyEndpoint(dashboardGenerator);
  }
}
