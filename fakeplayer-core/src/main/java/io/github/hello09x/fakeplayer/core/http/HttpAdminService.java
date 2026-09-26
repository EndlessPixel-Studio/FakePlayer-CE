package io.github.hello09x.fakeplayer.core.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.hello09x.fakeplayer.api.spi.ActionSetting;
import io.github.hello09x.fakeplayer.api.spi.ActionType;
import io.github.hello09x.fakeplayer.api.spi.NMSBridge;
import io.github.hello09x.fakeplayer.api.spi.NMSServerPlayer;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.constant.MetadataKeys;
import io.github.hello09x.fakeplayer.core.entity.FakeplayerTicker;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.manager.action.ActionManager;
import io.github.hello09x.fakeplayer.core.util.LookUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 通过 HTTP 接口远程管理假人。
 * <p>接口 (均要求 GET):
 * <ul>
 *     <li>GET /list     -> {"fakeplayer":["name",...]}</li>
 *     <li>GET /status?name=xxx -> {"fakeplayer":[{...}]}, 省略 name 时返回全部假人</li>
 *     <li>GET /info     -> {"plugin":...,"minecraft":...,"fakeplayer":...}</li>
 *     <li>GET /spawn?name=xxx -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /kick?name=xxx  -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /kill?name=xxx  -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /respawn?name=xxx -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /say?name=xxx&amp;message=yyy -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /action?name=xxx&amp;action=yyy[&amp;count=&amp;interval=&amp;wait=&amp;message=] -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /stop?name=xxx -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /teleport?name=xxx[&amp;world=&amp;x=&amp;y=&amp;z=&amp;yaw=&amp;pitch=] -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /look?name=xxx[&amp;direction=|&amp;at=x,y,z|&amp;yaw=&amp;pitch=] -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /hold?name=xxx&amp;slot=1-9 -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /swap?name=xxx -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /cmd?name=xxx&amp;command=yyy -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /kickall -> {"status":"success","count":n}</li>
 *     <li>GET /killall -> {"status":"success","count":n}</li>
 *     <li>GET /sayall?message=yyy -> {"status":"success","count":n}</li>
 * </ul>
 * 所有接口都需要通过 {@code Authorization: Bearer <token>} 请求头携带令牌 (不支持 ?token=),
 * token 为空时启动时随机生成。每个接口可通过 config.yml 中的 http-admin.interface.&lt;名字&gt; 单独关闭。
 */
@Singleton
public class HttpAdminService {

    private final static Logger log = Main.getInstance().getLogger();

    private final static List<String> LOOK_DIRECTIONS = List.of("NORTH", "SOUTH", "EAST", "WEST", "UP", "DOWN");

    /**
     * 日志中路径与 query 的最大长度, 超出部分截断, 避免被超长请求撑爆日志
     */
    private final static int MAX_LOG_LENGTH = 512;

    /**
     * 请求 URI 的最大长度, 超出直接拒绝, 避免解析超长 query 消耗资源
     */
    private final static int MAX_REQUEST_URI_LENGTH = 4096;

    /**
     * 被跟踪的客户端数量上限, 防止限流状态无界增长
     */
    private final static int MAX_TRACKED_CLIENTS = 1024;

    /**
     * 处理线程数 / 排队上限, 队列满时由调用线程直接执行形成反压
     */
    private final static int WORKER_THREADS = 4;

    private final static int WORKER_QUEUE_SIZE = 64;

    /**
     * 一个接口: 精确路径 + 处理逻辑
     */
    @FunctionalInterface
    private interface Endpoint {
        void handle(HttpExchange ex) throws IOException;
    }

    private final FakeplayerManager manager;
    private final FakeplayerConfig config;
    private final ActionManager actionManager;
    private final NMSBridge bridge;

    /**
     * 精确路径 -> 处理逻辑。使用精确匹配而非 createContext 的前缀匹配,
     * 避免 /listfoo 之类路径被 /list 命中, 绕过外部基于精确路径的 ACL / WAF
     */
    private final Map<String, Endpoint> routes = new LinkedHashMap<>();

    /**
     * 按来源 IP 记录的限流与失败计数状态
     */
    private final Map<String, ClientState> clients = new ConcurrentHashMap<>();

    private HttpServer server;
    private ExecutorService executor;
    private String token;

    @Inject
    public HttpAdminService(FakeplayerManager manager, FakeplayerConfig config, ActionManager actionManager, NMSBridge bridge) {
        this.manager = manager;
        this.config = config;
        this.actionManager = actionManager;
        this.bridge = bridge;
    }

