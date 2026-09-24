package io.github.hello09x.fakeplayer.core.compat.login;

import io.github.hello09x.fakeplayer.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Paper integration for LibreLogin and LibreLoginNext.
 *
 * <p>Both plugins expose matching authorization and database APIs. Their Paper listeners also expect a real
 * network login to seed a private user cache before {@code PlayerJoinEvent}; fake players skip that network
 * pipeline, so this adapter creates a normal unregistered user record when needed and mirrors the user into
 * that cache under the fake player's UUID.</p>
 */
public class LibreLoginCompat implements LoginCompat {

    private static final String PAPER_LISTENERS_FIELD = "listeners";
    private static final String USER_CACHE_FIELD = "readOnlyUserCache";

    private final String pluginName;
    private final String apiPackage;
    private final String providerClassName;
    private final String apiGetter;
    private final Map<UUID, Object> createdUsers = new ConcurrentHashMap<>();
    private final Map<UUID, Object> usersByFakePlayer = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Void>> pendingCleanups = new ConcurrentHashMap<>();

    public LibreLoginCompat(
            @NotNull String pluginName,
            @NotNull String apiPackage,
            @NotNull String providerClassName,
            @NotNull String apiGetter
    ) {
        this.pluginName = pluginName;
        this.apiPackage = apiPackage;
        this.providerClassName = providerClassName;
        this.apiGetter = apiGetter;
    }

    @Override
    public @NotNull String pluginName() {
        return pluginName;
    }

    @Override
    public boolean prepare(@NotNull Player fakePlayer, @NotNull InetAddress address) {
        try {
            Object api = getApi();
            Object database = invoke(api, "getDatabaseProvider");
            Object user = getUser(database, fakePlayer.getName());

            if (user != null) {
                String storedName = (String) invoke(user, "getLastNickname");
                if (!fakePlayer.getName().equals(storedName)) {
                    throw new IllegalStateException("account name casing differs (stored as " + storedName + ")");
                }
                return true;
            }

            UUID userId = newUserId(api, database, fakePlayer);
            Timestamp now = Timestamp.from(Instant.now());
            ClassLoader classLoader = Bukkit.getPluginManager().getPlugin(pluginName).getClass().getClassLoader();
            Class<?> hashedPasswordClass = classLoader.loadClass(apiPackage + ".api.crypto.HashedPassword");
            Method createUser = api.getClass().getMethod(
                    "createUser",
                    UUID.class,
                    UUID.class,
                    hashedPasswordClass,
                    String.class,
                    Timestamp.class,
                    Timestamp.class,
                    String.class,
                    String.class,
                    Timestamp.class,
                    String.class,
                    String.class
            );
            user = createUser.invoke(api, new Object[]{
                    userId, null, null, fakePlayer.getName(), now, now, null, address.getHostAddress(), null, null, null
            });
            createdUsers.put(fakePlayer.getUniqueId(), user);
            invoke(database, "insertUser", user);
            return true;
        } catch (Throwable throwable) {
            warn("could not prepare fake player " + fakePlayer.getName(), throwable);
            return false;
        }
    }

    @Override
    public boolean afterPreLogin(@NotNull Player fakePlayer) {
        try {
            Object api = getApi();
            Object database = invoke(api, "getDatabaseProvider");
            Object user = getUser(database, fakePlayer.getName());
            if (user == null) {
                throw new IllegalStateException("user record was not created");
            }

            usersByFakePlayer.put(fakePlayer.getUniqueId(), user);
            Object listeners = getField(api, PAPER_LISTENERS_FIELD);
            Object userCache = getField(listeners, USER_CACHE_FIELD);
            invoke(userCache, "put", fakePlayer.getUniqueId(), user);
            return true;
        } catch (Throwable throwable) {
            warn("could not prepare the Paper login cache for fake player " + fakePlayer.getName(), throwable);
            return false;
        }
    }

