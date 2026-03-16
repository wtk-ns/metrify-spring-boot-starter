package io.wouns.metrify.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.wouns.metrify.autoconfigure.MetrifyAutoConfiguration;
import io.wouns.metrify.autoconfigure.MetrifyValidationAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MetricAnnotationValidatorTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(
              MetrifyAutoConfiguration.class,
              MetrifyValidationAutoConfiguration.class))
          .withUserConfiguration(ValidatorMeterRegistryConfig.class);

  @Test
  void validConfigPasses() {
    contextRunner
        .withUserConfiguration(ValidServiceConfig.class)
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).hasSingleBean(
              MetricAnnotationValidator.class);
        });
  }

  @Test
  void emptyMetricNameFailsInFailMode() {
    contextRunner
        .withPropertyValues("metrify.validation.mode=FAIL")
        .withUserConfiguration(EmptyNameServiceConfig.class)
        .run(context ->
            assertThat(context).hasFailed());
  }

  @Test
  void emptyMetricNameWarnsInWarnMode() {
    contextRunner
        .withPropertyValues("metrify.validation.mode=WARN")
        .withUserConfiguration(EmptyNameServiceConfig.class)
        .run(context ->
            assertThat(context).hasNotFailed());
  }

  @Test
  void duplicateTagKeysFailsInFailMode() {
    contextRunner
        .withPropertyValues("metrify.validation.mode=FAIL")
        .withUserConfiguration(DuplicateTagServiceConfig.class)
        .run(context ->
            assertThat(context).hasFailed());
  }

  @Test
  void oddNumberStaticTagsFailsInFailMode() {
    contextRunner
        .withPropertyValues("metrify.validation.mode=FAIL")
        .withUserConfiguration(OddTagsServiceConfig.class)
        .run(context ->
            assertThat(context).hasFailed());
  }

  @Test
  void invalidSpelFailsInFailMode() {
    contextRunner
        .withPropertyValues("metrify.validation.mode=FAIL")
        .withUserConfiguration(InvalidSpelServiceConfig.class)
        .run(context ->
            assertThat(context).hasFailed());
  }

  @Test
  void validatorBeanIsRegistered() {
    contextRunner.run(context ->
        assertThat(context).hasSingleBean(
            MetricAnnotationValidator.class));
  }
}
