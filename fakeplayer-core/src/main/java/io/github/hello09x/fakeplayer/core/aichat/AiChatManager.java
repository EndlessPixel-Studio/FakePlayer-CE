package io.github.hello09x.fakeplayer.core.aichat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.api.spi.ActionSetting;
import io.github.hello09x.fakeplayer.api.spi.ActionType;
import io.github.hello09x.fakeplayer.api.spi.NMSBridge;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.entity.Fakeplayer;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerList;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * 组织假人的 AI 对话: 维护每个假人的上下文、驱动持续的发言任务
 */
@Singleton
public class AiChatManager {

    private final static Logger log = Main.getInstance().getLogger();

    private final AiChatConfig config;
    private final AiChatClient client;
    private final NMSBridge bridge;
    private final FakeplayerList fakeplayerList;

    private final AtomicLong lastAutoReplyAt = new AtomicLong(0);

    private final Map<UUID, Deque<ChatMessage>> histories = new ConcurrentHashMap<>();

    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    private final Set<UUID> inFlight = ConcurrentHashMap.newKeySet();

    private final static int MAX_RECENT_CHATS = 50;

    private final Deque<String> recentChats = new ArrayDeque<>();

    @Inject
    public AiChatManager(AiChatConfig config, AiChatClient client, NMSBridge bridge, FakeplayerList fakeplayerList) {
        this.config = config;
        this.client = client;
        this.bridge = bridge;
        this.fakeplayerList = fakeplayerList;
    }

    /**
     * 让假人说一句 (target 为空时发到公共聊天)
     */
    public void say(@NotNull Player fake, @Nullable Player target) {
        this.generate(fake, target);
    }

    /**
     * 记录一条公共聊天, 供后续生成时参考
     */
    public void recordChat(@NotNull String speaker, @NotNull String message) {
        if (config.getRecentChatLines() <= 0 || message.isBlank()) {
            return;
        }
        synchronized (this.recentChats) {
            this.recentChats.addLast(speaker + ": " + message);
            while (this.recentChats.size() > MAX_RECENT_CHATS) {
                this.recentChats.removeFirst();
            }
        }
    }

    /**
     * 玩家发言时调用: 内容像样、冷却已过且随机命中才会让某个假人接一句
     */
    public void onPlayerChat(@NotNull Player speaker, @NotNull String message) {
        if (this.isFake(speaker)) {
            // 假人自己的发言不参与触发, 避免自问自答
            return;
        }

        this.recordChat(speaker.getName(), message);

        if (!config.isAutoReplyEnabled() || !this.isMeaningful(message) || !this.tryAcquireAutoReply()) {
            return;
        }

        var fake = this.pickFakeplayer();
        if (fake != null) {
            this.generate(fake, null);
        }
    }

    private boolean isFake(@NotNull Player player) {
        return this.fakeplayerList.getAll().stream().anyMatch(fake -> fake.getPlayer().equals(player));
    }

    /**
     * 过滤掉「1」「?」「哈哈哈哈」这类不值得接话的消息
     */
    private boolean isMeaningful(@NotNull String message) {
        var text = message.trim();
        if (text.length() < config.getAutoReplyMinLength() || text.length() > config.getAutoReplyMaxLength()) {
            return false;
        }

        long alphanumeric = text.codePoints().filter(Character::isLetterOrDigit).count();
        if (alphanumeric * 2 < text.length()) {
            // 大半是符号或表情
            return false;
        }

        long distinct = text.codePoints().distinct().count();
        return distinct * 3 >= text.length();
    }

    private boolean tryAcquireAutoReply() {
        var now = System.currentTimeMillis();
        var last = this.lastAutoReplyAt.get();
        if (now - last < config.getAutoReplyCooldownSeconds() * 1000L) {
            return false;
        }
        if (ThreadLocalRandom.current().nextDouble() >= config.getAutoReplyChance()) {
            return false;
        }
        return this.lastAutoReplyAt.compareAndSet(last, now);
    }

