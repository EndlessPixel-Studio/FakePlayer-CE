package io.github.hello09x.fakeplayer.core.config;


import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.devtools.core.config.ConfigUtils;
import io.github.hello09x.devtools.core.config.PluginConfig;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import io.github.hello09x.fakeplayer.core.repository.model.Feature;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.translatable;

@Getter
@ToString
@Singleton
public class FakeplayerConfig extends PluginConfig {

    private final static Logger log = Main.getInstance().getLogger();

    private final static String defaultNameChars = "^[a-zA-Z0-9_]+$";

    /**
     * 每位玩家最多多少个假人
     */
    private int playerLimit;

    /**
     * 服务器最多多少个假人
     */
    private int serverLimit;

    /**
     * 命名模版
     */
    private String nameTemplate;

    /**
     * 假人名称前缀
     */
    private String namePrefix;

    /**
     * 名称样式, 颜色
     */
    private NamedTextColor nameStyleColor;

    /**
     * 名称样式, 格式
     */
    private List<TextDecoration> nameStyleDecorations;

    /**
     * 创建者玩家下线时是否跟随下线
     */
    private boolean followQuiting;

    /**
     * 创建者玩家下线时是否<strong>立即</strong>清理其假人
     * <p>默认的跟随下线是基于定时轮询, 开启后会在玩家退出时立刻就清理</p>
     */
    private boolean followQuitingForce;

    /**
     * 创建者玩家下线后延迟多少秒执行立即清理
     */
    private int followQuitingForceDelay;

    /**
     * 是否探测 IP
     */
    private boolean detectIp;

    /**
     * 服务器 tps 低于这个值移除所有假人
     */
    private int kaleTps;

    /**
     * 创建前执行命令
     */
    private List<String> preSpawnCommands;

    /**
     * 创建时执行命令
     */
    private List<String> postSpawnCommands;

    /**
     * 创建后执行命令
     */
    private List<String> afterSpawnCommands;

    /**
     * 退出前执行命令
     */
    private List<String> postQuitCommands;

    /**
     * 退出后命令
     */
    private List<String> afterQuitCommands;

    /**
     * 自执行命令
     */
    private List<String> selfCommands;

    /**
     * 退出时是否丢弃背包物品
     */
    private boolean dropInventoryOnQuiting;

    /**
     * 是否保存假人存档
     */
    private boolean persistData;

    /**
     * 死亡时是否踢出游戏
     */
    private boolean kickOnDead;

    /**
     * 是否在服务器启动后自动恢复上次在线的假人
     */
    private boolean restoreOnStart;

    /**
     * 自定义名称规则
     */
    private Pattern namePattern;

    /**
     * 检测更新
     */
    private boolean checkForUpdates;

    /**
     * 允许执行的命令
     */
    @Deprecated
    private Set<String> allowCommands;

    /**
     * 默认假人存活时间
     */
    @Nullable
    private Duration lifespan;

    /**
     * 开发者调试模式
     */
    private boolean debug;

    /**
     * 防止踢出
     */
    private PreventKicking preventKicking;

    /**
     * invsee 实现方式
     */
    private InvseeImplement invseeImplement;

    /**
     * 允许非 OP 玩家打开假人背包
     */
    private boolean allowNonOpOpenInv;

    /**
     * 真实皮肤
     */
    @Beta
    private boolean defaultOnlineSkin;

    private Map<Feature, String> defaultFeatures;

    /**
     * 自动补货时工具磨损到多少百分比自动修复
     * <p>取值 0-100, 0 表示不自动修复工具</p>
     */
    private int replenishToolsDurabilityThreshold;

    /**
     * 主手工具剩余耐久低于该值时尝试从背包替换
     */
    private int toolReplacementRemainingDurabilityThreshold;

    /**
     * 假人生成后是否自动登录
     */
    private boolean autoLogin;

    /**
     * 自动注册命令模板, %password% 会被替换为数据库中存储的密码
     */
    private String registerCommand;

    /**
     * 自动登录命令模板, %password% 会被替换为数据库中存储的密码
     */
    private String loginCommand;

