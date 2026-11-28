package io.github.endlesspixel.fakeplayer.core.http;

import io.github.endlesspixel.fakeplayer.core.util.library.LibraryManager;
import io.github.endlesspixel.fakeplayer.core.util.library.LibrarySet;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/** Undertow 不在插件 jar 内, 全部通过按需下载的 classloader 反射调用 */
public final class UndertowSupport {

  private static final String ADAPTER =
      "io.github.endlesspixel.fakeplayer.core.http.UndertowExchange";

  private static Object server;

  private UndertowSupport() {}

  public static synchronized void start(String host, int port, Consumer<Exchange> handler)
      throws Exception {
    if (server != null) {
      return;
    }
    var loader = LibraryManager.loader(LibrarySet.UNDERTOW);
    var undertow = Class.forName("io.undertow.Undertow", true, loader);
    var options = Class.forName("io.undertow.UndertowOptions", true, loader);
    var httpHandler = Class.forName("io.undertow.server.HttpHandler", true, loader);
    var blockingHandler =
        Class.forName("io.undertow.server.handlers.BlockingHandler", true, loader);

    var proxy =
        Proxy.newProxyInstance(
            loader,
            new Class<?>[] {httpHandler},
            (self, method, args) -> {
              if (method.getName().equals("handleRequest") && args != null && args.length == 1) {
                var exchange = args[0];
                var adapter = Class.forName(ADAPTER, true, loader);
                var constructor = adapter.getDeclaredConstructor(exchange.getClass());
                constructor.setAccessible(true);
                handler.accept((Exchange) constructor.newInstance(exchange));
                return null;
              }
              return switch (method.getName()) {
                case "toString" -> "FakePlayer-CE HTTP handler";
                case "hashCode" -> System.identityHashCode(self);
                case "equals" -> self == args[0];
                default -> null;
              };
            });

    var built = undertow.getMethod("builder").invoke(null);
    built = findMethod(built, "addHttpListener").invoke(built, port, host);
    var setOption = findMethod(built, "setServerOption");
    built = setOption.invoke(built, options.getField("MAX_ENTITY_SIZE").get(null), (long) 4096);
    built = setOption.invoke(built, options.getField("IDLE_TIMEOUT").get(null), 30_000);
    built = built.getClass().getMethod("setHandler", httpHandler).invoke(built, proxy);
    var instance = built.getClass().getMethod("build").invoke(built);
    instance.getClass().getMethod("start").invoke(instance);
    server = instance;
  }

  public static synchronized void stop() {
    if (server == null) {
      return;
    }
    try {
      server.getClass().getMethod("stop").invoke(server);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to stop HTTP admin server", e);
    } finally {
      server = null;
    }
  }

  private static Method findMethod(@NotNull Object target, @NotNull String name) {
    for (var method : target.getClass().getMethods()) {
      if (method.getName().equals(name) && method.getParameterCount() == 2) {
        return method;
      }
    }
    throw new IllegalStateException("No method " + name + " on " + target.getClass());
  }
}
