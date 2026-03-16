package io.wouns.metrify.service.impl;

import io.micrometer.core.instrument.Tag;
import io.wouns.metrify.service.TagExtractor;
import io.wouns.metrify.service.TagResolver;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.JoinPoint;

public class DefaultTagExtractor implements TagExtractor {

  private final TagResolver tagResolver;

  public DefaultTagExtractor(TagResolver tagResolver) {
    this.tagResolver = tagResolver;
  }

  @Override
  public List<Tag> extract(String[] staticTags, JoinPoint joinPoint) {
    List<Tag> tags = new ArrayList<>(parseStaticTags(staticTags));
    tags.addAll(tagResolver.resolve(joinPoint));
    return tags;
  }

  private List<Tag> parseStaticTags(String[] staticTags) {
    if (staticTags.length % 2 != 0) {
      throw new IllegalArgumentException(
          "Static tags must be key-value pairs (even number of elements)");
    }

    List<Tag> tags = new ArrayList<>(staticTags.length / 2);
    for (int i = 0; i < staticTags.length; i += 2) {
      tags.add(Tag.of(staticTags[i], staticTags[i + 1]));
    }
    return tags;
  }
}
