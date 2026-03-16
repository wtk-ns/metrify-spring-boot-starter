package io.wouns.metrify.utility;

import java.util.function.Consumer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class ReactiveMetricHelper {

  private ReactiveMetricHelper() {}

  public static Mono<?> wrapCounterMono(
      Object result, Runnable onSuccess, Consumer<Throwable> onError) {
    return ((Mono<?>) result)
        .doOnSuccess(value -> onSuccess.run())
        .doOnError(onError);
  }

  public static Flux<?> wrapCounterFlux(
      Object result, Runnable onComplete, Consumer<Throwable> onError) {
    return ((Flux<?>) result)
        .doOnComplete(onComplete)
        .doOnError(onError);
  }

}
