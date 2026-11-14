package io.github.hello09x.fakeplayer.core.command;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.Plugin;

/** 按插件配置的语言解析帮助文案; 未翻译的键返回键本身。 */
final class CommandHelpLocalizer {

  private static final String BUNDLE_PATH = "message/message";
  private static final String EXTENSION = ".properties";
  private static final Map<String, Properties> CACHE = new ConcurrentHashMap<>();

  private final Locale locale;
  private final ClassLoader classLoader;

  CommandHelpLocalizer(Plugin plugin, Locale locale) {
    this.locale = locale;
    var pluginClassLoader = plugin.getClass().getClassLoader();
    this.classLoader =
        pluginClassLoader != null ? pluginClassLoader : ClassLoader.getSystemClassLoader();
  }

  private Properties loadProperties(Locale locale) {
    return CACHE.computeIfAbsent(
        locale.toLanguageTag(),
        key -> {
          var props = new Properties();

          // Load locale-specific resource: e.g., message/message_zh_CN.properties
          if (!locale.getCountry().isEmpty()) {
            loadResource(
                props,
                BUNDLE_PATH + "_" + locale.getLanguage() + "_" + locale.getCountry() + EXTENSION);
          }

          // Load language-specific resource: e.g., message/message_zh.properties
          loadResource(props, BUNDLE_PATH + "_" + locale.getLanguage() + EXTENSION);

          // Load base resource: message/message.properties (as fallback)
          loadResource(props, BUNDLE_PATH + EXTENSION);

          return props;
        });
  }

  private void loadResource(Properties props, String resourcePath) {
    try (var is = classLoader.getResourceAsStream(resourcePath)) {
      if (is != null) {
        try (var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
          props.load(reader);
        }
      }
    } catch (Exception ignored) {
    }
  }

  private Properties baseProperties() {
    var props = new Properties();
    loadResource(props, BUNDLE_PATH + EXTENSION);
    return props;
  }

  String translate(String key) {
    var props = loadProperties(locale);
    return props.getProperty(key, key);
  }

  /**
   * 是否是消息文件里的键。
   *
   * <p>帮助列表需要保留键而不是提前翻译成文本, 才能按每个玩家的语言渲染。
   */
  boolean isKey(String raw) {
    return raw != null
        && CACHE.computeIfAbsent("keys", ignored -> baseProperties()).containsKey(raw);
  }
}
