package io.github.hello09x.fakeplayer.core.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.devtools.command.exception.CommandException;
import io.github.hello09x.devtools.core.utils.Exceptions;
import io.github.hello09x.devtools.core.utils.MetadataUtils;
import io.github.hello09x.devtools.core.utils.SchedulerUtils;
import io.github.hello09x.fakeplayer.api.spi.ActionSetting;
import io.github.hello09x.fakeplayer.api.spi.ActionType;
import io.github.hello09x.fakeplayer.api.spi.NMSBridge;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.constant.MetadataKeys;
import io.github.hello09x.fakeplayer.core.entity.Fakeplayer;
import io.github.hello09x.fakeplayer.core.entity.SpawnOption;
import io.github.hello09x.fakeplayer.core.manager.feature.FakeplayerFeatureManager;
import io.github.hello09x.fakeplayer.core.manager.naming.NameManager;
import io.github.hello09x.fakeplayer.core.repository.model.Feature;
import io.github.hello09x.fakeplayer.core.util.AddressUtils;
import io.github.hello09x.fakeplayer.core.util.Reflections;
import io.github.hello09x.fakeplayer.core.util.Commands;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Singleton
public class FakeplayerManager {

    public final static String REMOVAL_REASON_PREFIX = "[fakeplayer] ";

    private final static Logger log = Main.getInstance().getLogger();

    private final NameManager nameManager;
    private final FakeplayerList playerList;
    private final FakeplayerFeatureManager featureManager;
    private final NMSBridge nms;
    private final FakeplayerConfig config;
    private final ScheduledExecutorService lagMonitor;
    private volatile boolean stopping;
    private int laglevel=0;

