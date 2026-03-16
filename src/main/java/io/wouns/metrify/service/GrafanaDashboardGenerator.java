package io.wouns.metrify.service;

import java.util.Map;

public interface GrafanaDashboardGenerator {

  Map<String, Object> generate();
}
