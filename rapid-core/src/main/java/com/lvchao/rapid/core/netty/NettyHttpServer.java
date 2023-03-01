package com.lvchao.rapid.core.netty;

import com.lvchao.rapid.common.exception.RapidStartUpException;
import com.lvchao.rapid.common.util.RemotingUtil;
import com.lvchao.rapid.core.LifeCycle;
import com.lvchao.rapid.core.RapidConfig;
import com.lvchao.rapid.core.netty.handler.NettyHttpServerHandler;
import com.lvchao.rapid.core.netty.handler.NettyServerConnectManagerHandler;
import com.lvchao.rapid.core.netty.processor.NettyProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * <p>
 * 承接所有网络请求的核心类
 * </p>
 *
 * @author lvchao
 * @since 2023/1/30 23:00
 */
@Slf4j
public class NettyHttpServer implements LifeCycle {

    private final RapidConfig rapidConfig;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup eventLoopGroupBoss;

    @Getter
    private EventLoopGroup eventLoopGroupWork;

    private NettyProcessor nettyProcessor;

    /**
     * 说明 final 定义的变量则在 构造方法中必须初始化
     *
     * @param rapidConfig
     */
    public NettyHttpServer(RapidConfig rapidConfig, NettyProcessor nettyProcessor) {
        this.rapidConfig = rapidConfig;
        this.nettyProcessor = nettyProcessor;
        // 初始化NettyHttpServer
        init();
    }

    @Override
    public void init() {
        serverBootstrap = new ServerBootstrap();
        if (useEPoll()) {
            eventLoopGroupBoss = new EpollEventLoopGroup(rapidConfig.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("NettyBossEPoll"));
            eventLoopGroupWork = new EpollEventLoopGroup(rapidConfig.getEventLoopGroupWorkNum(),
                    new DefaultThreadFactory("NettyWorkEPoll"));
        } else {
            eventLoopGroupBoss = new NioEventLoopGroup(rapidConfig.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("NettyBossNio"));
            eventLoopGroupWork = new NioEventLoopGroup(rapidConfig.getEventLoopGroupWorkNum(),
                    new DefaultThreadFactory("NettyWorkNio"));
        }
    }

    /**
     * 启动 netty server
     */
    @Override
    public void start() {
        serverBootstrap
                .group(eventLoopGroupBoss, eventLoopGroupWork)
                .channel(useEPoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                //	sync + accept = backlog
                .option(ChannelOption.SO_BACKLOG, 1024)
                //	tcp端口重绑定
                .option(ChannelOption.SO_REUSEADDR, true)
                //  如果在两小时内没有数据通信的时候，TCP会自动发送一个活动探测数据报文
                .option(ChannelOption.SO_KEEPALIVE, false)
                //	该参数的作用就是禁用Nagle算法，使用小数据传输时合并
                .childOption(ChannelOption.TCP_NODELAY, true)
                //	设置发送数据缓冲区大小
                .childOption(ChannelOption.SO_SNDBUF, 65535)
                //	设置接收数据缓冲区大小
                .childOption(ChannelOption.SO_RCVBUF, 65535)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(
                                // HttpServerCodec 为 http 编解码处理器
                                new HttpServerCodec(),
                                // HttpObjectAggregator 是 Netty 提供的 HTTP 消息聚合器，通过它可以把 HttpMessage 和 HttpContent 聚合成一个 FullHttpRequest 或者 FullHttpResponse(取决于是处理请求还是响应）
                                new HttpObjectAggregator(rapidConfig.getMaxContentLength()),
                                // 异常重试
                                new HttpServerExpectContinueHandler(),
                                // 服务端连接信息
                                new NettyServerConnectManagerHandler(),
                                // Netty核心处理handler
                                new NettyHttpServerHandler(nettyProcessor)
                        );
                    }
                })
                .localAddress(new InetSocketAddress(rapidConfig.getPort()));

        if (rapidConfig.isNettyAllocator()) {
            serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }

        try {
            // 启动
            serverBootstrap.bind().sync();
            log.info("< ============= Rapid Server StartUp On Port: " + rapidConfig.getPort() + " ================ >");
        } catch (Throwable e) {
            log.debug("Rapid Server StartUp ERROR", e);
            throw new RapidStartUpException();
        }
    }

    /**
     * 关闭资源
     */
    @Override
    public void shutdown() {
        if (eventLoopGroupBoss != null) {
            eventLoopGroupBoss.shutdownGracefully();
        }
        if (eventLoopGroupWork != null) {
            eventLoopGroupWork.shutdownGracefully();
        }
    }

    /**
     * 判断是否支持 EPoll
     *
     * @return
     */
    private boolean useEPoll() {
        return rapidConfig.isUseEPoll() && RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }
}