    @Inject
    public FakeplayerManager(NameManager nameManager, FakeplayerList playerList, FakeplayerFeatureManager featureManager, NMSBridge nms, FakeplayerConfig config) {
        this.nameManager = nameManager;
        this.playerList = playerList;
        this.featureManager = featureManager;
        this.nms = nms;
        this.config = config;

        this.lagMonitor = Executors.newSingleThreadScheduledExecutor();
        this.lagMonitor.scheduleWithFixedDelay(() -> {
                                                //Detects TPS performance from the past 1 minute only
                                                //将服务器卡顿检测范围缩小到过去一分钟，以配合新功能获得更及时的反应
                                                   if (Bukkit.getServer().getTPS()[0] < config.getKaleTps()) {
                                                       Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                                                           laglevel=min(laglevel+1,this.config.getPlayerLimit());
                                                           Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                                                           for (Player player : onlinePlayers) {
                                                               if(isFake(player))continue;
                                                               List<Player> fakeplayerlist= getAll(player);
                                                               if(fakeplayerlist.size()>this.config.getPlayerLimit()-laglevel){
                                                                   for (int i = fakeplayerlist.size() - 1; i >= this.config.getPlayerLimit()-laglevel; i--) {
                                                                       //Remove fakeplayers in reverse order of summoning
                                                                       //如果玩家召唤的假人数量超出降低后的上限，则按照反序移除假人
                                                                       remove(fakeplayerlist.get(i).getName(),"Server lag");
                                                                   }
                                                               }
                                                           }
                                                           //Lacking translation key for now
                                                           //暂时没有写翻译条目，先用英文播报
                                                           Bukkit.broadcast(Component.text("Server lag! Current fakeplayer limits: ").color(GOLD).append(Component.text(this.config.getPlayerLimit()-laglevel).color(RED)));
                                                       });
                                                   }
                                                   else {
                                                       //Restore fakeplayer limits, one at a time
                                                       //如果卡顿恢复，则每周期恢复1个假人上限
                                                       if(laglevel>0)Bukkit.broadcast(Component.text("Fakeplayer restrictions removed! Current limits: ").color(GREEN).append(Component.text(this.config.getPlayerLimit()-laglevel+1).color(AQUA)));
                                                       laglevel=max(laglevel-1,0);
                                                   }
                                               }, 0, 60, TimeUnit.SECONDS
        );
    }

    /**
     * 创建一个假人
     *
     * @param creator 创建者
     * @param spawnAt 生成地点
     */
    public @NotNull CompletableFuture<Player> spawnAsync(
            @NotNull CommandSender creator,
            @Nullable String name,
            @NotNull Location spawnAt,
            long lifespan
    ) {
        if (stopping) {
            return CompletableFuture.failedFuture(new IllegalStateException("Plugin is disabled"));
        }
        this.checkLimit(creator);

        var sn = name == null ? nameManager.getRegularName(creator) : nameManager.getSpecifiedName(name);
        log.info("UUID of fake player %s is %s".formatted(sn.name(), sn.uuid()));

        var fp = new Fakeplayer(
                creator,
                AddressUtils.getAddress(creator),
                sn,
                lifespan
        );

        var target = fp.getPlayer();    // 即使出现异常也不需要处理这个玩家, 最终会被 GC 掉
        if (!this.playerList.add(fp)) {
            this.nameManager.unregister(fp.getSequenceName());
            return CompletableFuture.failedFuture(new CommandException(
                    translatable("fakeplayer.spawn.error.name.online", text(fp.getName(), GOLD)).color(RED)
            ));
        }

        try {
            this.dispatchCommandsEarly(fp, this.config.getPreSpawnCommands());
        } catch (RuntimeException error) {
            this.playerList.remove(fp);
            this.nameManager.unregister(fp.getSequenceName());
            throw error;
        }

        var creatorId = creator instanceof Player player ? player.getUniqueId() : null;
        var future = CompletableFuture
                .supplyAsync(() -> {
                    if (stopping) {
                        throw new IllegalStateException("Plugin is disabled");
                    }
                    return featureManager.getUserConfigs(creatorId);
                })
                .thenCompose(userConfigs -> SchedulerUtils.runTask(Main.getInstance(), () -> {
                    if (stopping) {
                        throw new IllegalStateException("Plugin is disabled");
                    }
                    var configs = featureManager.getFeatures(creator, userConfigs);
                    return new SpawnOption(
                            spawnAt,
                            configs.get(Feature.invulnerable).asBoolean(),
                            configs.get(Feature.collidable).asBoolean(),
                            configs.get(Feature.look_at_entity).asBoolean(),
                            configs.get(Feature.pickup_items).asBoolean(),
                            configs.get(Feature.skin).asBoolean(),
                            configs.get(Feature.replenish).asBoolean(),
                            configs.get(Feature.autofish).asBoolean(),
                            configs.get(Feature.wolverine).asBoolean()
                    );
                }))
                .thenCompose(fp::spawnAsync)
                .thenApply(ignored -> target);

        return future.<CompletableFuture<Player>>handle((player, error) -> {
            if (error == null) {
                return CompletableFuture.completedFuture(player);
            }
            if (stopping) {
                return CompletableFuture.<Player>failedFuture(error);
            }
            try {
                return SchedulerUtils.runTask(Main.getInstance(), () -> {
                    if (target.isOnline()) {
                        target.kick(text(REMOVAL_REASON_PREFIX + "Failed to spawn"));
                    }
                    if (playerList.remove(fp)) {
                        nameManager.unregister(fp.getSequenceName());
                    }
                }).thenCompose(ignored -> CompletableFuture.failedFuture(error));
            } catch (IllegalPluginAccessException ignored) {
                // onDisable owns cleanup once Bukkit rejects new tasks.
                return CompletableFuture.<Player>failedFuture(error);
            }
        }).thenCompose(result -> result);
    }

    /**
     * 获取一个假人
     *
     * @param creator 创建者
     * @param name    假人名称
     * @return 假人
     */
    public @Nullable Player get(@NotNull CommandSender creator, @NotNull String name) {
        return Optional
                .ofNullable(this.playerList.getByName(name))
                .filter(p -> p.isCreatedBy(creator))
                .map(Fakeplayer::getPlayer)
                .orElse(null);
    }

    /**
     * 根据名称获取假人
     *
     * @param name 名称
     * @return 假人
     */
    public @Nullable Player get(@NotNull String name) {
        return Optional
                .ofNullable(this.playerList.getByName(name))
                .map(Fakeplayer::getPlayer)
                .orElse(null);
    }

    /**
     * 获取一个假人的创建者, 如果这个玩家不是假人, 则为 {@code null}
     *
     * @param target 假人
     * @return 假人的创建者
     */
    public @Nullable String getCreatorName(@NotNull Player target) {
        return Optional
                .ofNullable(this.playerList.getByUUID(target.getUniqueId()))
                .map(Fakeplayer::getCreator)
                .map(CommandSender::getName)
                .orElse(null);
    }

    /**
     * 获取假人的创建者
     *
     * @param target 假人
     * @return 创建者
     */
    public @Nullable CommandSender getCreator(@NotNull Player target) {
        return Optional.ofNullable(this.playerList.getByUUID(target.getUniqueId()))
                       .map(Fakeplayer::getCreator)
                       .map(creator -> {
                           if (creator instanceof Player p) {
                               return Bukkit.getPlayer(p.getUniqueId());
                           } else {
                               return creator;
                           }
                       })
                       .orElse(null);
    }

    /**
     * 根据名称删除假人
     *
     * @param name   名称
     * @param reason 原因
     * @return 是否删除成功
     */
    public boolean remove(@NotNull String name, @Nullable String reason) {
        return this.remove(name, reason == null ? null : text(reason));
    }

    /**
     * 根据名称删除假人
     *
     * @param name   名称
     * @param reason 原因
     * @return 是否移除成功
     */
    public boolean remove(@NotNull String name, @Nullable Component reason) {
        var target = this.get(name);
        if (target == null) {
            // 内部列表中没有, 但可能在服务器真实在线玩家列表中 (幽灵假人)。
            // 回退到真实在线玩家, 借助 SPAWNED_AT 元数据识别假人实体。
            target = this.findGhost(name);
        }
        if (target == null) {
            return false;
        }

        target.kick(textOfChildren(
                text("[fakeplayer] "),
                reason == null ? text("removed") : reason
        ));
        this.cleanup(target);
        return true;
    }

    /**
     * 在服务器真实在线玩家列表中按名称查找幽灵假人 (内部列表已丢失记录的假人实体)。
     *
     * @param name 名称
     * @return 命中的假人玩家, 不存在或不是假人则返回 {@code null}
     */
    private @Nullable Player findGhost(@NotNull String name) {
        for (var player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)
                    && MetadataKeys.getSpawnedAt(player) != null) {
                return player;
            }
        }
        return null;
    }

    /**
     * 移除所有假人
     *
     * @return 移除的假人数量
     */
    public int removeAll(@Nullable String reason) {
        // 内部列表中的假人
        var targets = new java.util.ArrayList<>(getAll());
        // 补齐服务器真实在线玩家里、内部列表丢失记录的假人 (幽灵)
        for (var player : Bukkit.getOnlinePlayers()) {
            if (MetadataKeys.getSpawnedAt(player) != null
                    && targets.stream().noneMatch(p -> p.getUniqueId().equals(player.getUniqueId()))) {
                targets.add(player);
            }
        }

        for (var target : targets) {
            target.kick(text(REMOVAL_REASON_PREFIX + (reason == null ? "removed" : reason)));
            this.cleanup(target);
        }
        return targets.size();
    }

    /**
     * @return 获取所有假人
     */
    public @NotNull List<Player> getAll() {
        return this.getAll((Predicate<Player>) null);
    }

    /**
     * @param predicate 筛选条件
     * @return 经过筛选的假人
     */
    public @NotNull List<Player> getAll(@Nullable Predicate<Player> predicate) {
        var stream = this.playerList.getAll().stream().map(Fakeplayer::getPlayer);
        if (predicate != null) {
            stream = stream.filter(predicate);
        }
        return stream.toList();
    }

    /**
     * 清理假人
     *
     * @param target 假人
     */
    public void cleanup(@NotNull Player target) {
        var fakeplayer = this.playerList.removeByUUID(target.getUniqueId());
        if (fakeplayer == null) {
            return;
        }
        this.nameManager.unregister(fakeplayer.getSequenceName());
        if (config.isDropInventoryOnQuiting()) {
            this.nms.createAction(
                    fakeplayer.getPlayer(),
                    ActionType.DROP_INVENTORY,
                    ActionSetting.once()
            ).tick();
        }
        // 真正从服务器在线玩家列表中注销该假人实体。
        // 假人通过自定义 NetworkManager 注入, Player.kick() 在 fake 网络上不会使其断线,
        // 必须调用 NMS PlayerList.remove(ServerPlayer) 才能让服务器真正移除它。
        removeFromServer(fakeplayer.getPlayer());
    }

    /**
     * 通过 NMS 的 {@code PlayerList.remove} 真正注销一名玩家实体。
     *
     * <p>兼容 Leaf 等服务端分支: {@code CraftServer#getServer()} 在 Leaf 上可能直接返回
     * {@code PlayerList} 本身 (而非 {@code MinecraftServer}), 因此优先尝试 {@code getPlayerList()}
     * 方法, 若对象自身就是 {@code PlayerList} 则直接使用。</p>
     *
     * @param player 要注销的 Bukkit 玩家
     */
    private void removeFromServer(@NotNull Player player) {
        try {
            var server = Bukkit.getServer();
            var playerList = resolvePlayerList(server);
            if (playerList == null) {
                return;
            }
            var serverPlayer = Reflections.getHandle(player);
            var spClass = serverPlayer.getClass();
            // 优先尝试 remove(ServerPlayer), 其次 remove(ServerPlayer, String)
            for (var paramTypes : new Class<?>[][]{new Class<?>[]{spClass}, new Class<?>[]{spClass, String.class}}) {
                try {
                    var method = playerList.getClass().getMethod("remove", paramTypes);
                    method.setAccessible(true);
                    if (paramTypes.length == 1) {
                        method.invoke(playerList, serverPlayer);
                    } else {
                        method.invoke(playerList, serverPlayer, "removed");
                    }
                    return;
                } catch (NoSuchMethodException ignored) {
                    // 尝试下一签名
                }
            }
            log.warning("未能找到 PlayerList.remove(" + spClass.getName() + ") 方法, 假人 "
                    + player.getName() + " 可能未被真正移除");
        } catch (Exception e) {
            log.warning("调用 PlayerList.remove 移除假人 " + player.getName() + " 失败: " + e);
        }
    }

    private static @Nullable Object resolvePlayerList(@NotNull org.bukkit.Server server) {
        try {
            var srv = Reflections.getServer(server);
            try {
                var method = srv.getClass().getMethod("getPlayerList");
                method.setAccessible(true);
                return method.invoke(srv);
            } catch (NoSuchMethodException e) {
                // Leaf 等分支上 getServer() 已直接返回 PlayerList
                return srv;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取创建者创建的所有假人
     *
     * @param creator 创建者
     * @return 创建者创建的假人
     */
    public @NotNull List<Player> getAll(@NotNull CommandSender creator) {
        return this.getAll(creator, null);
    }

    /**
     * 获取筛选过的创建者创建的假人
     *
     * @param creator   创建者
     * @param predicate 筛选条件
     * @return 假人
     */
    public @NotNull List<Player> getAll(@NotNull CommandSender creator, @Nullable Predicate<Player> predicate) {
        var stream = this.playerList.getByCreator(creator.getName()).stream().map(Fakeplayer::getPlayer);
        if (predicate != null) {
            stream = stream.filter(predicate);
        }
        return stream.toList();
    }

    public int getSize() {
        return this.playerList.getSize();
    }

    /**
     * 判断一名玩家是否是假人
     *
     * @param target 玩家
     * @return 是否是假人
     */
    public boolean isFake(@NotNull Player target) {
        return this.playerList.getByUUID(target.getUniqueId()) != null;
    }

    /**
     * 判断一名玩家不是假人
     *
     * @param target 玩家
     * @return 是否不是假人
     */
    public boolean isNotFake(@NotNull Player target) {
        return this.playerList.getByUUID(target.getUniqueId()) == null;
    }

    /**
     * 获取 IP 地址创建着多少个假人
     *
     * @param address IP 地址
     * @return 该 IP 地址创建着多少个假人
     */
    public long countByAddress(@NotNull String address) {
        return this.playerList
                .stream()
                .filter(p -> p.getCreatorIp().equals(address))
                .count();
    }

    /**
     * 获取这个玩家创建了多少个假人
     *
     * @param creator 玩家
     * @return 创建了多少个假人
     */
    public int countByCreator(@NotNull CommandSender creator) {
        return this.playerList.countByCreator(creator.getName());
    }

    /**
     * 设置玩家当前选择的假人
     *
     * @param creator 玩家
     * @param target  假人
     */
    public void setSelection(@NotNull Player creator, @Nullable Player target) {
        if (target == null) {
            creator.removeMetadata(MetadataKeys.SELECTION, Main.getInstance());
            return;
        }

        if (!this.isFake(target)) {
            return;
        }

        creator.setMetadata(MetadataKeys.SELECTION, new FixedMetadataValue(Main.getInstance(), target.getUniqueId()));
    }

    /**
     * 获取当前选中的假人
     *
     * @param creator 创建者
     * @return 选中的假人
     */
    public @Nullable Player getSelection(@NotNull CommandSender creator) {
        if (!(creator instanceof Player p)) {
            return null;
        }
        if (!p.hasMetadata(MetadataKeys.SELECTION)) {
            return null;
        }

        var uuid = MetadataUtils
                .find(Main.getInstance(), p, MetadataKeys.SELECTION, UUID.class)
                .map(MetadataValue::value)
                .map(UUID.class::cast)
                .orElse(null);

        if (uuid == null) {
            return null;
        }

        var target = Optional.ofNullable(this.playerList.getByUUID(uuid)).map(Fakeplayer::getPlayer).orElse(null);
        if (target == null) {
            this.setSelection(p, null);
        }
        return target;
    }

    /**
     * 以假人身份执行命令
     *
     * @param target   假人
     * @param commands 命令
     */
    public void issueCommands(@NotNull Player target, @NotNull List<String> commands) {
        if (commands.isEmpty()) {
            return;
        }
        if (this.isNotFake(target)) {
            return;
        }

        var p = target.getName();
        var u = target.getUniqueId().toString();
        var c = Objects.requireNonNull(this.getCreatorName(target));
        for (var cmd : Commands.formatCommands(commands, "%p", p, "%u", u, "%c", c)) {
            if (!target.performCommand(cmd)) {
                log.warning(target.getName() + " failed to execute command: " + cmd);
            } else {
                log.info(target.getName() + " issued command: " + cmd);
            }
        }
    }

    public void dispatchCommandsEarly(@NotNull Fakeplayer fp, @NotNull List<String> commands) {
        if (commands.isEmpty()) {
            return;
        }

        var server = Bukkit.getServer();
        var sender = Bukkit.getConsoleSender();
        var p = fp.getName();
        var u = fp.getUUID().toString();
        var c = fp.getCreator().getName();
        for (var cmd : Commands.formatCommands(commands, "%p", p, "%u", u, "%c", c)) {
            if (!server.dispatchCommand(sender, cmd)) {
                log.warning("Failed to execute command for %s: ".formatted(p) + cmd);
            } else {
                log.info("Dispatched command: " + cmd);
            }
        }
    }

    /**
     * 以控制台身份对玩家执行命令
     *
     * @param args   参数
     * @param commands 命令
     */
    public void dispatchCommands(@NotNull DispatchCommandArgs args, @NotNull List<String> commands) {
        if (commands.isEmpty()) {
            return;
        }

        var server = Bukkit.getServer();
        var sender = Bukkit.getConsoleSender();

        var p = args.fakeplayerName;
        var u = args.fakeplayerUUID;
        var c = args.creatorName;
        for (var cmd : Commands.formatCommands(commands, "%p", p, "%u", u, "%c", c)) {
            if (!server.dispatchCommand(sender, cmd)) {
                log.warning("Failed to execute command for %s: ".formatted(p) + cmd);
            } else {
                log.info("Dispatched command: " + cmd);
            }
        }
    }

    /**
     * 以控制台身份对玩家执行命令
     *
     * @param player   假人
     * @param commands 命令
     */
    public void dispatchCommands(@NotNull Player player, @NotNull List<String> commands) {
        this.dispatchCommands(new DispatchCommandArgs(player.getName(),player.getUniqueId().toString(),Objects.requireNonNull(this.getCreatorName(player))),commands);
    }

    @AllArgsConstructor
    public static class DispatchCommandArgs {
        public String fakeplayerName , fakeplayerUUID, creatorName;
    }

    /**
     * 检测限制, 不满足条件则抛出异常
     *
     * @param creator 创建者
     */
    private void checkLimit(@NotNull CommandSender creator) throws CommandException {
        if (creator.isOp()) {
            return;
        }

        if (this.playerList.getSize() >= this.config.getServerLimit()) {
            throw new CommandException(translatable("fakeplayer.command.spawn.error.server-limit"));
        }

        //Apply dynamic limits to fakeplayers
        //对玩家创建假人上限判断应用新的计算规则
        if (this.playerList.getByCreator(creator.getName()).size() >= this.config.getPlayerLimit()-laglevel) {
            throw new CommandException(translatable("fakeplayer.command.spawn.error.player-limit"));
        }

        if (this.config.isDetectIp() && this.countByAddress(AddressUtils.getAddress(creator)) >= this.config.getPlayerLimit()-laglevel) {
            throw new CommandException(translatable("fakeplayer.command.spawn.error.ip-limit"));
        }
    }

    public void onDisable() {
        this.stopping = true;
        Exceptions.suppress(Main.getInstance(), () -> this.removeAll("Plugin disabled"));
        Exceptions.suppress(Main.getInstance(), () -> this.playerList.getAll().forEach(fakeplayer -> {
            if (this.playerList.remove(fakeplayer)) {
                this.nameManager.unregister(fakeplayer.getSequenceName());
            }
        }));
        Exceptions.suppress(Main.getInstance(), () -> Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.getMetadata(MetadataKeys.SPAWNED_AT).stream()
                        .anyMatch(value -> value.getOwningPlugin() == Main.getInstance()))
                .forEach(player -> player.kick(text(REMOVAL_REASON_PREFIX + "Plugin disabled"))));
        Exceptions.suppress(Main.getInstance(), this.lagMonitor::shutdownNow);
    }

}