    @Override
    public void exempt(@NotNull Player fakePlayer) {
        // Wait until the login plugin's PlayerJoinEvent handler has started tracking this player.
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            if (!fakePlayer.isOnline()) {
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> authorize(fakePlayer));
        });
    }

    private void authorize(Player fakePlayer) {
        try {
            Object api = getApi();
            Object user = usersByFakePlayer.get(fakePlayer.getUniqueId());
            if (user == null) {
                Object database = invoke(api, "getDatabaseProvider");
                user = getUser(database, fakePlayer.getName());
            }
            if (user == null) {
                throw new IllegalStateException("user record was not found");
            }

            Object authorization = invoke(api, "getAuthorizationProvider");
            if ((boolean) invoke(authorization, "isAuthorized", fakePlayer)) {
                return;
            }

            Method authorize = findMethod(authorization, "authorize", user, fakePlayer, null);
            Class<?> reasonType = authorize.getParameterTypes()[2];
            if (!reasonType.isEnum()) {
                throw new IllegalStateException("unexpected authentication reason type: " + reasonType.getName());
            }
            Object loginReason = Arrays.stream(reasonType.getEnumConstants())
                    .filter(value -> ((Enum<?>) value).name().equals("LOGIN"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("LOGIN authentication reason is unavailable"));
            authorize.invoke(authorization, user, fakePlayer, loginReason);
        } catch (Throwable throwable) {
            warn("could not authorize fake player " + fakePlayer.getName(), throwable);
        } finally {
            usersByFakePlayer.remove(fakePlayer.getUniqueId());
        }
    }

    @Override
    public void cleanup(@NotNull Player fakePlayer) {
        cleanup(fakePlayer.getUniqueId());
    }

    @Override
    public void cleanupAll() {
        for (UUID fakePlayerId : createdUsers.keySet()) {
            cleanup(fakePlayerId);
        }
        CompletableFuture.allOf(pendingCleanups.values().toArray(CompletableFuture[]::new)).join();
    }

    private void cleanup(UUID fakePlayerId) {
        Object createdUser = createdUsers.remove(fakePlayerId);
        usersByFakePlayer.remove(fakePlayerId);
        if (createdUser == null) {
            return;
        }

        try {
            Object api = getApi();
            Object database = invoke(api, "getDatabaseProvider");
            Object userRecord = createdUser;
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> deleteTemporaryUser(
                    database,
                    userRecord,
                    fakePlayerId
            ));
            pendingCleanups.put(fakePlayerId, task);
            task.whenComplete((ignored, throwable) -> pendingCleanups.remove(fakePlayerId, task));
        } catch (Throwable throwable) {
            warn("could not remove temporary login record for fake player " + fakePlayerId, throwable);
        }
    }

    private void deleteTemporaryUser(Object database, Object createdUser, UUID fakePlayerId) {
        try {
            UUID userId = (UUID) invoke(createdUser, "getUuid");
            Object currentUser = invoke(database, "getByUUID", userId);
            if (currentUser == null) {
                return;
            }

            String createdName = (String) invoke(createdUser, "getLastNickname");
            String currentName = (String) invoke(currentUser, "getLastNickname");
            boolean registered = (boolean) invoke(currentUser, "isRegistered");
            if (!Objects.equals(createdName, currentName) || registered) {
                return;
            }

            invoke(database, "deleteUser", currentUser);
        } catch (Throwable throwable) {
            warn("could not remove temporary login record for fake player " + fakePlayerId, throwable);
        }
    }

    private Object getApi() throws ReflectiveOperationException {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null || !plugin.isEnabled()) {
            throw new IllegalStateException(pluginName + " is not enabled");
        }

        Class<?> providerClass = plugin.getClass().getClassLoader().loadClass(providerClassName);
        if (!providerClass.isInstance(plugin)) {
            throw new IllegalStateException(pluginName + " does not implement " + providerClassName);
        }

        Object api = providerClass.getMethod(apiGetter).invoke(plugin);
        if (api == null) {
            throw new IllegalStateException(pluginName + " API is not initialized");
        }
        return api;
    }

    private UUID newUserId(Object api, Object database, Player fakePlayer) throws ReflectiveOperationException {
        UUID configuredId = null;
        try {
            Method generator = api.getClass().getMethod("generateNewUUID", String.class, UUID.class);
            configuredId = (UUID) generator.invoke(api, fakePlayer.getName(), null);
        } catch (NoSuchMethodException ignored) {
            // Older API implementations may not expose the configured UUID generator.
        }

        if (configuredId != null && invoke(database, "getByUUID", configuredId) == null) {
            return configuredId;
        }

        UUID fakePlayerId = fakePlayer.getUniqueId();
        if (invoke(database, "getByUUID", fakePlayerId) == null) {
            return fakePlayerId;
        }

        for (int attempt = 0; attempt < 10; attempt++) {
            UUID candidate = UUID.randomUUID();
            if (invoke(database, "getByUUID", candidate) == null) {
                return candidate;
            }
        }
        throw new IllegalStateException("could not allocate a unique LibreLogin UUID");
    }

    private Object getUser(Object database, String name) throws ReflectiveOperationException {
        return invoke(database, "getByName", name);
    }

    private Object getField(Object target, String name) throws ReflectiveOperationException {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                Object value = field.get(target);
                if (value == null) {
                    throw new IllegalStateException(name + " has not been initialized");
                }
                return value;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(target.getClass().getName() + "." + name);
    }

    private Object invoke(Object target, String name, Object... arguments) throws ReflectiveOperationException {
        Method method = findMethod(target, name, arguments);
        return method.invoke(target, arguments);
    }

    private Method findMethod(Object target, String name, Object... arguments) throws NoSuchMethodException {
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != arguments.length) {
                continue;
            }

            Class<?>[] parameters = method.getParameterTypes();
            boolean compatible = true;
            for (int index = 0; index < parameters.length; index++) {
                if (arguments[index] != null && !parameters[index].isAssignableFrom(arguments[index].getClass())) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException(target.getClass().getName() + "." + name);
    }

    private void warn(String action, Throwable throwable) {
        Throwable cause = throwable instanceof InvocationTargetException invocation && invocation.getCause() != null
                ? invocation.getCause()
                : throwable;
        Main.getInstance().getLogger().warning(
                "LibreLogin compatibility (" + pluginName + "): " + action + ": " + cause.getMessage()
        );
    }

}
