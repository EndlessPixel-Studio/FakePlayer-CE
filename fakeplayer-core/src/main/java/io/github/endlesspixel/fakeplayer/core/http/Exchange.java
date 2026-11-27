package io.github.endlesspixel.fakeplayer.core.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 管理接口用到的最小请求/响应抽象。
 *
 * <p>具体实现由 HTTP 服务器适配 (见 {@link UndertowExchange}), 这样 handler 不再绑定某个服务器的 API。
 */
public interface Exchange {

  @NotNull
  String getRequestMethod();

  @NotNull
  URI getRequestURI();

  @Nullable
  String getRequestHeader(@NotNull String name);

  @Nullable
  InetSocketAddress getRemoteAddress();

  void addResponseHeader(@NotNull String name, @NotNull String value);

  /** 写入状态码与 Content-Length; length 为负时交给服务器决定 (分块传输) */
  void sendResponseHeaders(int status, long contentLength) throws IOException;

  @NotNull
  OutputStream getResponseBody() throws IOException;

  void close();
}