    /**
     * 随机密码字符集, 为空表示不自动生成密码
     */
    private String randomPasswordCharset;

    /**
     * 随机密码长度
     */
    private int randomPasswordLength;

    /**
     * 修改密码命令模板, %oldpassword% 与 %newpassword% 分别替换为旧密码和新密码
     */
    private String changePasswordCommand;

    /**
     * 是否忽略版本检查强制在不支持的 Minecraft 版本上运行
     * <p>开启后会以第一个可用的 NMSBridge 实现兜底, 可能因 NMS 不兼容而崩溃, 风险自负</p>
     */
    private boolean forcedExecution;

    /**
     * 是否启用 HTTP 远程管理接口
     */
    private boolean httpAdminEnabled;

    /**
     * HTTP 服务监听地址
     */
    private String httpAdminHost;

    /**
     * HTTP 服务监听端口
     */
    private int httpAdminPort;

    /**
     * HTTP 接口鉴权令牌, 为空时启动时随机生成
     */
    private String httpAdminToken;

    /**
     * 是否启用 list 接口
     */
    private boolean httpAdminList;

    /**
     * 是否启用 spawn 接口
     */
    private boolean httpAdminSpawn;

    /**
     * 是否启用 kick 接口
     */
    private boolean httpAdminKick;

    /**
     * 是否启用 kill 接口
     */
    private boolean httpAdminKill;

    /**
     * 是否启用 say 接口
     */
    private boolean httpAdminSay;

    /**
     * 是否启用 status 接口
     */
    private boolean httpAdminStatus;

    /**
     * 是否启用 info 接口
     */
    private boolean httpAdminInfo;

    /**
     * 是否启用 action 接口
     */
    private boolean httpAdminAction;

    /**
     * 是否启用 stop 接口
     */
    private boolean httpAdminStop;

    /**
     * 是否启用 teleport 接口
     */
    private boolean httpAdminTeleport;

    /**
     * 是否启用 look 接口
     */
    private boolean httpAdminLook;

    /**
     * 是否启用 hold 接口
     */
    private boolean httpAdminHold;

    /**
     * 是否启用 swap 接口
     */
    private boolean httpAdminSwap;

    /**
     * 是否启用 respawn 接口
     */
    private boolean httpAdminRespawn;

    /**
     * 是否启用 cmd 接口
     */
    private boolean httpAdminCmd;

    /**
     * 是否启用批量接口 (kickall / killall / sayall)
     */
    private boolean httpAdminBatch;

    /**
     * 是否允许 GET 请求。关闭后仅接受 POST
     */
    private boolean httpAdminAllowGet;

    /**
     * Host / Origin 白名单, 为空时不校验。用于缓解 DNS rebinding
     */
    private List<String> httpAdminAllowedHosts;

    /**
     * 每个来源 IP 每分钟允许的请求数
     */
    private int httpAdminRequestsPerMinute;

    /**
     * 连续鉴权失败多少次后锁定来源 IP, 0 表示不锁定
     */
    private int httpAdminAuthFailures;

    /**
     * 鉴权失败锁定时长 (秒)
     */
    private int httpAdminLockoutSeconds;

    @Inject
    public FakeplayerConfig() {
        super(Main.getInstance());
    }

    private static int maxIfZero(int value) {
        return value <= 0 ? Integer.MAX_VALUE : value;
    }

