package io.wouns.metrify.service.impl;

import io.wouns.metrify.model.dto.MetricInfo;
import io.wouns.metrify.service.GrafanaDashboardGenerator;
import io.wouns.metrify.service.MetricScanner;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultGrafanaDashboardGenerator
    implements GrafanaDashboardGenerator {

  private final MetricScanner metricScanner;

  public DefaultGrafanaDashboardGenerator(MetricScanner metricScanner) {
    this.metricScanner = metricScanner;
  }

  @Override
  public Map<String, Object> generate() {
    List<MetricInfo> metrics = metricScanner.scan();
    List<Map<String, Object>> panels = new ArrayList<>();
    int panelId = 1;
    int gridY = 0;

    for (MetricInfo metric : metrics) {
      panels.add(buildPanel(metric, panelId++, gridY));
      gridY += 8;
    }

    Map<String, Object> dashboard = new LinkedHashMap<>();
    dashboard.put("title", "Metrify Dashboard");
    dashboard.put("uid", "metrify-auto-generated");
    dashboard.put("schemaVersion", 39);
    dashboard.put("version", 1);
    dashboard.put("refresh", "10s");

    Map<String, Object> time = new LinkedHashMap<>();
    time.put("from", "now-1h");
    time.put("to", "now");
    dashboard.put("time", time);

    dashboard.put("panels", panels);
    return dashboard;
  }

  private Map<String, Object> buildPanel(
      MetricInfo metric, int panelId, int gridY) {
    return switch (metric.type()) {
      case TIMER -> buildTimerPanel(metric, panelId, gridY);
      case COUNTER -> buildCounterPanel(metric, panelId, gridY);
      case GAUGE -> buildGaugePanel(metric, panelId, gridY);
      case SUMMARY -> buildSummaryPanel(metric, panelId, gridY);
    };
  }

  private Map<String, Object> buildTimerPanel(
      MetricInfo metric, int panelId, int gridY) {
    String name = metric.name();
    List<Map<String, Object>> targets = List.of(
        promTarget(
            "histogram_quantile(0.50, rate("
                + name + "_seconds_bucket[$__rate_interval]))",
            "p50"),
        promTarget(
            "histogram_quantile(0.95, rate("
                + name + "_seconds_bucket[$__rate_interval]))",
            "p95"),
        promTarget(
            "histogram_quantile(0.99, rate("
                + name + "_seconds_bucket[$__rate_interval]))",
            "p99"));
    return panelTemplate(
        metric, panelId, gridY, "timeseries", targets, "s");
  }

  private Map<String, Object> buildCounterPanel(
      MetricInfo metric, int panelId, int gridY) {
    List<Map<String, Object>> targets = List.of(
        promTarget(
            "rate(" + metric.name() + "_total[$__rate_interval])",
            "rate"));
    return panelTemplate(
        metric, panelId, gridY, "timeseries", targets, "ops");
  }

  private Map<String, Object> buildGaugePanel(
      MetricInfo metric, int panelId, int gridY) {
    List<Map<String, Object>> targets = List.of(
        promTarget(metric.name(), "current"));
    return panelTemplate(
        metric, panelId, gridY, "gauge", targets, "");
  }

  private Map<String, Object> buildSummaryPanel(
      MetricInfo metric, int panelId, int gridY) {
    List<Map<String, Object>> targets = List.of(
        promTarget(metric.name() + "_sum", "sum"),
        promTarget(metric.name() + "_count", "count"));
    return panelTemplate(
        metric, panelId, gridY, "timeseries", targets, "");
  }

  private Map<String, Object> panelTemplate(
      MetricInfo metric,
      int panelId,
      int gridY,
      String panelType,
      List<Map<String, Object>> targets,
      String unit) {
    Map<String, Object> panel = new LinkedHashMap<>();
    panel.put("id", panelId);
    panel.put("type", panelType);

    String title = metric.name()
        + " (" + metric.type().name().toLowerCase() + ")";
    panel.put("title", title);

    if (!metric.description().isEmpty()) {
      panel.put("description", metric.description());
    }

    Map<String, Object> gridPos = new LinkedHashMap<>();
    gridPos.put("h", 8);
    gridPos.put("w", 12);
    gridPos.put("x", 0);
    gridPos.put("y", gridY);
    panel.put("gridPos", gridPos);

    Map<String, Object> datasource = new LinkedHashMap<>();
    datasource.put("type", "prometheus");
    datasource.put("uid", "${DS_PROMETHEUS}");
    panel.put("datasource", datasource);

    panel.put("targets", targets);

    if (!unit.isEmpty()) {
      Map<String, Object> fieldConfig = new LinkedHashMap<>();
      Map<String, Object> defaults = new LinkedHashMap<>();
      defaults.put("unit", unit);
      fieldConfig.put("defaults", defaults);
      panel.put("fieldConfig", fieldConfig);
    }

    return panel;
  }

  private Map<String, Object> promTarget(
      String expr, String legendFormat) {
    Map<String, Object> target = new LinkedHashMap<>();
    target.put("expr", expr);
    target.put("legendFormat", legendFormat);
    return target;
  }
}
