package io.wouns.metrify.aspect;

import io.wouns.metrify.annotation.BusinessMetric;
import io.wouns.metrify.annotation.MetricCounter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SuppressWarnings("NullableProblems")
class ReactiveTestService {

  @MetricCounter("mono.count")
  public Mono<String> monoCount() {
    return Mono.just("done");
  }

  @MetricCounter("mono.count.fail")
  public Mono<String> monoCountFailing() {
    return Mono.error(new RuntimeException("fail"));
  }

  @MetricCounter("flux.count")
  public Flux<String> fluxCount() {
    return Flux.just("a", "b", "c");
  }

  @MetricCounter("flux.count.fail")
  public Flux<String> fluxCountFailing() {
    return Flux.error(new IllegalStateException("fail"));
  }

  @BusinessMetric("mono.business")
  public Mono<String> monoBusinessMetric() {
    return Mono.just("done");
  }

  @BusinessMetric("flux.business")
  public Flux<Integer> fluxBusinessMetric() {
    return Flux.just(1, 2, 3);
  }
}
