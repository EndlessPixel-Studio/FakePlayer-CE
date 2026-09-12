package io.github.hello09x.fakeplayer.core.util;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * 离线创建者代理。
 * <p>服务器启动自动恢复假人时原创建者通常不在线, 以该代理作为创建者身份:
 * 假人仍归属于原创建者 (按名称匹配), 同时 {@link #isOp()} 返回 true 以跳过假人数量限制检查。</p>
 */
public class OfflineCreator implements CommandSender {

    private final String name;

    public OfflineCreator(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public @NotNull Component name() {
        return Component.text(this.name);
    }

    @Override
    public @NotNull Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public void sendMessage(@NotNull String message) {
        // 恢复流程中的提示消息无需发送, 忽略
    }

    @Override
    public void sendMessage(@NotNull String... messages) {
    }

    @Override
    public void sendMessage(@NotNull UUID sender, @NotNull String message) {
    }

    @Override
    public void sendMessage(@NotNull UUID sender, @NotNull String... messages) {
    }

    @Override
    public void sendMessage(@NotNull Component message) {
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message) {
    }

    @Override
    public boolean isOp() {
        // 跳过 checkLimit 中的数量限制检查, 保证恢复不会被限制拦截
        return true;
    }

    @Override
    public void setOp(boolean value) {
        // no-op
    }

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return false;
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull Permission perm) {
        return false;
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recalculatePermissions() {
        // no-op
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull Spigot spigot() {
        return new Spigot();
    }
}
