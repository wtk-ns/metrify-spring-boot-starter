package io.wouns.metrify.service;

import io.micrometer.core.instrument.Tag;
import java.util.List;
import org.aspectj.lang.JoinPoint;

public interface TagResolver {

  List<Tag> resolve(JoinPoint joinPoint);
}
