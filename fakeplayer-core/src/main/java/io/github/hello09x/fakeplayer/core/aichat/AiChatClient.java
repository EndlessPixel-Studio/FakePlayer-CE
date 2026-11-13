package io.github.hello09x.fakeplayer.core.aichat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

/** OpenAI 兼容的对话接口客户端 */
@Singleton
public class AiChatClient {

  private static final Logger log = Logger.getLogger(AiChatClient.class.getName());

  private static final Gson gson = new Gson();

  private static final String USER_AGENT = "FakePlayer-CE";
  private static final String CONTENT_TYPE_JSON = "application/json";
  private static final String AUTH_PREFIX = "Bearer ";

  private static final String FIELD_MODEL = "model";
  private static final String FIELD_MESSAGES = "messages";
  private static final String FIELD_ROLE = "role";
  private static final String FIELD_CONTENT = "content";
  private static final String FIELD_TEMPERATURE = "temperature";
  private static final String FIELD_TOP_P = "top_p";
  private static final String FIELD_PRESENCE_PENALTY = "presence_penalty";
  private static final String FIELD_FREQUENCY_PENALTY = "frequency_penalty";
  private static final String FIELD_MAX_TOKENS = "max_tokens";
  private static final String FIELD_CHOICES = "choices";
  private static final String FIELD_MESSAGE = "message";
  private static final String FIELD_TEXT = "text";

  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
  private static final int MAX_RETRIES = 2;
  private static final long INITIAL_BACKOFF_MS = 500L;
  private static final int ABBREVIATE_LENGTH = 200;

  private final HttpClient http =
      HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();