    @Override
    protected void reload(@NotNull FileConfiguration file) {
        this.playerLimit = maxIfZero(file.getInt("player-limit", 1));
        this.serverLimit = maxIfZero(file.getInt("server-limit", 1000));
        this.followQuiting = file.getBoolean("follow-quiting", true);
        this.followQuitingForce = file.getBoolean("follow-quiting-force", false);
        this.followQuitingForceDelay = file.getInt("follow-quiting-force-delay", 3);
        this.detectIp = file.getBoolean("detect-ip", false);
        this.kaleTps = file.getInt("kale-tps", 0);
        this.selfCommands = file.getStringList("self-commands");
        this.preSpawnCommands = file.getStringList("pre-spawn-commands");
        this.postSpawnCommands = file.getStringList("post-spawn-commands");
        this.afterSpawnCommands = file.getStringList("after-spawn-commands");
        this.postQuitCommands = file.getStringList("post-quit-commands");
        this.afterQuitCommands = file.getStringList("after-quit-commands");
        this.nameTemplate = file.getString("name-template", "");
        this.dropInventoryOnQuiting = file.getBoolean("drop-inventory-on-quiting", true);
        this.persistData = file.getBoolean("persist-data", true);
        this.kickOnDead = file.getBoolean("kick-on-dead", true);
        this.restoreOnStart = file.getBoolean("restore-on-start", false);
        this.checkForUpdates = file.getBoolean("check-for-updates", true);
        this.namePattern = getNamePattern(file);
        this.preventKicking = this.getPreventKicking(file);
        this.nameTemplate = getNameTemplate(file);
        this.namePrefix = file.getString("name-prefix", "");
        this.lifespan = getLifespan(file);
        this.allowCommands = file.getStringList("allow-commands")
                                 .stream()
                                 .map(c -> c.startsWith("/") ? c.substring(1) : c)
                                 .filter(c -> !c.isBlank())
                                 .collect(Collectors.toSet());

        this.defaultOnlineSkin = file.getBoolean("default-online-skin", false);
        this.defaultFeatures = Arrays.stream(Feature.values())
                                     .collect(Collectors.toMap(Function.identity(), key -> file.getString("default-features." + key.name(), key.getDefaultOption())));
        this.invseeImplement = ConfigUtils.getEnum(file, "invsee-implement", InvseeImplement.class, InvseeImplement.AUTO);
        this.allowNonOpOpenInv = file.getBoolean("allow-non-op-open-inv", false);
        this.debug = file.getBoolean("debug", false);
        this.replenishToolsDurabilityThreshold = file.getInt("replenish.tools.durability-threshold", 0);
        this.toolReplacementRemainingDurabilityThreshold = Math.max(0, file.getInt("tool-replacement.remaining-durability-threshold", 10));
        this.autoLogin = file.getBoolean("auto-login", false);
        this.registerCommand = file.getString("register", "/reg %password% %password%");
        this.loginCommand = file.getString("login", "/login %password%");
        this.randomPasswordCharset = file.getString("random-password", "");
        var randomLength = file.getInt("random-password-length", 12);
        this.randomPasswordLength = randomLength <= 0 ? 12 : randomLength;
        this.changePasswordCommand = file.getString("change-password", "/changepassword %oldpassword% %newpassword% %newpassword%");
        this.forcedExecution = file.getBoolean("forced-execution", false);

        this.httpAdminEnabled = file.getBoolean("http-admin.enabled", false);
        this.httpAdminHost = file.getString("http-admin.host", "0.0.0.0");
        this.httpAdminPort = file.getInt("http-admin.port", 3253);
        this.httpAdminToken = file.getString("http-admin.token", "");
        this.httpAdminList = file.getBoolean("http-admin.interface.list", true);
        this.httpAdminSpawn = file.getBoolean("http-admin.interface.spawn", true);
        this.httpAdminKick = file.getBoolean("http-admin.interface.kick", true);
        this.httpAdminKill = file.getBoolean("http-admin.interface.kill", true);
        this.httpAdminSay = file.getBoolean("http-admin.interface.say", true);
        this.httpAdminStatus = file.getBoolean("http-admin.interface.status", true);
        this.httpAdminInfo = file.getBoolean("http-admin.interface.info", true);
        this.httpAdminAction = file.getBoolean("http-admin.interface.action", true);
        this.httpAdminStop = file.getBoolean("http-admin.interface.stop", true);
        this.httpAdminTeleport = file.getBoolean("http-admin.interface.teleport", true);
        this.httpAdminLook = file.getBoolean("http-admin.interface.look", true);
        this.httpAdminHold = file.getBoolean("http-admin.interface.hold", true);
        this.httpAdminSwap = file.getBoolean("http-admin.interface.swap", true);
        this.httpAdminRespawn = file.getBoolean("http-admin.interface.respawn", true);
        this.httpAdminCmd = file.getBoolean("http-admin.interface.cmd", true);
        this.httpAdminBatch = file.getBoolean("http-admin.interface.batch", true);
        this.httpAdminAllowGet = file.getBoolean("http-admin.allow-get", true);
        this.httpAdminAllowedHosts = file.getStringList("http-admin.allowed-hosts")
                                         .stream()
                                         .filter(host -> host != null && !host.isBlank())
                                         .toList();
        this.httpAdminRequestsPerMinute = Math.max(1, file.getInt("http-admin.rate-limit.requests-per-minute", 120));
        this.httpAdminAuthFailures = Math.max(0, file.getInt("http-admin.rate-limit.auth-failures", 10));
        this.httpAdminLockoutSeconds = Math.max(1, file.getInt("http-admin.rate-limit.lockout-seconds", 60));
        this.nameStyleColor = this.getNameStyleColor(file);
        this.nameStyleDecorations = this.getNameStyleDecorations(file);

        if (this.isConfigFileOutOfDate()) {
            Schedulers.globalLater(Main.getInstance(), 1, () -> {
                if (Main.getInstance().isEnabled()) {
                    Main.getInstance().getComponentLogger().warn(translatable("fakeplayer.configuration.out-of-date"));
                }
            });
        }

        if (!this.allowCommands.isEmpty()) {
            log.warning("allow-commands is deprecated which will be removed at 0.4.0, you should use Permissions Plugin to assign permission groups to fake players.");
        }

        var preparingCommands = file.getStringList("preparing-commands");
        if (!preparingCommands.isEmpty()) {
            log.warning("preparing-commands is deprecated, use post-spawn-commands instead.");
            this.postSpawnCommands.addAll(preparingCommands);
        }

        var destroyCommands = file.getStringList("destroy-commands");
        if (!destroyCommands.isEmpty()) {
            log.warning("destroy-commands is deprecated, use post-quit-commands instead.");
            this.postQuitCommands.addAll(destroyCommands);
        }

    }

