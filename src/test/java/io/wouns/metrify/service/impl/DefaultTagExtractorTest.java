package io.wouns.metrify.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Tag;
import io.wouns.metrify.service.TagResolver;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;

class DefaultTagExtractorTest {

  @Test
  void extractsStaticTagsOnly() {
    TagResolver tagResolver = mock(TagResolver.class);
    JoinPoint joinPoint = mock(JoinPoint.class);
    when(tagResolver.resolve(joinPoint)).thenReturn(List.of());

    DefaultTagExtractor extractor = new DefaultTagExtractor(tagResolver);
    List<Tag> tags = extractor.extract(new String[]{"env", "prod", "region", "us-east"}, joinPoint);

    assertThat(tags).containsExactly(
        Tag.of("env", "prod"),
        Tag.of("region", "us-east"));
  }

  @Test
  void combinesStaticAndDynamicTags() {
    TagResolver tagResolver = mock(TagResolver.class);
    JoinPoint joinPoint = mock(JoinPoint.class);
    when(tagResolver.resolve(joinPoint)).thenReturn(List.of(Tag.of("dynamic", "value")));

    DefaultTagExtractor extractor = new DefaultTagExtractor(tagResolver);
    List<Tag> tags = extractor.extract(new String[]{"static", "value"}, joinPoint);

    assertThat(tags).containsExactly(
        Tag.of("static", "value"),
        Tag.of("dynamic", "value"));
  }

  @Test
  void returnsEmptyWhenNoTags() {
    TagResolver tagResolver = mock(TagResolver.class);
    JoinPoint joinPoint = mock(JoinPoint.class);
    when(tagResolver.resolve(joinPoint)).thenReturn(List.of());

    DefaultTagExtractor extractor = new DefaultTagExtractor(tagResolver);
    List<Tag> tags = extractor.extract(new String[]{}, joinPoint);

    assertThat(tags).isEmpty();
  }

  @Test
  void throwsOnOddNumberOfStaticTags() {
    TagResolver tagResolver = mock(TagResolver.class);
    JoinPoint joinPoint = mock(JoinPoint.class);

    DefaultTagExtractor extractor = new DefaultTagExtractor(tagResolver);

    assertThatThrownBy(() -> extractor.extract(new String[]{"key"}, joinPoint))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("key-value pairs");
  }
}
