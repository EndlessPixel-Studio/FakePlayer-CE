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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
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
 * 所有接口需携带正确 token (?token= 或 Authorization: Bearer), token 为空时启动时随机生成。
 * 每个接口可通过 config.yml 中的 http-admin.interface.&lt;名字&gt; 单独关闭。
 */
@Singleton
public class HttpAdminService {

    private final static Logger log = Main.getInstance().getLogger();

    private final static List<String> LOOK_DIRECTIONS = List.of("NORTH", "SOUTH", "EAST", "WEST", "UP", "DOWN");

    private final FakeplayerManager manager;
    private final FakeplayerConfig config;
    private final ActionManager actionManager;
    private final NMSBridge bridge;

    private HttpServer server;
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

        server.createContext("/list", this::handleList);
        server.createContext("/status", this::handleStatus);
        server.createContext("/info", this::handleInfo);
        server.createContext("/spawn", this::handleSpawn);
        server.createContext("/kick", this::handleKick);
        server.createContext("/kill", this::handleKill);
        server.createContext("/respawn", this::handleRespawn);
        server.createContext("/say", this::handleSay);
        server.createContext("/action", this::handleAction);
        server.createContext("/stop", this::handleStop);
        server.createContext("/teleport", this::handleTeleport);
        server.createContext("/look", this::handleLook);
        server.createContext("/hold", this::handleHold);
        server.createContext("/swap", this::handleSwap);
        server.createContext("/cmd", this::handleCmd);
        server.createContext("/kickall", this::handleKickAll);
        server.createContext("/killall", this::handleKillAll);
        server.createContext("/sayall", this::handleSayAll);
        server.setExecutor(null);
        server.start();
        log.info("HTTP admin server started on " + config.getHttpAdminHost() + ":" + config.getHttpAdminPort());
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            log.info("HTTP admin server stopped");
        }
    }

    private void handleList(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, "failure", "Method not allowed");
            return;
        }
        if (!config.isHttpAdminList()) {
            sendJson(ex, 403, "failure", "Interface disabled");
            return;
        }
        if (!authorize(ex)) {
            sendUnauthorized(ex);
            return;
        }
        var names = manager.getAll().stream().map(Player::getName).collect(Collectors.toList());
        logCall(ex, 200, "success", names.size() + " fake player(s)");
        send(ex, 200, "application/json", "{\"fakeplayer\":" + toJsonArray(names) + "}");
    }

    private void handleSpawn(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, "failure", "Method not allowed");
            return;
        }
        if (!config.isHttpAdminSpawn()) {
            sendJson(ex, 403, "failure", "Interface disabled");
            return;
        }
        if (!authorize(ex)) {
            sendUnauthorized(ex);
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
        if (!"GET".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, "failure", "Method not allowed");
            return;
        }
        if (!config.isHttpAdminKick()) {
            sendJson(ex, 403, "failure", "Interface disabled");
            return;
        }
        if (!authorize(ex)) {
            sendUnauthorized(ex);
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
        if (!"GET".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, "failure", "Method not allowed");
            return;
        }
        if (!config.isHttpAdminKill()) {
            sendJson(ex, 403, "failure", "Interface disabled");
            return;
        }
        if (!authorize(ex)) {
            sendUnauthorized(ex);
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
        if (!"GET".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, "failure", "Method not allowed");
            return;
        }
        if (!config.isHttpAdminSay()) {
            sendJson(ex, 403, "failure", "Interface disabled");
            return;
        }
        if (!authorize(ex)) {
            sendUnauthorized(ex);
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
     * 统一处理请求方法、接口开关与鉴权
     *
     * @return 校验通过返回 true; 否则已输出错误响应并返回 false
     */
    private boolean preflight(HttpExchange ex, boolean enabled) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, "failure", "Method not allowed");
            return false;
        }
        if (!enabled) {
            sendJson(ex, 403, "failure", "Interface disabled");
            return false;
        }
        if (!authorize(ex)) {
            sendUnauthorized(ex);
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
        return Bukkit.getScheduler().callSyncMethod(Main.getInstance(), callable).get(10, TimeUnit.SECONDS);
    }

    private boolean authorize(HttpExchange ex) {
        var provided = query(ex, "token");
        if (provided == null) {
            var auth = ex.getRequestHeaders().getFirst("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                provided = auth.substring(7);
            }
        }
        return token != null && token.equals(provided);
    }

    private void sendUnauthorized(HttpExchange ex) throws IOException {
        sendJson(ex, 401, "failure", "Unauthorized");
    }

    /**
     * 打印 HTTP 调用日志, query 中的 token 已脱敏
     */
    private static void logCall(HttpExchange ex, int code, String status, String detail) {
        var uri = ex.getRequestURI();
        var sb = new StringBuilder(uri.getPath());
        var q = uri.getQuery();
        if (q != null && !q.isEmpty()) {
            sb.append('?').append(Arrays.stream(q.split("&"))
                    .map(p -> p.startsWith("token=") ? "token=***" : p)
                    .collect(Collectors.joining("&")));
        }
        var remote = ex.getRemoteAddress();
        var ip = remote == null ? "unknown" : remote.getAddress().getHostAddress();
        log.info("[HTTP] %s %s from %s -> %d %s%s".formatted(
                ex.getRequestMethod(),
                sb,
                ip,
                code,
                status,
                detail == null ? "" : " (" + detail + ")"
        ));
    }

    private void sendJson(HttpExchange ex, int code, String status, String msg) throws IOException {
        logCall(ex, code, status, msg);
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
        ex.getResponseHeaders().add("Content-Type", contentType + "; charset=utf-8");
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
        var sb = new StringBuilder();
        for (var c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
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
