package io.github.endlesspixel.fakeplayer.http;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Undertow 适配; 只在 blocking handler 中使用, 读写流会阻塞工作线程 */
final class UndertowExchange implements Exchange {

  private final HttpServerExchange exchange;

  UndertowExchange(@NotNull HttpServerExchange exchange) {
    this.exchange = exchange;
  }

  @Override
  public @NotNull String getRequestMethod() {
    return this.exchange.getRequestMethod().toString();
  }

  @Override
  public @NotNull URI getRequestURI() {
    return URI.create(this.exchange.getRequestURI());
  }

  @Override
  public @Nullable String getRequestHeader(@NotNull String name) {
    return this.exchange.getRequestHeaders().getFirst(name);
  }

  @Override
  public @Nullable InetSocketAddress getRemoteAddress() {
    return this.exchange.getSourceAddress();
  }

  @Override
  public void addResponseHeader(@NotNull String name, @NotNull String value) {
    this.exchange.getResponseHeaders().put(new HttpString(name), value);
  }

  @Override
  public void sendResponseHeaders(int status, long contentLength) {
    this.exchange.setStatusCode(status);
    if (contentLength >= 0) {
      this.exchange.setResponseContentLength(contentLength);
    }
  }

  @Override
  public @NotNull OutputStream getResponseBody() throws IOException {
    return this.exchange.getOutputStream();
  }

  @Override
  public void close() {
    this.exchange.endExchange();
  }
}
