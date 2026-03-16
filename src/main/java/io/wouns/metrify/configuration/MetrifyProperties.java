package io.wouns.metrify.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "metrify")
public class MetrifyProperties {

  private boolean enabled = true;

  private String prefix = "";

  private ValidationProperties validation = new ValidationProperties();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public ValidationProperties getValidation() {
    return validation;
  }

  public void setValidation(ValidationProperties validation) {
    this.validation = validation;
  }
}
