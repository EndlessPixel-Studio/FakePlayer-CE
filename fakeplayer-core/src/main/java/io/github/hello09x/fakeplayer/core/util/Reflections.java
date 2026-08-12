package io.github.hello09x.fakeplayer.core.util;

import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Reflections {

    public static @Nullable Field getFirstFieldByType(
            @NotNull Class<?> clazz,
            @NotNull Class<?> fieldType,
            boolean includeStatic
    ) {
        for (var field : clazz.getDeclaredFields()) {
            if (includeStatic ^ Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getType() == fieldType) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }


    public static @N Field getFistFieldByTypeIncludeParent(
            @NotNull Class<?> clazz,
            @NotNull Class<?> fieldType
    ) {
        for (var field : clazz.getDeclaredFields()) {
            if (field.getType() == fieldType) {
                field.setAccessible(true);
                return field;
            }
        }
        var superclass = clazz.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return null;
        }
        return getFistFieldByTypeIncludeParent(superclass, fieldType);
    }

    public static @Nullable Field getFirstFieldByAssignFromType(
            @NotNull Class<?> clazz,
            @NotNull Class<?> fieldType,
            boolean includeStatic
    ) {
        for (var field : clazz.getDeclaredFields()) {
            if (includeStatic ^ Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (fieldType.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    public static Object getHandle(Object obj) {
        try {
            Method method = obj.getClass().getMethod("getHandle");
            method.setAccessible(true);
            return method.invoke(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getServer(Object obj) {
        try {
            Method method = obj.getClass().getMethod("getServer");
            method.setAccessible(true);
            return method.invoke(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resolves the NMS {@code MinecraftServer} from a Bukkit {@link org.bukkit.Server}.
     *
     * <p>Some server forks (e.g. Leaf 1.21.11) changed the semantics of
     * {@code CraftServer#getServer()} so it no longer returns the {@code MinecraftServer}
     * instance (it may return the player list instead). To stay compatible, we first look
     * for a field whose exact type is the given {@code minecraftServerClass} (typically the
     * {@code console} field on CraftServer), and only fall back to the {@code getServer()}
     * method when no such field exists.</p>
     *
     * @param server               the Bukkit server instance
     * @param minecraftServerClass the NMS {@code MinecraftServer} class of the current version
     * @return the NMS {@code MinecraftServer} instance
     */
    public static Object getMinecraftServer(Object server, Class<?> minecraftServerClass) {
        Field field = getFistFieldByTypeIncludeParent(server.getClass(), minecraftServerClass);
        if (field != null) {
            try {
                return field.get(server);
            } catch (Exception ignored) {
                // fall through to method-based lookup
            }
        }
        return getServer(server);
    }

}
