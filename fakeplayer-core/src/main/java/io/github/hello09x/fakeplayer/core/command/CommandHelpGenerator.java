package io.github.hello09x.fakeplayer.core.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import io.github.hello09x.devtools.command.Page;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * 生成 /fp help 命令树，替代 devtools 的 HelpCommand（其只接受页码参数，无法查询子命令用法）。
 * <p>
 * 支持的用法：
 * <ul>
 *     <li>{@code /fp help} —— 第 1 页命令列表</li>
 *     <li>{@code /fp help <页码> [每页数]} —— 翻页（向后兼容原行为）</li>
 *     <li>{@code /fp help <命令名> [更多层级...]} —— 查看指定命令的详情，如 {@code /fp help config}、{@code /fp help look north}</li>
 * </ul>
 * 条目按发送者权限过滤，描述按配置语言本地化，子命令名提供 Tab 补全。
 */
final class CommandHelpGenerator {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    private CommandHelpGenerator() {
    }

    static void generate(CommandAPICommand root, CommandHelpLocalizer localizer) {
        var aliases = root.getAliases();
        var base = aliases != null && aliases.length > 0 && !aliases[0].isBlank()
                ? aliases[0]
                : root.getName();
        generate(root, base, localizer);
    }

    private static void generate(CommandAPICommand command, String path, CommandHelpLocalizer localizer) {
        var subcommands = command.getSubcommands();
        if (subcommands.isEmpty()) {
            return;
        }

        // 已有手写 help 的命令（如 /fp say help、/fp config help）保留原文案，不重复生成 help
        if (subcommands.stream().anyMatch(CommandHelpGenerator::isHelpSubcommand)) {
            for (var sub : subcommands) {
                if (!isHelpSubcommand(sub)) {
                    generate(sub, path + " " + sub.getName(), localizer);
                }
            }
            return;
        }

        command.withSubcommand(buildHelp(command, path, subcommands, localizer));

        for (var sub : subcommands) {
            generate(sub, path + " " + sub.getName(), localizer);
        }
    }

    private static CommandAPICommand buildHelp(CommandAPICommand command, String path, List<CommandAPICommand> subcommands, CommandHelpLocalizer localizer) {
        var usages = new ArrayList<Usage>(subcommands.size());
        for (var sub : subcommands) {
            var name = text(sub.getName(), GOLD);
            var description = sub.getShortDescription();
            usages.add(new Usage(sub, description == null || description.isEmpty()
                    ? name
                    : textOfChildren(name, text(" -", GRAY), text(description, WHITE))));
        }

        var headline = headline(path);

        return new CommandAPICommand("help")
                .withAliases("?")
                .withShortDescription(localizer.translate("fakeplayer.command.help.description"))
                .withOptionalArguments(new GreedyStringArgument("query")
                        .replaceSuggestions(ArgumentSuggestions.<CommandSender>strings(info -> usages.stream()
                                .filter(usage -> canUse(info.sender(), usage.command()))
                                .map(usage -> usage.command().getName())
                                .toArray(String[]::new))))
                .executes((sender, args) -> {
                    var query = (String) args.getOptional("query").orElse(null);
                    if (query == null || query.isBlank()) {
                        showList(sender, usages, headline, path, DEFAULT_PAGE, DEFAULT_SIZE);
                        return;
                    }

                    var parts = query.trim().split("\\s+");
                    if (parts[0].chars().allMatch(Character::isDigit)) {
                        showList(
                                sender,
                                usages,
                                headline,
                                path,
                                parseIntOr(parts[0], DEFAULT_PAGE),
                                parts.length > 1 ? parseIntOr(parts[1], DEFAULT_SIZE) : DEFAULT_SIZE
                        );
                        return;
                    }

                    showDetail(sender, command, path, parts);
                });
    }

    private static void showList(CommandSender sender, List<Usage> usages, Component headline, String path, int page, int size) {
        var safePage = Math.max(1, page);
        var safeSize = Math.max(1, size);
        var allowed = usages.stream().filter(usage -> canUse(sender, usage.command())).toList();
        sender.sendMessage(Page.of(allowed, safePage, safeSize).asComponent(
                headline,
                usage -> {
                    var row = textOfChildren(text("- "), usage.component());
                    var fullName = path + " " + usage.command().getName();
                    var hasChildren = usage.command().getSubcommands().stream().anyMatch(sub -> !isHelpSubcommand(sub));
                    if (hasChildren) {
                        // 有子命令的命令必然存在 help（自动生成的或手写的），可直接跳转查看
                        var more = "/" + fullName + " help";
                        return row.append(text(" " + more, DARK_GRAY).clickEvent(runCommand(more)));
                    }
                    return row.append(text(" /" + fullName, DARK_GRAY).clickEvent(suggestCommand("/" + fullName)));
                },
                i -> "/" + path + " help " + i + " " + safeSize
        ));
    }

