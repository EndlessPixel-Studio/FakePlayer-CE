package io.github.hello09x.fakeplayer.core.aichat;

import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * 把模型返回的 Markdown 转成 Minecraft 能显示的东西。
 *
 * <p>MC 的聊天栏不认 Markdown, 直接把模型输出丢进去只会看到一堆 {@code **}、{@code ###}、{@code ```}。 这里的原则是: <b>MC
 * 有对应能力就用</b> (加粗 / 斜体 / 删除线 / 可点击链接), <b>没有的就转成普通文本或丢掉</b> (标题转加粗、列表转 {@code
 * •}、代码块去掉围栏只留内容、分隔线直接丢)。链接在组件里是可点击的, 纯文本模式下退化成 「文字 (地址)」。
 *
 * <p>聊天栏只有一行, 所以所有换行都会被压成空格。
 */
public final class MarkdownRenderer {

  private static final Pattern LINK = Pattern.compile("\\[([^\\]]{1,200})]\\(([^)\\s]{1,500})\\)");

  private static final Pattern HEADING = Pattern.compile("^#{1,6}\\s*");

  private static final Pattern BULLET = Pattern.compile("^\\s*[-*+]\\s+");

  private static final Pattern ORDERED = Pattern.compile("^\\s*(\\d{1,3})[.)]\\s+");

  private static final Pattern QUOTE = Pattern.compile("^\\s*>+\\s*");

  private static final Pattern HR = Pattern.compile("^\\s*([-*_])\\s*(\\1\\s*){2,}$");

  /** 表格的分隔行, 例如 {@code |---|:---:|} */
  private static final Pattern TABLE_SEPARATOR = Pattern.compile("^\\s*\\|?[\\s:|-]+\\|?\\s*$");

  private MarkdownRenderer() {}

  /**
   * 转成带样式的组件 (私聊等能塞组件的场合使用)。
   *
   * @param markdown 模型返回的原始文本
   * @return 单行组件
   */
  public static @NotNull Component toComponent(@NotNull String markdown) {
    return render(markdown, false);
  }

  /**
   * 转成纯文本 (走 {@code Player#chat} 的公共聊天使用, 那里只接受字符串)。
   *
   * @param markdown 模型返回的原始文本
   * @return 单行纯文本
   */
  public static @NotNull String toPlainText(@NotNull String markdown) {
    return PlainTextComponentSerializer.plainText().serialize(render(markdown, true));
  }

  private static @NotNull Component render(@NotNull String markdown, boolean plain) {
    var builder = Component.text();
    var appended = false;

    for (var rawLine : markdown.split("\\R")) {
      var line = rawLine.strip();
      if (line.isEmpty()) {
        continue;
      }
      if (line.startsWith("```")) {
        // 围栏本身丢掉, 内容保留 (代码块的内容通常是命令行/JSON, 去掉围栏还能读)
        line = line.replace("```", "").strip();
        if (line.isEmpty()) {
          continue;
        }
      }
      if (HR.matcher(line).matches()) {
        continue;
      }
      var heading = line.startsWith("#");
      line = HEADING.matcher(line).replaceFirst("");

      if (line.indexOf('-') >= 0 && TABLE_SEPARATOR.matcher(line).matches()) {
        continue;
      }
      if (line.indexOf('|') >= 0) {
        // 表格没有对应渲染, 退化成空格分隔
        line = line.replace('|', ' ');
      }

      var bullet = BULLET.matcher(line);
      var ordered = ORDERED.matcher(line);
      if (bullet.find()) {
        line = "• " + line.substring(bullet.end());
      } else if (ordered.find()) {
        line = ordered.group(1) + ". " + line.substring(ordered.end());
      }
      line = QUOTE.matcher(line).replaceFirst("");
      line = line.replaceAll("\\s{2,}", " ").strip();
      if (line.isEmpty()) {
        continue;
      }

      if (appended) {
        builder.append(Component.space());
      }
      appendInline(builder, line, plain, heading);
      appended = true;
    }

    return appended ? builder.build() : Component.empty();
  }

  /** 一行内的样式状态; 链接会把一行切成多段, 状态必须跨段延续, 否则收尾标记会断在链接处 */
  private static final class InlineStyle {
    boolean bold;
    boolean italic;
    boolean strike;
    boolean code;

    /** 链接文字的基础样式 (aqua + 下划线 + 点击); 非链接段为 null */
    Style.Builder linkBase;

    InlineStyle copy() {
      var copy = new InlineStyle();
      copy.bold = bold;
      copy.italic = italic;
      copy.strike = strike;
      copy.code = code;
      copy.linkBase = linkBase;
      return copy;
    }
  }

  private static void appendInline(
      @NotNull TextComponent.Builder builder,
      @NotNull String line,
      boolean plain,
      boolean heading) {
    var style = new InlineStyle();
    var matcher = LINK.matcher(line);
    var last = 0;
    while (matcher.find()) {
      appendStyled(builder, line.substring(last, matcher.start()), plain, heading, style);
      var label = matcher.group(1);
      var url = matcher.group(2);
      if (plain) {
        appendStyled(builder, label + " (" + url + ")", plain, heading, style);
      } else {
        // 链接文字也走 appendStyled 解析其中的加粗/斜体/代码标记, 否则 [**粗**](url) 的星号会漏进聊天栏。
        // 用副本解析: 链接文字内的标记可以改变链接自己的样式, 但不能泄漏到链接之后的正文。
        // 链接基础样式作为起点叠加, 于是 **粗 [链接] 仍粗** 里链接也继承当前的粗体。
        var linkStyle = style.copy();
        linkStyle.linkBase =
            Style.style()
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.UNDERLINED, TextDecoration.State.TRUE)
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(Component.text(url)));
        if (heading) {
          linkStyle.bold = true;
        }
        appendStyled(builder, label, plain, heading, linkStyle);
      }
      last = matcher.end();
    }
    appendStyled(builder, line.substring(last), plain, heading, style);
  }

  /**
   * 处理行内标记。
   *
   * <p>不配对时直接把标记本身吃掉, 剩下的文字照常输出 —— 模型经常写出半截的 {@code **}, 不能让它们漏进聊天栏。
   */
  private static void appendStyled(
      @NotNull TextComponent.Builder builder,
      @NotNull String text,
      boolean plain,
      boolean heading,
      @NotNull InlineStyle style) {
    var buffer = new StringBuilder();

    for (var i = 0; i < text.length(); i++) {
      var c = text.charAt(i);
      if (c == '`') {
        flush(builder, buffer, plain, heading, style);
        style.code = !style.code;
        continue;
      }
      if (c == '*' || c == '_' || c == '~') {
        var doubled = nextIs(text, i, c);
        // 收尾标记后面必然是空格或行尾, 所以「正在生效」时无条件认它; 只有起始标记才需要判断边界
        var active = doubled ? (c == '~' ? style.strike : style.bold) : style.italic;
        if (!active && !markerStartsHere(text, i)) {
          buffer.append(c);
          continue;
        }
        flush(builder, buffer, plain, heading, style);
        if (doubled) {
          if (c == '~') {
            style.strike = !style.strike;
          } else {
            style.bold = !style.bold;
          }
          i++;
        } else {
          style.italic = !style.italic;
        }
        continue;
      }
      buffer.append(c);
    }
    flush(builder, buffer, plain, heading, style);
  }

  private static void flush(
      @NotNull TextComponent.Builder builder,
      @NotNull StringBuilder buffer,
      boolean plain,
      boolean heading,
      @NotNull InlineStyle style) {
    if (buffer.isEmpty()) {
      return;
    }
    // 链接文字以链接基础样式为起点叠加, 普通文字从空样式开始
    var applied = style.linkBase == null ? Style.style() : style.linkBase;
    if (style.bold || heading) {
      applied = applied.decoration(TextDecoration.BOLD, TextDecoration.State.TRUE);
    }
    if (style.italic) {
      applied = applied.decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE);
    }
    if (style.strike && !plain) {
      applied = applied.decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.TRUE);
    }
    if (style.code) {
      // MC 没有等宽字体, 用灰色区分行内代码
      applied = applied.color(NamedTextColor.GRAY);
    }
    builder.append(Component.text(buffer.toString()).style(applied));
    buffer.setLength(0);
  }

  private static boolean nextIs(@NotNull String text, int index, char c) {
    return index + 1 < text.length() && text.charAt(index + 1) == c;
  }

  /**
   * 判断某个标记是否是一个格式区的开始。
   *
   * <p>避免把 {@code name_1} 这种下划线、{@code a * b} 这种乘号当成格式标记: 起始标记后面不能是空白, 且 {@code _} 前面不能紧挨字母数字。
   */
  private static boolean markerStartsHere(@NotNull String text, int index) {
    var c = text.charAt(index);
    var before = index == 0 ? ' ' : text.charAt(index - 1);
    var after = index + 1 < text.length() ? text.charAt(index + 1) : ' ';
    if (Character.isWhitespace(after)) {
      return false;
    }
    return c != '_' || !Character.isLetterOrDigit(before);
  }
}
