package io.github.hello09x.fakeplayer.core.aichat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;

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

/**
 * OpenAI 兼容的对话接口客户端
 */
@Singleton
public class AiChatClient {

    private final static Gson gson = new Gson();

    private final HttpClient http = HttpClient.newHttpClient();

    /**
     * 请求一次对话补全, 返回模型给出的文本
     */
    public @NotNull CompletableFuture<String> complete(
            @NotNull AiChatConfig config,
            @NotNull String apiKey,
            @NotNull List<ChatMessage> messages
    ) {
        var request = HttpRequest.newBuilder()
                                 .uri(URI.create(config.getCompletionsUrl()))
                                 .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                                 .header("Content-Type", "application/json")
                                 .header("Authorization", "Bearer " + apiKey)
                                 .POST(HttpRequest.BodyPublishers.ofString(this.buildBody(config, messages), StandardCharsets.UTF_8))
                                 .build();

        return http.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                   .thenApply(response -> {
                       if (response.statusCode() != 200) {
                           throw new CompletionException(new IOException(
                                   "HTTP " + response.statusCode() + ": " + abbreviate(response.body())
                           ));
                       }
                       return this.parseContent(response.body());
                   });
    }

    private @NotNull String buildBody(@NotNull AiChatConfig config, @NotNull List<ChatMessage> messages) {
        var root = new JsonObject();
        root.addProperty("model", config.getModel());

        var array = new JsonArray();
        for (var message : messages) {
            var item = new JsonObject();
            item.addProperty("role", message.role());
            item.addProperty("content", message.content());
            array.add(item);
        }
        root.add("messages", array);

        root.addProperty("temperature", config.getTemperature());
        root.addProperty("top_p", config.getTopP());
        root.addProperty("presence_penalty", config.getPresencePenalty());
        root.addProperty("frequency_penalty", config.getFrequencyPenalty());
        if (config.getMaxTokens() > 0) {
            root.addProperty("max_tokens", config.getMaxTokens());
        }

        return gson.toJson(root);
    }

    private @NotNull String parseContent(@NotNull String body) {
        var json = JsonParser.parseString(body).getAsJsonObject();
        var choices = json.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new CompletionException(new IOException("No choices in response: " + abbreviate(body)));
        }

        var message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        if (message == null || !message.has("content") || message.get("content").isJsonNull()) {
            throw new CompletionException(new IOException("No content in response: " + abbreviate(body)));
        }

        // 部分兼容实现会把消息包在内容数组里
        var content = message.get("content");
        if (content.isJsonArray()) {
            var builder = new StringBuilder();
            for (var part : content.getAsJsonArray()) {
                if (part.isJsonObject() && part.getAsJsonObject().has("text")) {
                    builder.append(part.getAsJsonObject().get("text").getAsString());
                }
            }
            return builder.toString().trim();
        }

        return content.getAsString().trim();
    }

    private static @NotNull String abbreviate(@NotNull String text) {
        var single = text.replace('\n', ' ').trim();
        return single.length() <= 200 ? single : single.substring(0, 200) + "...";
    }

}