  /** 请求一次对话补全, 返回模型给出的文本 */
  public @NotNull CompletableFuture<String> complete(
      @NotNull AiChatConfig config, @NotNull String apiKey, @NotNull List<ChatMessage> messages) {
    if (messages.isEmpty()) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("messages must not be empty"));
    }
    if (apiKey.isBlank()) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("apiKey must not be blank"));
    }

    var url = config.getCompletionsUrl();
    if (url == null || url.isBlank()) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("completionsUrl must not be blank"));
    }

    final HttpRequest request;
    try {
      request =
          HttpRequest.newBuilder()
              .uri(URI.create(url))
              .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
              .header("Content-Type", CONTENT_TYPE_JSON)
              .header("Authorization", AUTH_PREFIX + apiKey)
              .header("User-Agent", USER_AGENT)
              .POST(
                  HttpRequest.BodyPublishers.ofString(
                      this.buildBody(config, messages), StandardCharsets.UTF_8))
              .build();
    } catch (IllegalArgumentException e) {
      // URI 非法或 timeout <= 0 等情况
      return CompletableFuture.failedFuture(e);
    }

    return this.sendWithRetry(request, 0).thenApply(this::parseContent);
  }

  private @NotNull CompletableFuture<String> sendWithRetry(
      @NotNull HttpRequest request, int attempt) {
    return http.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        .thenApply(
            response -> {
              int code = response.statusCode();
              // 429 / 5xx 视为可重试
              if (code == 429 || (code >= 500 && code < 600)) {
                throw new CompletionException(
                    new RetryableException("HTTP " + code + ": " + abbreviate(response.body())));
              }
              // 其余非 2xx 直接失败 (兼容 201/206 等)
              if (code < 200 || code >= 300) {
                throw new CompletionException(
                    new IOException("HTTP " + code + ": " + abbreviate(response.body())));
              }
              return response.body();
            })
        .exceptionallyCompose(
            ex -> {
              var cause = unwrap(ex);
              if (cause instanceof RetryableException && attempt < MAX_RETRIES) {
                long backoff = INITIAL_BACKOFF_MS << attempt;
                log.log(
                    Level.WARNING,
                    "AiChat request failed (attempt {0}): {1}, retry in {2}ms",
                    new Object[] {attempt + 1, cause.getMessage(), backoff});
                return CompletableFuture.supplyAsync(
                        () -> null,
                        CompletableFuture.delayedExecutor(backoff, TimeUnit.MILLISECONDS))
                    .thenCompose(ignored -> this.sendWithRetry(request, attempt + 1));
              }
              return CompletableFuture.failedFuture(ex);
            });
  }

  private @NotNull String buildBody(
      @NotNull AiChatConfig config, @NotNull List<ChatMessage> messages) {
    var root = new JsonObject();
    root.addProperty(FIELD_MODEL, config.getModel());

    var array = new JsonArray();
    for (var message : messages) {
      var item = new JsonObject();
      item.addProperty(FIELD_ROLE, message.role());
      item.addProperty(FIELD_CONTENT, message.content());
      array.add(item);
    }
    root.add(FIELD_MESSAGES, array);

    root.addProperty(FIELD_TEMPERATURE, config.getTemperature());
    root.addProperty(FIELD_TOP_P, config.getTopP());
    root.addProperty(FIELD_PRESENCE_PENALTY, config.getPresencePenalty());
    root.addProperty(FIELD_FREQUENCY_PENALTY, config.getFrequencyPenalty());
    if (config.getMaxTokens() > 0) {
      root.addProperty(FIELD_MAX_TOKENS, config.getMaxTokens());
    }

    return gson.toJson(root);
  }

  private @NotNull String parseContent(@NotNull String body) {
    final JsonElement root;
    try {
      root = JsonParser.parseString(body);
    } catch (JsonSyntaxException e) {
      throw new CompletionException(new IOException("Invalid JSON: " + abbreviate(body), e));
    }
    if (!root.isJsonObject()) {
      throw new CompletionException(
          new IOException("Response is not an object: " + abbreviate(body)));
    }

    var choicesEl = root.getAsJsonObject().get(FIELD_CHOICES);
    if (choicesEl == null || !choicesEl.isJsonArray() || choicesEl.getAsJsonArray().isEmpty()) {
      throw new CompletionException(new IOException("No choices: " + abbreviate(body)));
    }

    var firstEl = choicesEl.getAsJsonArray().get(0);
    if (!firstEl.isJsonObject()) {
      throw new CompletionException(new IOException("Invalid choice: " + abbreviate(body)));
    }

    var messageEl = firstEl.getAsJsonObject().get(FIELD_MESSAGE);
    if (messageEl == null || !messageEl.isJsonObject()) {
      throw new CompletionException(new IOException("No message: " + abbreviate(body)));
    }

    var contentEl = messageEl.getAsJsonObject().get(FIELD_CONTENT);
    if (contentEl == null || contentEl.isJsonNull()) {
      throw new CompletionException(new IOException("No content: " + abbreviate(body)));
    }

    // 部分兼容实现会把消息包在内容数组里
    if (contentEl.isJsonArray()) {
      var builder = new StringBuilder();
      for (var part : contentEl.getAsJsonArray()) {
        if (!part.isJsonObject()) {
          continue;
        }
        var textEl = part.getAsJsonObject().get(FIELD_TEXT);
        if (textEl != null && textEl.isJsonPrimitive()) {
          builder.append(textEl.getAsString());
        }
      }
      var text = builder.toString().trim();
      if (text.isEmpty()) {
        throw new CompletionException(
            new IOException("Empty content array: " + abbreviate(body)));
      }
      return text;
    }

    if (!contentEl.isJsonPrimitive()) {
      throw new CompletionException(
          new IOException("Unexpected content type: " + abbreviate(body)));
    }
    return contentEl.getAsString().trim();
  }

  private static @NotNull String abbreviate(@NotNull String text) {
    var single = text.replace('\n', ' ').replace('\r', ' ').trim();
    return single.length() <= ABBREVIATE_LENGTH
        ? single
        : single.substring(0, ABBREVIATE_LENGTH) + "...";
  }

  private static @NotNull Throwable unwrap(@NotNull Throwable ex) {
    var current = ex;
    while (current instanceof CompletionException || current instanceof ExecutionException) {
      var cause = current.getCause();
      if (cause == null) {
        break;
      }
      current = cause;
    }
    return current;
  }

  /** 可重试异常 (429 / 5xx), 内部使用 */
  private static final class RetryableException extends IOException {
    RetryableException(String message) {
      super(message);
    }
  }
}
