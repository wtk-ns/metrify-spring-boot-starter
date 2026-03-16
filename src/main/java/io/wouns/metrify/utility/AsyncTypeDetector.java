package io.wouns.metrify.utility;

public final class AsyncTypeDetector {

  private static final Class<?> MONO_CLASS = loadClass("reactor.core.publisher.Mono");
  private static final Class<?> FLUX_CLASS = loadClass("reactor.core.publisher.Flux");

  private AsyncTypeDetector() {}

  public static boolean isMono(Object result) {
    return MONO_CLASS != null && MONO_CLASS.isInstance(result);
  }

  public static boolean isFlux(Object result) {
    return FLUX_CLASS != null && FLUX_CLASS.isInstance(result);
  }

  private static Class<?> loadClass(String className) {
    try {
      return Class.forName(className, false, AsyncTypeDetector.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
