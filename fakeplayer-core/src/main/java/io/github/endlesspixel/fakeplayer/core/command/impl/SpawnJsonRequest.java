package io.github.endlesspixel.fakeplayer.core.command.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@code /fp spawnjson} 的参数。只解析与校验, 不碰 Bukkit API, 因此可以离线测试。
 *
 * <p>第一版支持名称、UUID、世界与坐标、朝向和寿命; NBT ({@code #192}) 不在范围内, 未知字段一律拒绝,
 * 这样拼错字段名能立刻发现, 以后加新字段再显式放开。
 */
public record SpawnJsonRequest(
    @Nullable String name,
    @Nullable String uuid,
    @Nullable String world,
    @Nullable Double x,
    @Nullable Double y,
    @Nullable Double z,
    @Nullable Float yaw,
    @Nullable Float pitch,
    @Nullable Long lifespanSeconds) {

  /** JSON 文本的最大长度, 防止一条命令塞进超大对象 */
  public static final int MAX_LENGTH = 4096;

  private static final Set<String> ALLOWED =
      Set.of("name", "uuid", "world", "x", "y", "z", "yaw", "pitch", "lifespan_seconds");

  /** 解析错误, 携带消息键和参数, 由命令层按玩家语言渲染 */
  public static final class Invalid extends RuntimeException {
    private final String key;
    private final Object[] args;

    Invalid(@NotNull String key, @NotNull Object... args) {
      super(key);
      this.key = key;
      this.args = args;
    }

    public @NotNull String key() {
      return this.key;
    }

    public @NotNull Object[] args() {
      return this.args;
    }
  }

  public static @NotNull SpawnJsonRequest parse(@NotNull String raw) {
    if (raw.isBlank()) {
      throw new Invalid("fakeplayer.command.spawn.json.error.invalid", raw);
    }
    if (raw.length() > MAX_LENGTH) {
      throw new Invalid("fakeplayer.command.spawn.json.error.too-long", MAX_LENGTH);
    }

    final JsonObject root;
    try {
      var parsed = JsonParser.parseString(raw);
      if (!parsed.isJsonObject()) {
        throw new Invalid("fakeplayer.command.spawn.json.error.invalid", raw);
      }
      root = parsed.getAsJsonObject();
    } catch (JsonParseException e) {
      throw new Invalid("fakeplayer.command.spawn.json.error.invalid", raw);
    }

    for (var key : root.keySet()) {
      if (!ALLOWED.contains(key)) {
        throw new Invalid("fakeplayer.command.spawn.json.error.unknown", key);
      }
    }

    var name = text(root, "name");
    var uuid = text(root, "uuid");
    var world = text(root, "world");
    var x = number(root, "x");
    var y = number(root, "y");
    var z = number(root, "z");
    if ((x == null) != (y == null) || (y == null) != (z == null)) {
      throw new Invalid("fakeplayer.command.spawn.json.error.coordinates");
    }
    var yaw = number(root, "yaw");
    var pitch = number(root, "pitch");

    Long lifespan = null;
    var lifespanEl = root.get("lifespan_seconds");
    if (lifespanEl != null && !lifespanEl.isJsonNull()) {
      var value = number(root, "lifespan_seconds");
      if (value == null || value <= 0 || value > Long.MAX_VALUE) {
        throw new Invalid("fakeplayer.command.spawn.json.error.lifespan");
      }
      lifespan = value.longValue();
    }

    return new SpawnJsonRequest(
        name, uuid, world, x, y, z, yaw == null ? null : yaw.floatValue(),
        pitch == null ? null : pitch.floatValue(), lifespan);
  }

  private static @Nullable String text(@NotNull JsonObject root, @NotNull String key) {
    var el = root.get(key);
    if (el == null || el.isJsonNull()) {
      return null;
    }
    if (!el.isJsonPrimitive() || !((JsonPrimitive) el).isString()) {
      throw new Invalid("fakeplayer.command.spawn.json.error.type", key, "string");
    }
    return el.getAsString();
  }

  private static @Nullable Double number(@NotNull JsonObject root, @NotNull String key) {
    var el = root.get(key);
    if (el == null || el.isJsonNull()) {
      return null;
    }
    if (!el.isJsonPrimitive() || !((JsonPrimitive) el).isNumber()) {
      throw new Invalid("fakeplayer.command.spawn.json.error.type", key, "number");
    }
    var value = el.getAsDouble();
    if (!Double.isFinite(value)) {
      throw new Invalid("fakeplayer.command.spawn.json.error.type", key, "finite number");
    }
    return value;
  }
}