    private @Nullable Duration getLifespan(@NotNull FileConfiguration file) {
        var minutes = file.getLong("lifespan");
        if (minutes <= 0) {
            return null;
        }
        return Duration.ofMinutes(minutes);
    }


    private @NotNull Pattern getNamePattern(@NotNull FileConfiguration file) {
        try {
            return Pattern.compile(file.getString("name-pattern", defaultNameChars));
        } catch (PatternSyntaxException e) {
            log.warning("Invalid name-pattern: " + file.getString("name-chars"));
            return Pattern.compile(defaultNameChars);
        }
    }

    private @NotNull String getNameTemplate(@NotNull FileConfiguration file) {
        var tmpl = file.getString("name-template", "");
        if (tmpl.startsWith("-") || tmpl.startsWith("@")) {
            log.warning("Invalid name template: " + this.nameTemplate);
            return "";
        }
        return tmpl;
    }

    private @NotNull PreventKicking getPreventKicking(@NotNull FileConfiguration file) {
        if (file.getBoolean("prevent-kicked-on-spawning", false)) {
            log.warning("prevent-kicked-on-spawning is deprecated which will be removed at 0.4.0, use prevent-kick instead");
            return PreventKicking.ON_SPAWNING;
        }

        return ConfigUtils.getEnum(file, "prevent-kicking", PreventKicking.class, PreventKicking.ON_SPAWNING);
    }

    private @NotNull NamedTextColor getNameStyleColor(@NotNull FileConfiguration file) {
        var styles = Objects.requireNonNullElse(file.getString("name-style"), "").split(",\\s*");
        var color = NamedTextColor.WHITE;
        for (var style : styles) {
            var c = NamedTextColor.NAMES.value(style);
            if (c != null) {
                color = c;
            }
        }
        return color;
    }

    private @NotNull List<TextDecoration> getNameStyleDecorations(@NotNull FileConfiguration file) {
        var styles = Objects.requireNonNullElse(file.getString("name-style"), "").split(",\\s*");
        var decorations = new ArrayList<TextDecoration>();
        for (var style : styles) {
            var decoration = TextDecoration.NAMES.value(style);
            if (decoration != null) {
                decorations.add(decoration);
            }
        }
        return decorations;
    }

}
