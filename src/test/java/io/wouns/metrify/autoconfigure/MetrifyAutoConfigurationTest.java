package io.wouns.metrify.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.wouns.metrify.aspect.GaugeAspect;
import io.wouns.metrify.aspect.MetricGaugeBeanPostProcessor;
import io.wouns.metrify.configuration.MetrifyProperties;
import io.wouns.metrify.model.enums.ValidationMode;
import io.wouns.metrify.service.MetricNameResolver;
import io.wouns.metrify.service.TagExtractor;
import io.wouns.metrify.service.TagResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MetrifyAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(MetrifyAutoConfiguration.class))
          .withBean(MeterRegistry.class, SimpleMeterRegistry::new);

  @Test
  void autoConfigurationRegistersProperties() {
    contextRunner.run(
        context -> assertThat(context).hasSingleBean(MetrifyProperties.class));
  }

  @Test
  void backsOffWhenDisabled() {
    contextRunner
        .withPropertyValues("metrify.enabled=false")
        .run(
            context -> assertThat(context).doesNotHaveBean(MetrifyProperties.class));
  }

  @Test
  void backsOffWhenMeterRegistryClassMissing() {
    contextRunner
        .withClassLoader(new FilteredClassLoader(MeterRegistry.class))
        .run(
            context -> assertThat(context).doesNotHaveBean(MetrifyProperties.class));
  }

  @Test
  void backsOffWhenNoMeterRegistryBean() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MetrifyAutoConfiguration.class))
        .run(
            context -> assertThat(context).doesNotHaveBean(MetrifyProperties.class));
  }

  @Test
  void propertiesAreCustomizable() {
    contextRunner
        .withPropertyValues("metrify.prefix=app", "metrify.validation.mode=FAIL")
        .run(
            context -> {
              assertThat(context).hasSingleBean(MetrifyProperties.class);
              MetrifyProperties props = context.getBean(MetrifyProperties.class);
              assertThat(props.getPrefix()).isEqualTo("app");
              assertThat(props.getValidation().getMode())
                  .isEqualTo(ValidationMode.FAIL);
            });
  }

  @Test
  void registersBeans() {
    contextRunner.run(
        context -> {
          assertThat(context).hasSingleBean(TagResolver.class);
          assertThat(context).hasSingleBean(MetricNameResolver.class);
          assertThat(context).hasSingleBean(TagExtractor.class);
          assertThat(context).hasSingleBean(GaugeAspect.class);
          assertThat(context).hasSingleBean(MetricGaugeBeanPostProcessor.class);
        });
  }

  @Test
  void userCanOverrideGaugeAspect() {
    GaugeAspect customAspect = new GaugeAspect(
        new SimpleMeterRegistry(), null, null);
    contextRunner
        .withBean(GaugeAspect.class, () -> customAspect)
        .run(
            context -> {
              assertThat(context).hasSingleBean(GaugeAspect.class);
              assertThat(context.getBean(GaugeAspect.class)).isSameAs(customAspect);
            });
  }
}