    private static void showDetail(CommandSender sender, CommandAPICommand command, String path, String[] parts) {
        var current = command;
        var fullPath = new StringBuilder(path);
        for (var part : parts) {
            var next = current.getSubcommands().stream()
                    .filter(sub -> matches(sub, part))
                    .findFirst()
                    .orElse(null);
            if (next == null || !canUse(sender, next)) {
                sender.sendMessage(textOfChildren(
                        translatable("fakeplayer.command.help.unknown", RED),
                        space(),
                        text(part, WHITE)));
                return;
            }
            current = next;
            fullPath.append(' ').append(current.getName());
        }
        sender.sendMessage(renderDetail(current, fullPath.toString(), sender));
    }

    private static Component renderDetail(CommandAPICommand command, String fullPath, CommandSender sender) {
        var lines = new ArrayList<Component>();
        lines.add(headline(fullPath));

        var description = command.getShortDescription();
        if (description != null && !description.isEmpty()) {
            lines.add(text(description, WHITE));
        }
        var fullDescription = command.getFullDescription();
        if (fullDescription != null && !fullDescription.isEmpty() && !fullDescription.equals(description)) {
            lines.add(text(fullDescription, GRAY));
        }

        var usage = usageOf(command, fullPath);
        if (usage != null) {
            lines.add(text(usage, WHITE));
        }

        var aliases = command.getAliases();
        if (aliases != null && aliases.length > 0) {
            lines.add(textOfChildren(
                    translatable("fakeplayer.command.help.aliases", GRAY),
                    space(),
                    text(String.join(", ", aliases), WHITE)));
        }

        var visible = command.getSubcommands().stream()
                .filter(sub -> !isHelpSubcommand(sub))
                .filter(sub -> canUse(sender, sub))
                .toList();
        if (!visible.isEmpty()) {
            var line = textOfChildren(translatable("fakeplayer.command.help.subcommands", GRAY), space());
            for (var i = 0; i < visible.size(); i++) {
                if (i > 0) {
                    line = line.append(text(", ", GRAY));
                }
                var sub = visible.get(i);
                line = line.append(text(sub.getName(), GOLD)
                        .clickEvent(suggestCommand("/" + fullPath + " " + sub.getName())));
            }
            lines.add(line);
        }

        return joinLines(lines);
    }

    /**
     * 组装该命令的用法行：优先复用手写 help 文案（如 fakeplayer.command.say.help），
     * 其次显式设置的 usage，再次由参数/子命令拼装。
     */
    private static String usageOf(CommandAPICommand command, String fullPath) {
        var handWritten = command.getSubcommands().stream()
                .filter(CommandHelpGenerator::isHelpSubcommand)
                .map(CommandAPICommand::getShortDescription)
                .filter(desc -> desc != null && !desc.isEmpty())
                .findFirst()
                .orElse(null);
        if (handWritten != null) {
            return handWritten;
        }

        var explicit = command.getUsage();
        if (explicit != null && explicit.length > 0 && !explicit[0].isBlank()) {
            return "/" + fullPath + " " + String.join(" ", explicit);
        }

        var arguments = command.getArguments();
        if (arguments != null && !arguments.isEmpty()) {
            var sb = new StringBuilder("/").append(fullPath);
            for (var argument : arguments) {
                sb.append(" <").append(argument.getNodeName()).append('>');
            }
            return sb.toString();
        }

        if (!command.getSubcommands().isEmpty()) {
            return "/" + fullPath + " <subcommand>";
        }
        return "/" + fullPath;
    }

    private static Component joinLines(List<Component> lines) {
        var result = lines.get(0);
        for (var i = 1; i < lines.size(); i++) {
            result = result.append(newline()).append(lines.get(i));
        }
        return result;
    }

    private static Component headline(String path) {
        return textOfChildren(text("---------- ", YELLOW), text(path, WHITE), text(" ----------", YELLOW));
    }

    private static boolean canUse(CommandSender sender, CommandAPICommand command) {
        var permission = command.getPermission();
        if (permission == null || permission == CommandPermission.NONE) {
            return true;
        }
        if (permission == CommandPermission.OP) {
            return sender.isOp();
        }
        return permission.getPermission().map(sender::hasPermission).orElse(true);
    }

    private static boolean matches(CommandAPICommand command, String input) {
        if (command.getName().equalsIgnoreCase(input)) {
            return true;
        }
        var aliases = command.getAliases();
        if (aliases != null) {
            for (var alias : aliases) {
                if (alias.equalsIgnoreCase(input)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isHelpSubcommand(CommandAPICommand command) {
        if ("help".equalsIgnoreCase(command.getName())) {
            return true;
        }
        var aliases = command.getAliases();
        if (aliases != null) {
            for (var alias : aliases) {
                if ("?".equals(alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int parseIntOr(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private record Usage(CommandAPICommand command, Component component) {
    }

}
