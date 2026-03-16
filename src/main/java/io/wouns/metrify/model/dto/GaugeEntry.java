package io.wouns.metrify.model.dto;

import java.util.concurrent.atomic.AtomicReference;

public record GaugeEntry(AtomicReference<CachedValue> valueRef, long ttlMillis) {}
