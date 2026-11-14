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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakeChannel extends AbstractChannel {

  private static final ChannelMetadata METADATA = new ChannelMetadata(true);
  private static final EventLoop EVENT_LOOP = new DefaultEventLoop();

  private final ChannelConfig config = new DefaultChannelConfig(this);
  private final ChannelPipeline pipeline = new FakeChannelPipeline(this);
  private final InetAddress address;

  /** 底层 fd 是否未释放; 初值 true 对应 Netty 语义中"已就绪未连接" */
  private volatile boolean open = true;

  /** 连接是否已建立; 初值 false, connect 成功时置 true */
  private volatile boolean active = false;

  public FakeChannel(@Nullable Channel parent, @NotNull InetAddress address) {
    super(parent);
    this.address = address;
    // autoRead 只在此处设置一次, config() getter 不再产生副作用
    this.config.setAutoRead(true);
  }

  @Override
  public ChannelConfig config() {
    return config;
  }

  @Override
  public ChannelPipeline pipeline() {
    return pipeline;
  }

  @Override
  public EventLoop eventLoop() {
    return EVENT_LOOP;
  }

  @Override
  public ChannelMetadata metadata() {
    return METADATA;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  protected boolean isCompatible(EventLoop arg0) {
    return arg0 == EVENT_LOOP;
  }

  @Override
  protected void doBeginRead() throws Exception {}

  @Override
  protected void doBind(SocketAddress arg0) throws Exception {}

  @Override
  protected void doClose() throws Exception {
    // 不在此处 fireChannelInactive: AbstractUnsafe#close() 会在 doClose0 返回后
    // 依据 wasActive 自行 fireChannelInactiveAndDeregister, 再手动 fire 会重复触发
    active = false;
    open = false;
  }

  @Override
  protected void doDisconnect() throws Exception {
    doClose();
  }

  @Override
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    for (; ; ) {
      Object msg = in.current();
      if (msg == null) {
        break;
      }
      in.remove();
    }
  }

  @Override
  protected SocketAddress localAddress0() {
    return new InetSocketAddress(address, 25565);
  }

  @Override
  protected SocketAddress remoteAddress0() {
    return new InetSocketAddress(address, 25565);
  }

  @Override
  protected AbstractUnsafe newUnsafe() {
    return new AbstractUnsafe() {
      @Override
      public void connect(
          SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        // 先置 active 再完成 promise: promise listener 中 isActive() 已为 true
        active = true;
        safeSetSuccess(promise);
        pipeline().fireChannelActive();
      }
    };
  }
}