    public void start() {
        if (!config.isHttpAdminEnabled()) {
            return;
        }
        var token = config.getHttpAdminToken();
        if (token == null || token.isBlank()) {
            token = UUID.randomUUID().toString().replace("-", "");
            log.warning("http-admin.token is empty, an auto-generated token will be used: " + token);
        }
        this.token = token;

        try {
            server = HttpServer.create(new InetSocketAddress(config.getHttpAdminHost(), config.getHttpAdminPort()), 0);
        } catch (IOException e) {
            log.severe("Failed to start HTTP admin server: " + e);
            return;
        }

        routes.clear();
        routes.put("/list", this::handleList);
        routes.put("/status", this::handleStatus);
        routes.put("/info", this::handleInfo);
        routes.put("/spawn", this::handleSpawn);
        routes.put("/kick", this::handleKick);
        routes.put("/kill", this::handleKill);
        routes.put("/respawn", this::handleRespawn);
        routes.put("/say", this::handleSay);
        routes.put("/action", this::handleAction);
        routes.put("/stop", this::handleStop);
        routes.put("/teleport", this::handleTeleport);
        routes.put("/look", this::handleLook);
        routes.put("/hold", this::handleHold);
        routes.put("/swap", this::handleSwap);
        routes.put("/cmd", this::handleCmd);
        routes.put("/kickall", this::handleKickAll);
        routes.put("/killall", this::handleKillAll);
        routes.put("/sayall", this::handleSayAll);

        // 所有请求统一走 "/" 上下文, 再按精确路径分发 (见 routes 的说明)
        server.createContext("/", this::handleRequest);
        // 有界队列 + 反压: 队列满时由 dispatcher 线程直接执行, 避免任务无限堆积吃内存;
        // 线程设为 daemon, 避免影响服务端退出
        this.executor = new ThreadPoolExecutor(
                WORKER_THREADS,
                WORKER_THREADS,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(WORKER_QUEUE_SIZE),
                r -> {
                    var thread = new Thread(r, "fakeplayer-http");
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        server.setExecutor(this.executor);
        server.start();
        log.info("HTTP admin server started on " + config.getHttpAdminHost() + ":" + config.getHttpAdminPort());
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            log.info("HTTP admin server stopped");
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        clients.clear();
    }

    /**
     * 统一入口: 精确路由 -&gt; 限流 -&gt; 主机/同源校验 -&gt; 方法校验 -&gt; 鉴权 -&gt; 交给具体接口。
     *
     * <p>鉴权刻意排在接口开关之前: 未授权的调用方一律得到 401,
     * 无法通过 401 / 403 的差异枚举出哪些接口被启用。</p>
     */
    private void handleRequest(HttpExchange ex) throws IOException {
        var endpoint = routes.get(ex.getRequestURI().getPath());
        if (endpoint == null) {
            sendJsonNoQuery(ex, 404, "failure", "Not found");
            return;
        }
        if (ex.getRequestURI().toString().length() > MAX_REQUEST_URI_LENGTH) {
            sendJsonNoQuery(ex, 414, "failure", "Request URI too long");
            return;
        }

        var ip = remoteAddress(ex);
        var state = client(ip);
        var now = System.currentTimeMillis();
        if (state.isLocked(now)) {
            sendJsonNoQuery(ex, 429, "failure", "Too many failed attempts, try again later");
            return;
        }
        if (!state.allowRequest(now, config.getHttpAdminRequestsPerMinute())) {
            sendJsonNoQuery(ex, 429, "failure", "Too many requests");
            return;
        }
        if (!checkHost(ex) || !checkOrigin(ex)) {
            sendJsonNoQuery(ex, 403, "failure", "Host not allowed");
            return;
        }
        if (!isMethodAllowed(ex)) {
            sendJsonNoQuery(ex, 405, "failure", "Method not allowed");
            return;
        }
        if (!authorize(ex)) {
            if (state.recordFailure(now, config.getHttpAdminAuthFailures(), config.getHttpAdminLockoutSeconds() * 1000L)) {
                log.warning("HTTP admin: too many failed attempts from " + ip
                        + ", locked for " + config.getHttpAdminLockoutSeconds() + "s");
            }
            sendUnauthorized(ex);
            return;
        }
        state.recordSuccess();
        endpoint.handle(ex);
    }

    private static String remoteAddress(HttpExchange ex) {
        var remote = ex.getRemoteAddress();
        return remote == null ? "unknown" : remote.getAddress().getHostAddress();
    }

    private ClientState client(String ip) {
        var state = clients.get(ip);
        if (state != null) {
            return state;
        }
        if (clients.size() >= MAX_TRACKED_CLIENTS) {
            var now = System.currentTimeMillis();
            clients.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        }
        return clients.computeIfAbsent(ip, key -> new ClientState());
    }

    /**
     * Host 校验: 配置了 {@code http-admin.allowed-hosts} 时, Host 必须命中白名单。
     * <p>用于缓解 DNS rebinding —— 恶意网页把自己的域名解析到本机时, Host 会是那个域名。</p>
     */
    private boolean checkHost(HttpExchange ex) {
        var allowed = config.getHttpAdminAllowedHosts();
        if (allowed == null || allowed.isEmpty()) {
            return true;
        }
        var host = ex.getRequestHeaders().getFirst("Host");
        return host != null && isAllowedHost(allowed, host);
    }

    /**
     * 同源校验: 浏览器发起的跨源请求一定带 Origin, 其主机必须与 Host 相同或在白名单内。
     * <p>这样即使恶意网页能连到本机端口, 也无法驱动接口执行操作。</p>
     */
    private boolean checkOrigin(HttpExchange ex) {
        var origin = ex.getRequestHeaders().getFirst("Origin");
        if (origin == null || origin.isBlank()) {
            return true;
        }
        var host = ex.getRequestHeaders().getFirst("Host");
        if (host == null) {
            return false;
        }
        var originHost = hostOf(origin);
        if (originHost == null) {
            return false;
        }
        if (originHost.equals(hostOf(host))) {
            return true;
        }
        var allowed = config.getHttpAdminAllowedHosts();
        return allowed != null && isAllowedHost(allowed, originHost);
    }

    /**
     * GET 允许的前提是 {@code http-admin.allow-get} 为 true (默认), POST 始终允许。
     * <p>令牌统一走请求头后, GET 已不会被浏览器预取或爬虫意外触发,
     * 对公网可达的实例仍建议关闭 GET (所有接口都有副作用)。</p>
     */
    private boolean isMethodAllowed(HttpExchange ex) {
        var method = ex.getRequestMethod();
        if ("POST".equalsIgnoreCase(method)) {
            return true;
        }
        return "GET".equalsIgnoreCase(method) && config.isHttpAdminAllowGet();
    }

    private static boolean isAllowedHost(List<String> allowed, String host) {
        var hostName = hostOf(host);
        if (hostName == null) {
            return false;
        }
        var rawHost = host.trim().toLowerCase(Locale.ROOT);
        for (var entry : allowed) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            var value = entry.trim().toLowerCase(Locale.ROOT);
            if (value.equals(rawHost) || value.equals(hostName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从 {@code scheme://host[:port]} 或 {@code host[:port]} 中取出小写主机名 (去掉端口)
     */
    private static @Nullable String hostOf(@Nullable String value) {
        if (value == null) {
            return null;
        }
        var host = value.trim();
        var schemeEnd = host.indexOf("://");
        if (schemeEnd >= 0) {
            host = host.substring(schemeEnd + 3);
        }
        var end = host.length();
        for (int i = 0; i < host.length(); i++) {
            var c = host.charAt(i);
            if (c == '/' || c == '?' || c == '#') {
                end = i;
                break;
            }
        }
        host = host.substring(0, end);
        if (host.isBlank()) {
            return null;
        }
        if (host.startsWith("[")) {
            var close = host.indexOf(']');
            return (close < 0 ? host : host.substring(0, close + 1)).toLowerCase(Locale.ROOT);
        }
        var colon = host.indexOf(':');
        return (colon < 0 ? host : host.substring(0, colon)).toLowerCase(Locale.ROOT);
    }

    /**
     * 单个来源 IP 的限流与鉴权失败状态
     */
    private static final class ClientState {

        /**
         * 最近一分钟内的请求时间戳
         */
        private final ArrayDeque<Long> requests = new ArrayDeque<>();

        private int failures;

        private long lastFailureAt;

        private long lockedUntil;

        synchronized boolean isLocked(long now) {
            return now < lockedUntil;
        }

        synchronized boolean isExpired(long now) {
            return requests.isEmpty()
                    && failures == 0
                    && now - lastFailureAt > 300_000L
                    && now >= lockedUntil;
        }

        synchronized boolean allowRequest(long now, int maxPerMinute) {
            var cutoff = now - 60_000L;
            while (!requests.isEmpty() && requests.peekFirst() < cutoff) {
                requests.pollFirst();
            }
            if (requests.size() >= maxPerMinute) {
                return false;
            }
            requests.addLast(now);
            return true;
        }

        /**
         * @return 本次失败是否触发了锁定
         */
        synchronized boolean recordFailure(long now, int maxFailures, long lockMillis) {
            if (maxFailures <= 0) {
                return false;
            }
            if (now - lastFailureAt > 60_000L) {
                failures = 0;
            }
            lastFailureAt = now;
            if (++failures < maxFailures) {
                return false;
            }
            failures = 0;
            lockedUntil = now + lockMillis;
            return true;
        }

        synchronized void recordSuccess() {
            failures = 0;
        }
    }

    private void handleList(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminList())) {
            return;
        }
        var names = manager.getAll().stream().map(Player::getName).collect(Collectors.toList());
        logCall(ex, 200, "success", names.size() + " fake player(s)");
        send(ex, 200, "application/json", "{\"fakeplayer\":" + toJsonArray(names) + "}");
    }

    private void handleSpawn(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminSpawn())) {
            return;
        }
        var name = query(ex, "name");
        if (name == null || name.isBlank()) {
            sendJson(ex, 400, "failure", "Name is required");
            return;
        }

        // 与真实玩家重名
        var online = Bukkit.getPlayerExact(name);
        if (online != null && manager.get(name) == null) {
            sendJson(ex, 400, "failure", "The dummy's name conflicts with that of an actual player.");
            return;
        }
        // 假人已在线
        if (manager.get(name) != null) {
            sendJson(ex, 400, "failure", "The dummy is already online.");
            return;
        }

        var location = Bukkit.getWorlds().get(0).getSpawnLocation().clone();
        var lifespan = Optional.ofNullable(config.getLifespan()).map(Duration::toMillis).orElse(FakeplayerTicker.NON_REMOVE_AT);
        try {
            var player = manager.spawnAsync(Bukkit.getConsoleSender(), name, location, lifespan).get(15, TimeUnit.SECONDS);
            if (player == null) {
                sendJson(ex, 500, "failure", "Spawn failed");
            } else {
                sendJson(ex, 200, "success", null);
            }
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleKick(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminKick())) {
            return;
        }
        var name = query(ex, "name");
        if (name == null || name.isBlank()) {
            sendJson(ex, 400, "failure", "Name is required");
            return;
        }
        var removed = manager.remove(name, "Removed via HTTP admin");
        if (removed) {
            sendJson(ex, 200, "success", null);
        } else {
            sendJson(ex, 400, "failure", "Dummies do not exist.");
        }
    }

    private void handleKill(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminKill())) {
            return;
        }
        var name = query(ex, "name");
        if (name == null || name.isBlank()) {
            sendJson(ex, 400, "failure", "Name is required");
            return;
        }
        var fake = manager.get(name);
        if (fake == null) {
            sendJson(ex, 400, "failure", "The dummy does not exist.");
            return;
        }
        if (fake.isDead()) {
            sendJson(ex, 400, "failure", "The dummy is already dead.");
            return;
        }
        try {
            callSync(() -> {
                fake.setHealth(0D);
                return null;
            });
            sendJson(ex, 200, "success", null);
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleSay(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminSay())) {
            return;
        }
        var name = query(ex, "name");
        if (name == null || name.isBlank()) {
            sendJson(ex, 400, "failure", "Name is required");
            return;
        }
        var message = query(ex, "message");
        if (message == null || message.isBlank()) {
            sendJson(ex, 400, "failure", "Message is required");
            return;
        }
        var fake = manager.get(name);
        if (fake == null) {
            sendJson(ex, 400, "failure", "The dummy does not exist.");
            return;
        }
        try {
            callSync(() -> {
                var setting = ActionSetting.once().clone();
                setting.message = message;
                actionManager.setAction(fake, ActionType.SAY, setting);
                return null;
            });
            sendJson(ex, 200, "success", null);
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleStatus(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminStatus())) {
            return;
        }
        var name = query(ex, "name");
        try {
            var list = callSync(() -> {
                List<Player> players;
                if (name == null || name.isBlank()) {
                    players = manager.getAll();
                } else {
                    var fake = manager.get(name);
                    players = fake == null ? List.of() : List.of(fake);
                }
                var statuses = new ArrayList<Map<String, Object>>(players.size());
                for (var fake : players) {
                    statuses.add(statusOf(fake));
                }
                return statuses;
            });
            if (name != null && !name.isBlank() && list.isEmpty()) {
                sendJson(ex, 400, "failure", "The dummy does not exist.");
                return;
            }
            logCall(ex, 200, "success", list.size() + " fake player(s)");
            send(ex, 200, "application/json", "{\"fakeplayer\":" + toJson(list) + "}");
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleInfo(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminInfo())) {
            return;
        }
        try {
            var info = callSync(() -> {
                var map = new LinkedHashMap<String, Object>();
                map.put("plugin", Main.getInstance().getPluginMeta().getVersion());
                map.put("minecraft", Bukkit.getMinecraftVersion());
                map.put("server", Bukkit.getName());
                map.put("server-version", Bukkit.getVersion());
                map.put("fakeplayer", manager.getSize());
                map.put("online-players", Bukkit.getOnlinePlayers().size());
                map.put("server-limit", config.getServerLimit());
                map.put("player-limit", config.getPlayerLimit());
                map.put("forced-execution", config.isForcedExecution());
                return map;
            });
            logCall(ex, 200, "success", info.get("fakeplayer") + " fake player(s)");
            send(ex, 200, "application/json", toJson(info));
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    /**
     * 触发任意动作, 参数映射到 {@link ActionSetting}
     */
    private void handleAction(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminAction())) {
            return;
        }
        var fake = requireFake(ex, query(ex, "name"));
        if (fake == null) {
            return;
        }

        var rawAction = query(ex, "action");
        if (rawAction == null || rawAction.isBlank()) {
            sendJson(ex, 400, "failure", "Action is required, available actions: " + actionNames());
            return;
        }
        final ActionType action;
        try {
            action = ActionType.valueOf(rawAction.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            sendJson(ex, 400, "failure", "Unknown action: " + rawAction + ", available actions: " + actionNames());
            return;
        }

        final Integer count;
        final Integer interval;
        final Integer wait;
        try {
            count = intQuery(ex, "count");
            interval = intQuery(ex, "interval");
            wait = intQuery(ex, "wait");
        } catch (IllegalArgumentException e) {
            sendJson(ex, 400, "failure", e.getMessage());
            return;
        }
        if (count != null && count < -1) {
            sendJson(ex, 400, "failure", "count must be -1 (unlimited) or greater than or equal to 0");
            return;
        }
        if (interval != null && interval < 0) {
            sendJson(ex, 400, "failure", "interval must be greater than or equal to 0");
            return;
        }
        if (wait != null && wait < 0) {
            sendJson(ex, 400, "failure", "wait must be greater than or equal to 0");
            return;
        }

        // 未指定 count 时: 指定了 interval 视为持续执行, 否则只执行一次
        final int maximum = count != null ? count : (interval != null && interval > 0 ? -1 : 1);
        final int tickInterval = interval == null ? 0 : interval;
        final int waitTicks = wait == null ? 0 : wait;
        final var message = query(ex, "message");
        if (action == ActionType.SAY && (message == null || message.isBlank())) {
            sendJson(ex, 400, "failure", "Message is required for the say action");
            return;
        }

        try {
            callSync(() -> {
                actionManager.setAction(fake, action, new ActionSetting(maximum, tickInterval, waitTicks, message));
                return null;
            });
            sendJson(ex, 200, "success", null);
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleStop(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminStop())) {
            return;
        }
        var fake = requireFake(ex, query(ex, "name"));
        if (fake == null) {
            return;
        }
        try {
            callSync(() -> {
                actionManager.stop(fake);
                return null;
            });
            sendJson(ex, 200, "success", null);
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleTeleport(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminTeleport())) {
            return;
        }
        var fake = requireFake(ex, query(ex, "name"));
        if (fake == null) {
            return;
        }

        final Double x;
        final Double y;
        final Double z;
        final Double yaw;
        final Double pitch;
        try {
            x = doubleQuery(ex, "x");
            y = doubleQuery(ex, "y");
            z = doubleQuery(ex, "z");
            yaw = doubleQuery(ex, "yaw");
            pitch = doubleQuery(ex, "pitch");
        } catch (IllegalArgumentException e) {
            sendJson(ex, 400, "failure", e.getMessage());
            return;
        }

        World world = null;
        var worldName = query(ex, "world");
        if (worldName != null && !worldName.isBlank()) {
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                sendJson(ex, 400, "failure", "Unknown world: " + worldName);
                return;
            }
        }
        final World targetWorld = world;

        try {
            callSync(() -> {
                var current = fake.getLocation();
                var target = new Location(
                        targetWorld == null ? current.getWorld() : targetWorld,
                        x == null ? current.getX() : x,
                        y == null ? current.getY() : y,
                        z == null ? current.getZ() : z,
                        yaw == null ? current.getYaw() : yaw.floatValue(),
                        pitch == null ? current.getPitch() : pitch.floatValue()
                );
                fake.teleport(target);
                return null;
            });
            sendJson(ex, 200, "success", null);
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    /**
     * 设置朝向, 支持直接指定方向、看向坐标或指定角度
     */
    private void handleLook(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminLook())) {
            return;
        }
        var fake = requireFake(ex, query(ex, "name"));
        if (fake == null) {
            return;
        }

        String direction = null;
        var rawDirection = query(ex, "direction");
        if (rawDirection != null && !rawDirection.isBlank()) {
            direction = rawDirection.trim().toUpperCase(Locale.ROOT);
            if (!LOOK_DIRECTIONS.contains(direction)) {
                sendJson(ex, 400, "failure", "Unknown direction: " + rawDirection + ", available directions: " + String.join(", ", LOOK_DIRECTIONS));
                return;
            }
        }

        double[] at = null;
        var rawAt = query(ex, "at");
        if (rawAt != null && !rawAt.isBlank()) {
            var parts = rawAt.split(",");
            if (parts.length != 3) {
                sendJson(ex, 400, "failure", "at must be in the form x,y,z");
                return;
            }
            at = new double[3];
            try {
                for (int i = 0; i < 3; i++) {
                    at[i] = Double.parseDouble(parts[i].trim());
                }
            } catch (NumberFormatException e) {
                sendJson(ex, 400, "failure", "at must be in the form x,y,z");
                return;
            }
        }

        final Double yaw;
        final Double pitch;
        try {
            yaw = doubleQuery(ex, "yaw");
            pitch = doubleQuery(ex, "pitch");
        } catch (IllegalArgumentException e) {
            sendJson(ex, 400, "failure", e.getMessage());
            return;
        }
        if (direction == null && at == null && yaw == null && pitch == null) {
            sendJson(ex, 400, "failure", "One of direction, at, yaw or pitch is required");
            return;
        }

        final String targetDirection = direction;
        final double[] targetAt = at;
        try {
            callSync(() -> {
                var handle = bridge.fromPlayer(fake);
                if (targetDirection != null) {
                    switch (targetDirection) {
                        case "NORTH" -> setRotation(handle, 180, 0);
                        case "SOUTH" -> setRotation(handle, 0, 0);
                        case "EAST" -> setRotation(handle, -90, 0);
                        case "WEST" -> setRotation(handle, 90, 0);
                        case "UP" -> setRotation(handle, fake.getLocation().getYaw(), -90);
                        case "DOWN" -> setRotation(handle, fake.getLocation().getYaw(), 90);
                        default -> {
                        }
                    }
                } else if (targetAt != null) {
                    LookUtils.lookAt(handle, new Location(null, targetAt[0], targetAt[1], targetAt[2]));
                } else {
                    var current = fake.getLocation();
                    setRotation(
                            handle,
                            yaw == null ? current.getYaw() : yaw.floatValue(),
                            pitch == null ? current.getPitch() : pitch.floatValue()
                    );
                }
                return null;
            });
            sendJson(ex, 200, "success", null);
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleHold(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminHold())) {
            return;
        }
        var fake = requireFake(ex, query(ex, "name"));
        if (fake == null) {
            return;
        }
        var rawSlot = query(ex, "slot");
        if (rawSlot == null || rawSlot.isBlank()) {
            sendJson(ex, 400, "failure", "Slot is required");
            return;
        }
        final int slot;
        try {
            slot = Integer.parseInt(rawSlot.trim());
        } catch (NumberFormatException e) {
            sendJson(ex, 400, "failure", "Invalid number: slot");
            return;
        }
        if (slot < 1 || slot > 9) {
            sendJson(ex, 400, "failure", "Slot must be between 1 and 9");
            return;
        }
        try {
            callSync(() -> {
                fake.getInventory().setHeldItemSlot(slot - 1);
                return null;
            });
            sendJson(ex, 200, "success", null);
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleSwap(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminSwap())) {
            return;
        }
        var fake = requireFake(ex, query(ex, "name"));
        if (fake == null) {
            return;
        }
        try {
            callSync(() -> {
                bridge.fromPlayer(fake).swapItemWithOffhand();
                return null;
            });
            sendJson(ex, 200, "success", null);
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleRespawn(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminRespawn())) {
            return;
        }
        var fake = requireFake(ex, query(ex, "name"));
        if (fake == null) {
            return;
        }
        try {
            if (!callSync(fake::isDead)) {
                sendJson(ex, 400, "failure", "The dummy is not dead.");
                return;
            }
            callSync(() -> {
                bridge.fromPlayer(fake).respawn();
                return null;
            });
            sendJson(ex, 200, "success", null);
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    /**
     * 以假人身份执行命令
     */
    private void handleCmd(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminCmd())) {
            return;
        }
        var fake = requireFake(ex, query(ex, "name"));
        if (fake == null) {
            return;
        }
        var command = query(ex, "command");
        if (command == null || command.isBlank()) {
            sendJson(ex, 400, "failure", "Command is required");
            return;
        }
        try {
            callSync(() -> {
                manager.issueCommands(fake, List.of(command));
                return null;
            });
            sendJson(ex, 200, "success", null);
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleKickAll(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminBatch())) {
            return;
        }
        try {
            var count = callSync(() -> manager.removeAll("Removed via HTTP admin"));
            sendJson(ex, 200, successWithCount(count));
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleKillAll(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminBatch())) {
            return;
        }
        try {
            var count = callSync(() -> {
                var killed = 0;
                for (var fake : manager.getAll()) {
                    if (fake.isDead()) {
                        continue;
                    }
                    fake.setHealth(0D);
                    killed++;
                }
                return killed;
            });
            sendJson(ex, 200, successWithCount(count));
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    private void handleSayAll(HttpExchange ex) throws IOException {
        if (!preflight(ex, config.isHttpAdminBatch())) {
            return;
        }
        var message = query(ex, "message");
        if (message == null || message.isBlank()) {
            sendJson(ex, 400, "failure", "Message is required");
            return;
        }
        try {
            var count = callSync(() -> {
                var said = 0;
                for (var fake : manager.getAll()) {
                    var setting = ActionSetting.once().clone();
                    setting.message = message;
                    actionManager.setAction(fake, ActionType.SAY, setting);
                    said++;
                }
                return said;
            });
            sendJson(ex, 200, successWithCount(count));
        } catch (Exception e) {
            sendJson(ex, 500, "failure", rootMsg(e));
        }
    }

    /**
     * 收集假人的详细状态, 必须在主线程调用
     */
    private Map<String, Object> statusOf(Player fake) {
        var location = fake.getLocation();
        var inventory = fake.getInventory();
        var status = new LinkedHashMap<String, Object>();

        status.put("name", fake.getName());
        status.put("uuid", fake.getUniqueId().toString());
        status.put("world", location.getWorld() == null ? null : location.getWorld().getName());
        status.put("x", location.getX());
        status.put("y", location.getY());
        status.put("z", location.getZ());
        status.put("yaw", location.getYaw());
        status.put("pitch", location.getPitch());
        status.put("dead", fake.isDead());
        status.put("alive", !fake.isDead() && fake.isValid());
        status.put("health", fake.getHealth());
        status.put("max-health", fake.getMaxHealth());
        status.put("food-level", fake.getFoodLevel());
        status.put("saturation", fake.getSaturation());
        status.put("level", fake.getLevel());
        status.put("exp", fake.getExp());
        status.put("game-mode", fake.getGameMode().name());
        status.put("sneaking", fake.isSneaking());
        status.put("sprinting", fake.isSprinting());
        status.put("sleeping", fake.isSleeping());
        status.put("on-ground", fake.isOnGround());
        status.put("ping", fake.getPing());
        status.put("creator", manager.getCreatorName(fake));
        status.put("main-hand", inventory.getItemInMainHand().getType().name());
        status.put("off-hand", inventory.getItemInOffHand().getType().name());
        status.put("hotbar-slot", inventory.getHeldItemSlot() + 1);
        status.put("spawned-at", MetadataKeys.getSpawnedAt(fake));
        status.put("actions", actionManager
                .getActiveActions(fake)
                .stream()
                .map(Enum::name)
                .collect(Collectors.toList()));
        return status;
    }

    /**
     * 接口自身的校验: 请求方法与接口开关。
     *
     * <p>鉴权、限流、路由与主机校验都已在 {@link #handleRequest(HttpExchange)} 中完成,
     * 这里不再重复鉴权 —— 正因为鉴权在前, 未授权调用方才无法通过 401 / 403 枚举接口开关。</p>
     *
     * @return 校验通过返回 true; 否则已输出错误响应并返回 false
     */
    private boolean preflight(HttpExchange ex, boolean enabled) throws IOException {
        if (!isMethodAllowed(ex)) {
            sendJson(ex, 405, "failure", "Method not allowed");
            return false;
        }
        if (!enabled) {
            sendJson(ex, 403, "failure", "Interface disabled");
            return false;
        }
        return true;
    }

    /**
     * 校验假人名并返回对应假人, 不存在时已输出错误响应
     */
    private @Nullable Player requireFake(HttpExchange ex, @Nullable String name) throws IOException {
        if (name == null || name.isBlank()) {
            sendJson(ex, 400, "failure", "Name is required");
            return null;
        }
        var fake = manager.get(name);
        if (fake == null) {
            sendJson(ex, 400, "failure", "The dummy does not exist.");
            return null;
        }
        return fake;
    }

    private static @Nullable Integer intQuery(HttpExchange ex, String key) {
        var raw = query(ex, key);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + key);
        }
    }

