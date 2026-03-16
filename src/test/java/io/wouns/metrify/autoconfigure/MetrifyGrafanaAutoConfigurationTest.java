package io.wouns.metrify.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.wouns.metrify.endpoint.MetrifyEndpoint;
import io.wouns.metrify.service.GrafanaDashboardGenerator;
import io.wouns.metrify.service.MetricScanner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MetrifyGrafanaAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(
              MetrifyAutoConfiguration.class,
              MetrifyGrafanaAutoConfiguration.class))
          .withUserConfiguration(GrafanaMeterRegistryConfig.class);

  @Test
  void beansRegisteredWhenEnabled() {
    contextRunner
        .withPropertyValues("metrify.grafana.enabled=true")
        .run(context -> {
          assertThat(context).hasSingleBean(MetricScanner.class);
          assertThat(context).hasSingleBean(
              GrafanaDashboardGenerator.class);
          assertThat(context).hasSingleBean(MetrifyEndpoint.class);
        });
  }

  @Test
  void beansAbsentWhenDisabled() {
    contextRunner.run(context -> {
      assertThat(context).doesNotHaveBean(MetricScanner.class);
      assertThat(context).doesNotHaveBean(
          GrafanaDashboardGenerator.class);
      assertThat(context).doesNotHaveBean(MetrifyEndpoint.class);
    });
  }

  @Test
  void endpointReturnsDashboardJson() {
    contextRunner
        .withPropertyValues("metrify.grafana.enabled=true")
        .withUserConfiguration(GrafanaTestServiceConfig.class)
        .run(context -> {
          MetrifyEndpoint endpoint =
              context.getBean(MetrifyEndpoint.class);
          var dashboard = endpoint.dashboard();
          assertThat(dashboard).containsKey("title");
          assertThat(dashboard).containsKey("panels");
          assertThat(dashboard.get("title"))
              .isEqualTo("Metrify Dashboard");
        });
  }

  @Test
  void dashboardContainsPanelsForAnnotatedMethods() {
    contextRunner
        .withPropertyValues("metrify.grafana.enabled=true")
        .withUserConfiguration(GrafanaTestServiceConfig.class)
        .run(context -> {
          MetrifyEndpoint endpoint =
              context.getBean(MetrifyEndpoint.class);
          var dashboard = endpoint.dashboard();
          var panels = (java.util.List<?>) dashboard.get("panels");
          assertThat(panels).isNotEmpty();
        });
  }
}
