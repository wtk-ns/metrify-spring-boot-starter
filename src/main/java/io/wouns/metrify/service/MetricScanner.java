package io.wouns.metrify.service;

import io.wouns.metrify.model.dto.MetricInfo;
import java.util.List;

public interface MetricScanner {

  List<MetricInfo> scan();
}