    private static @Nullable Double doubleQuery(HttpExchange ex, String key) {
        var raw = query(ex, key);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + key);
        }
    }

    private static void setRotation(NMSServerPlayer handle, float yaw, float pitch) {
        handle.setYRot(yaw % 360);
        handle.setXRot(Math.max(-90F, Math.min(90F, pitch)));
    }

    private static String actionNames() {
        return Arrays.stream(ActionType.values()).map(Enum::name).collect(Collectors.joining(", "));
    }

    private static Map<String, Object> successWithCount(int count) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("status", "success");
        payload.put("count", count);
        return payload;
    }

    /**
     * 在主线程执行并等待结果 (HTTP 处理线程不能直接操作 Bukkit API)
     */
    private static <T> T callSync(Callable<T> callable) throws Exception {
        return Schedulers.callGlobal(Main.getInstance(), callable).get(10, TimeUnit.SECONDS);
    }

    /**
     * 鉴权: 令牌统一通过 {@code Authorization: Bearer <token>} 请求头传递。
     *
     * <p>不再支持 {@code ?token=} 查询参数: 令牌出现在 URL 里会进入代理日志、浏览器历史与
     * Referer, 且每个接口都要重复拼一遍参数, 因此统一为请求头这一种方式。</p>
     */
    private boolean authorize(HttpExchange ex) {
        var auth = ex.getRequestHeaders().getFirst("Authorization");
        // Bearer 认证方案大小写不敏感 (RFC 7235)
        if (auth == null || !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return false;
        }
        var provided = auth.substring(7).trim();
        if (token == null || provided.isEmpty()) {
            return false;
        }
        // 定长比较, 避免通过响应时间逐字节推断 token
        return MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8)
        );
    }

    private void sendUnauthorized(HttpExchange ex) throws IOException {
        // 未授权的请求不把 query 写进日志
        sendJsonNoQuery(ex, 401, "failure", "Unauthorized: pass the token via the Authorization: Bearer header");
    }

    /**
     * 打印 HTTP 调用日志, 记录 query (其中的 token 已脱敏)
     */
    private static void logCall(HttpExchange ex, int code, String status, String detail) {
        logCall(ex, code, status, detail, true);
    }

    /**
     * 打印 HTTP 调用日志。
     * <p>记录 query 时: token 会被脱敏, 控制字符会被替换并截断长度,
     * 避免调用方通过换行等内容伪造日志或撑爆日志文件。
     * 未授权请求不应把 query 持久化, 此时传 {@code withQuery = false}。</p>
     */
    private static void logCall(HttpExchange ex, int code, String status, String detail, boolean withQuery) {
        var uri = ex.getRequestURI();
        var sb = new StringBuilder(uri.getPath());
        var q = uri.getQuery();
        if (withQuery && q != null && !q.isEmpty()) {
            sb.append('?').append(Arrays.stream(q.split("&"))
                    .map(HttpAdminService::redactParam)
                    .collect(Collectors.joining("&")));
        }
        log.info("[HTTP] %s %s from %s -> %d %s%s".formatted(
                ex.getRequestMethod(),
                sanitize(sb.toString()),
                remoteAddress(ex),
                code,
                status,
                detail == null ? "" : " (" + sanitize(detail) + ")"
        ));
    }

    /**
     * 参数名中包含 token 的一律打码 (不区分大小写与参数名写法)
     */
    private static String redactParam(String param) {
        var eq = param.indexOf('=');
        if (eq >= 0 && param.substring(0, eq).toLowerCase(Locale.ROOT).contains("token")) {
            return param.substring(0, eq) + "=***";
        }
        return param;
    }

    /**
     * 去掉会破坏单行日志的控制字符, 并限制长度
     */
    private static String sanitize(String s) {
        var truncated = s.length() > MAX_LOG_LENGTH;
        var end = truncated ? MAX_LOG_LENGTH : s.length();
        var sb = new StringBuilder(end + 3);
        for (int i = 0; i < end; i++) {
            var c = s.charAt(i);
            sb.append(c < 0x20 || c == 0x7f ? '?' : c);
        }
        return truncated ? sb.append("...").toString() : sb.toString();
    }

    private void sendJson(HttpExchange ex, int code, String status, String msg) throws IOException {
        logCall(ex, code, status, msg);
        writeJson(ex, code, status, msg);
    }

    /**
     * 输出错误响应, 且日志中不记录 query
     */
    private void sendJsonNoQuery(HttpExchange ex, int code, String status, String msg) throws IOException {
        logCall(ex, code, status, msg, false);
        writeJson(ex, code, status, msg);
    }

    private void writeJson(HttpExchange ex, int code, String status, String msg) throws IOException {
        var body = msg == null
                ? "{\"status\":\"" + status + "\"}"
                : "{\"status\":\"" + status + "\",\"msg\":\"" + escape(msg) + "\"}";
        send(ex, code, "application/json", body);
    }

    /**
     * 输出带附加字段的 JSON 响应
     */
    private void sendJson(HttpExchange ex, int code, Map<String, Object> payload) throws IOException {
        logCall(ex, code, String.valueOf(payload.getOrDefault("status", "success")), null);
        send(ex, code, "application/json", toJson(payload));
    }

    private void send(HttpExchange ex, int code, String contentType, String body) throws IOException {
        var bytes = body.getBytes(StandardCharsets.UTF_8);
        var headers = ex.getResponseHeaders();
        headers.add("Content-Type", contentType + "; charset=utf-8");
        // 管理接口的响应不应被浏览器或中间层缓存
        headers.add("Cache-Control", "no-store");
        headers.add("X-Content-Type-Options", "nosniff");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String query(HttpExchange ex, String key) {
        var q = ex.getRequestURI().getQuery();
        if (q == null) {
            return null;
        }
        for (var pair : q.split("&")) {
            var kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                try {
                    return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    return kv[1];
                }
            }
        }
        return null;
    }

    private static String toJsonArray(List<String> items) {
        var sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(escape(items.get(i))).append("\"");
        }
        return sb.append("]").toString();
    }

    /**
     * 极简 JSON 序列化, 支持 {@link Map} / {@link Iterable} / 数字 / 布尔 / 字符串 / null
     */
    private static String toJson(@Nullable Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Map<?, ?> map) {
            var sb = new StringBuilder("{");
            var first = true;
            for (var entry : map.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append('"').append(escape(String.valueOf(entry.getKey()))).append("\":").append(toJson(entry.getValue()));
            }
            return sb.append('}').toString();
        }
        if (value instanceof Iterable<?> iterable) {
            var sb = new StringBuilder("[");
            var first = true;
            for (var item : iterable) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append(toJson(item));
            }
            return sb.append(']').toString();
        }
        if (value instanceof Double d) {
            return Double.isFinite(d) ? d.toString() : "null";
        }
        if (value instanceof Float f) {
            return Float.isFinite(f) ? f.toString() : "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    private static String escape(String s) {
        var sb = new StringBuilder(s.length() + 16);
        for (var c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                // 其余控制字符一律转义, 保证输出始终是合法 JSON
                default -> {
                    if (c < 0x20 || c == 0x7f) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String rootMsg(Throwable e) {
        var t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        var m = t.getMessage();
        return m != null ? m : t.getClass().getSimpleName();
    }
}
