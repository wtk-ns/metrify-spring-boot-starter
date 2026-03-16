package io.wouns.metrify.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import io.wouns.metrify.configuration.MetrifyProperties;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

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
}
