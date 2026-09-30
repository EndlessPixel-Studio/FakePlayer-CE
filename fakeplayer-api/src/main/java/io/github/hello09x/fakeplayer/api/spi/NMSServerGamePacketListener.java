package io.github.hello09x.fakeplayer.api.spi;

import org.bukkit.plugin.messaging.StandardMessenger;

public interface NMSServerGamePacketListener {


    String BUNGEE_CORD_CHANNEL = "BungeeCord";

    String BUNGEE_CORD_CORRECTED_CHANNEL = StandardMessenger.validateAndCorrectChannel(BUNGEE_CORD_CHANNEL);

    /**
     * 设置显示用的延迟值 (ping)
     * <p>会影响 Tab 列表里的延迟以及 {@code Player#getPing()}</p>
     *
     * @param ping      延迟 (毫秒), 负数表示改回使用服务端计算出的真实值
     * @param broadcast 是否立即把该延迟广播给所有在线玩家
     *                  (原版每 30 秒才广播一次, 因此设置后需要主动广播一次才能立刻生效)
     */
    void setPing(int ping, boolean broadcast);

}
