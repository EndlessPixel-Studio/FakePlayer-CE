package io.github.endlesspixel.fakeplayer.util;

import io.github.hello09x.fakeplayer.core.Main;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * 假人发包失败时的兜底。
 *
 * <p>假人没有真实连接, 各版本模块都重写了 {@code ServerGamePacketListenerImpl#send}。一旦重写里抛出异常, 它会顺着 调用栈冒泡回调用方 ——
 * 而调用方经常是别的插件: 例如 PlayerPoints 在自己的异步任务里调用 {@code Player#sendPluginMessage} 通知点券变化,
 * 异常会让它的任务中断、事务回滚, 玩家看到的就是「交易失败并回退」。
 *
 * <p>所以假人自己的发包逻辑必须自己吞掉异常, 只在日志里留痕。
 */
public final class PacketSendGuard {

  private static final Logger log = Main.getInstance().getLogger();

  /** 同类异常的最小日志间隔, 避免刷屏 */
  private static final long INTERVAL_MS = 60_000L;

  private static final Map<String, Long> lastLogged = new ConcurrentHashMap<>();

  private PacketSendGuard() {}

  /**
   * 记录一次发包异常。相同类型与消息的异常每分钟最多输出一条。
   *
   * @param error 异常
   */
  public static void onFailure(@NotNull Throwable error) {
    var key = error.getClass().getName() + ": " + error.getMessage();
    var now = System.currentTimeMillis();
    var previous = lastLogged.get(key);
    if (previous != null && now - previous < INTERVAL_MS) {
      return;
    }
    lastLogged.put(key, now);

    var hint =
        error instanceof LinkageError
            // 常见于非 Paper 系核心 (如 Leaves 删掉了 DiscardedPayload#data()), 只影响对应功能本身
            ? " (the server fork is missing a required NMS method, the affected feature is skipped)"
            : "";
    log.log(Level.WARNING, "A fake player failed to handle an outgoing packet" + hint, error);
  }
}
