package io.wouns.metrify.endpoint;

import io.wouns.metrify.service.GrafanaDashboardGenerator;
import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id = "metrify")
public class MetrifyEndpoint {

  private final GrafanaDashboardGenerator dashboardGenerator;

  public MetrifyEndpoint(
      GrafanaDashboardGenerator dashboardGenerator) {
    this.dashboardGenerator = dashboardGenerator;
  }

  @ReadOperation
  public Map<String, Object> dashboard() {
    return dashboardGenerator.generate();
  }
}
