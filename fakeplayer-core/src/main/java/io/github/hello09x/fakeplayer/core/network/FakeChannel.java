package io.github.hello09x.fakeplayer.core.network;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FakeChannel extends AbstractChannel {

  private static final ChannelMetadata METADATA = new ChannelMetadata(true);
  private static final EventLoop EVENT_LOOP = new DefaultEventLoop();

  private final ChannelConfig config = new DefaultChannelConfig(this);
  private final ChannelPipeline pipeline = new FakeChannelPipeline(this);
  private final InetAddress address;
  private volatile boolean active = true;

  public FakeChannel(@Nullable Channel parent, @NotNull InetAddress address) {
    super(parent);
    this.address = address;
    // autoRead 只设置一次, 不在 config() getter 里做副作用
    this.config.setAutoRead(true);
  }

  /** 在插件 onDisable 中调用, 避免 DefaultEventLoop 线程泄漏 */
  public static void shutdownEventLoop() {
    if (EVENT_LOOP instanceof DefaultEventLoop del) {
      del.shutdownGracefully(0, 1, TimeUnit.SECONDS);
    }
  }

  @Override public ChannelConfig config() { return config; }

  @Override public ChannelPipeline pipeline() { return pipeline; }

  @Override public EventLoop eventLoop() { return EVENT_LOOP; }

  @Override public ChannelMetadata metadata() { return METADATA; }

  @Override public boolean isActive() { return active; }

  @Override public boolean isOpen() { return active; }

  @Override protected boolean isCompatible(EventLoop loop) { return loop == EVENT_LOOP; }

  @Override protected void doBeginRead() {}

  @Override protected void doBind(SocketAddress arg0) {}

  @Override protected void doClose() {
    if (active) {
      active = false;
      pipeline.fireChannelInactive();
    }
  }

  @Override protected void doDisconnect() {
    doClose();
  }

  @Override
  protected void doWrite(ChannelOutboundBuffer in) {
    // 假人不真正发送网络包, 但显式丢弃便于排查
    for (;;) {
      Object msg = in.current();
      if (msg == null) break;
      in.remove();
    }
  }

  @Override protected SocketAddress localAddress0() {
    // 本地地址用回环地址: 假人并不真的占用 address
    return new InetSocketAddress("127.0.0.1", 0);
  }

  @Override protected SocketAddress remoteAddress0() {
    // 远程地址即"服务器地址", address 表示假人所在服务器 IP
    return new InetSocketAddress(address, 25565);
  }

  @Override
  protected AbstractUnsafe newUnsafe() {
    return new AbstractUnsafe() {
      @Override
      public void connect(
          SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        safeSetSuccess(promise);
        pipeline().fireChannelActive();
      }
    };
  }
}
