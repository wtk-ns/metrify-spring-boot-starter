package io.wouns.metrify.aspect;

import io.wouns.metrify.annotation.MetricGauge;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

class FieldGaugeBean {

  @MetricGauge(value = "connections.active", tags = {"pool", "primary"})
  final AtomicInteger activeConnections = new AtomicInteger(0);

  @MetricGauge("bytes.total")
  final AtomicLong totalBytes = new AtomicLong(0);

  @MetricGauge("cpu.usage")
  final DoubleAdder cpuUsage = new DoubleAdder();

  @MetricGauge("tasks.pending")
  final Collection<String> pendingTasks = new ArrayList<>();

  @MetricGauge("cache.size")
  final Map<String, String> cacheEntries = new HashMap<>();
}