    private @Nullable Player pickFakeplayer() {
        var candidates = this.fakeplayerList
                .getAll()
                .stream()
                .map(Fakeplayer::getPlayer)
                .filter(player -> player.isOnline() && player.isValid())
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    /**
     * 让假人持续发言, 直到 {@link #stop(Player)}
     */
    public void start(@NotNull Player fake, @Nullable Player target, @Nullable Integer intervalTicks) {
        this.stop(fake);

        var ticks = intervalTicks == null ? config.getIntervalTicks() : Math.max(1, intervalTicks);
        var task = Schedulers.entityTimer(Main.getInstance(), fake, ticks, ticks, () -> this.generate(fake, target));
        if (task != null) {
            this.tasks.put(fake.getUniqueId(), task);
        }

        this.generate(fake, target);
    }

    /**
     * @return 是否真的有正在运行的持续发言任务
     */
    public boolean stop(@NotNull Player fake) {
        var task = this.tasks.remove(fake.getUniqueId());
        if (task == null) {
            return false;
        }
        task.cancel();
        return true;
    }

    public void forget(@NotNull Player fake) {
        this.stop(fake);
        this.histories.remove(fake.getUniqueId());
    }

    private void generate(@NotNull Player fake, @Nullable Player target) {
        var apiKey = config.resolveApiKey();
        if (apiKey == null) {
            return;
        }

        var id = fake.getUniqueId();
        if (!this.inFlight.add(id)) {
            // 上一次请求还没回来, 直接跳过这一轮, 避免请求堆积
            return;
        }

        var history = this.histories.computeIfAbsent(id, key -> new ArrayDeque<>());
        var turn = config.getTurnPrompt().replace("{name}", fake.getName());

        var messages = new ArrayList<ChatMessage>();
        messages.add(new ChatMessage(ChatMessage.ROLE_SYSTEM, config.getSystemPrompt().replace("{name}", fake.getName())));

        var recent = this.recentChatSnapshot();
        if (!recent.isEmpty()) {
            messages.add(new ChatMessage(ChatMessage.ROLE_SYSTEM, "最近的公共聊天记录:\n" + String.join("\n", recent)));
        }

        synchronized (history) {
            messages.addAll(history);
        }
        messages.add(new ChatMessage(ChatMessage.ROLE_USER, turn));

        this.client.complete(config, apiKey, messages).whenComplete((reply, error) -> {
            this.inFlight.remove(id);
            if (error != null) {
                log.warning("假人 %s 的 AI 对话请求失败: %s".formatted(fake.getName(), rootMessage(error)));
                return;
            }
            if (reply.isBlank()) {
                return;
            }

            synchronized (history) {
                history.addLast(new ChatMessage(ChatMessage.ROLE_USER, turn));
                history.addLast(new ChatMessage(ChatMessage.ROLE_ASSISTANT, reply));
                this.trim(history);
            }

            Schedulers.runFor(Main.getInstance(), fake, () -> this.deliver(fake, target, reply));
        });
    }

    private @NotNull List<String> recentChatSnapshot() {
        var limit = config.getRecentChatLines();
        if (limit <= 0) {
            return List.of();
        }
        synchronized (this.recentChats) {
            var all = new ArrayList<>(this.recentChats);
            return all.subList(Math.max(0, all.size() - limit), all.size());
        }
    }

    private void trim(@NotNull Deque<ChatMessage> history) {
        var limit = config.getMaxContext();
        if (limit <= 0) {
            return;
        }
        while (history.size() > limit * 2) {
            history.removeFirst();
        }
    }

    private void deliver(@NotNull Player fake, @Nullable Player target, @NotNull String message) {
        if (!fake.isOnline() || !fake.isValid()) {
            return;
        }

        if (target != null) {
            if (!target.isOnline()) {
                return;
            }
            var format = config.getPrivateFormat()
                               .replace("{name}", fake.getName())
                               .replace("{message}", message);
            target.sendMessage(MiniMessage.miniMessage().deserialize(format));
            return;
        }

        var setting = ActionSetting.once();
        setting.message = message;
        this.bridge.createAction(fake, ActionType.SAY, setting).tick();
    }

    private static @NotNull String rootMessage(@NotNull Throwable error) {
        var cause = error;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }

}
