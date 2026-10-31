package io.github.hello09x.fakeplayer.api.spi;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface NMSServerPlayer {

    /**
     * @return 返回 bukkit 的 Player 对象
     */
    @NotNull
    Player getPlayer();

    /**
     * @return X 坐标
     */
    double getX();

    /**
     * @return Y 坐标
     */
    double getY();

    /**
     * @return Z 坐标
     */
    double getZ();

    /**
     * 设置 X 坐标偏移值
     *
     * @param xo 偏移值
     */
    void setXo(double xo);

    /**
     * 设置 Y 坐标偏移值
     *
     * @param yo 偏移值
     */
    void setYo(double yo);

    /**
     * 设置 Z 坐标偏移值
     *
     * @param zo 偏移值
     */
    void setZo(double zo);

    /**
     * 执行时刻运算
     */
    void doTick();

    /**
     * 移动玩家
     *
     * @param x    X 坐标
     * @param y    Y 坐标
     * @param z    Z 坐标
     * @param yRot 头部 Y 角度
     * @param xRot 头部 X 角度
     */
    void absMoveTo(double x, double y, double z, float yRot, float xRot);

    /**
     * 获取头部 Y 角度
     *
     * @return Y 角度
     */
    float getYRot();

    /**
     * 设置头部 Y 角度
     *
     * @param yRot Y 角度
     */
    void setYRot(float yRot);

    /**
     * 获取头部 X 角度
     *
     * @return 头部 X 角度
     */
    float getXRot();

    /**
     * 设置头部 X 角度
     *
     * @param xRot 头部 X 角度
     */
    void setXRot(float xRot);

    /**
     * 获取 Z 坐标移动
     * @return Z 坐标移动
     */
    float getZza();

    /**
     * 设置 Z 坐标移动
     *
     * @param zza 移动距离
     */
    void setZza(float zza);

    /**
     * 获取 X 坐标移动
     * @return X 坐标移动
     */
    float getXxa();

    /**
     * 设置 X 坐标移动
     *
     * @param xxa 与第三双重
     */
    void setXxa(float xxa);

    /**
     * 设置相对移动
     * @param vector 相对移动
     */
    void setDeltaMovement(@NotNull Vector vector);

    /**
     * 骑上实体
     *
     * @param entity 实体
     * @param force  是否强制
     * @return 是否骑上
     */
    boolean startRiding(@NotNull Entity entity, boolean force);

    /**
     * 取消骑行实体
     */
    void stopRiding();

    /**
     * 设置不保存成就数据
     *
     * @param plugin 插件
     */
    void disableAdvancements(@NotNull Plugin plugin);

    /**
     * 标记假人的「客户端已加载世界」
     * <p>假人没有真实客户端, 不标记时服务端会认为它还没加载完世界, 出生后一段时间内免疫伤害。</p>
     * <p>仅 1.21.11 及以后的服务端有这个机制, 更早的版本无需处理 (默认空实现)。</p>
     */
    default void markClientLoaded() {
        // 老版本没有这个限制
    }

    /**
     * 获取时刻计数, 尽管假人会退出游戏, 但服务器重启前这个值不会重置
     *
     * @return 时刻计数
     */
    int getTickCount();

    /**
     * 丢弃物品
     *
     * @param slot  槽位
     * @param flag
     * @param flag1
     */
    void drop(int slot, boolean flag, boolean flag1);

    /**
     * 丢弃物品
     *
     * @param allStack 是否丢弃整组
     */
    void drop(boolean allStack);

    /**
     * 重设最后活跃时间
     */
    void resetLastActionTime();

    /**
     * 判断是否在地面
     *
     * @return 是否在地面
     */
    boolean onGround();

    /**
     * 从地面跳起
     */
    void jumpFromGround();

    /**
     * 设置是否跳跃中
     *
     * @param jumping 是否条约中
     */
    void setJumping(boolean jumping);

    /**
     * 是否在使用物品
     *
     * @return 是否在使用物品
     */
    boolean isUsingItem();

    /**
     * 设置曾玩过这个服务器
     */
    void setPlayBefore();

    /**
     * 设置客户端选项。聊天可见性必须是 FULL：
     * Paper 的 ServerGamePacketListenerImpl.chat() 会对 SYSTEM/HIDDEN 提前返回，
     * 导致 /fp say 无法进入 ChatProcessor，也不会触发聊天格式化事件。
     */
    void setupClientOptions();

    /**
     * 重生
     */
    void respawn();

    /**
     * 交换主副手物品
     */
    void swapItemWithOffhand();

    /**
     * 发送聊天信息
     *
     * @param message 聊天信息
     */
    void chat(@NotNull String message);

}
