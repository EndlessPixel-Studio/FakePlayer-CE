package io.github.hello09x.fakeplayer.core.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.hello09x.fakeplayer.api.spi.ActionSetting;
import io.github.hello09x.fakeplayer.api.spi.ActionType;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.entity.FakeplayerTicker;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.manager.action.ActionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
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
 *     <li>GET /list  -> {"fakeplayer":["name",...]}</li>
 *     <li>GET /spawn?name=xxx -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /kick?name=xxx  -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /kill?name=xxx  -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 *     <li>GET /say?name=xxx&amp;message=yyy -> {"status":"success"} | {"status":"failure","msg":"..."}</li>
 * </ul>
 * 所有接口需携带正确 token (?token= 或 Authorization: Bearer), token 为空时启动时随机生成。
 */
@Singleton
public class HttpAdminService {

    private final static Logger log = Main.getInstance().getLogger();

    private final FakeplayerManager manager;
    private final FakeplayerConfig config;
    private final ActionManager actionManager;

    private HttpServer server;
    private String token;

    @Inject
    public HttpAdminService(FakeplayerManager manager, FakeplayerConfig config, ActionManager actionManager) {
        this.manager = manager;
        this.config = config;
        this.actionManager = actionManager;
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
        server.createContext("/spawn", this::handleSpawn);
        server.createContext("/kick", this::handleKick);
        server.createContext("/kill", this::handleKill);
        server.createContext("/say", this::handleSay);
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

    private void sendJson(HttpExchange ex, int code, String status, String msg) throws IOException {
        var body = msg == null
                ? "{\"status\":\"" + status + "\"}"
                : "{\"status\":\"" + status + "\",\"msg\":\"" + escape(msg) + "\"}";
        send(ex, code, "application/json", body);
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
