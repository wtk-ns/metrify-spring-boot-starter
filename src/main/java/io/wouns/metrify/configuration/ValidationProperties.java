package io.wouns.metrify.configuration;

import io.wouns.metrify.model.enums.ValidationMode;

public class ValidationProperties {

  private ValidationMode mode = ValidationMode.WARN;

  public ValidationMode getMode() {
    return mode;
  }

  public void setMode(ValidationMode mode) {
    this.mode = mode;
  }
}
