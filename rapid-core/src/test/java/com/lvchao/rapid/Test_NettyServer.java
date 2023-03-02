package com.lvchao.rapid;

import com.lvchao.rapid.common.util.RemotingUtil;
import com.lvchao.rapid.core.netty.handler.NettyHttpServerHandler;
import com.lvchao.rapid.core.netty.handler.NettyServerConnectManagerHandler;
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
import org.asynchttpclient.*;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 测试 NettyServer
 * </p>
 *
 * @author lvchao
 * @since 2023/3/1 22:00
 */
public class Test_NettyServer {

    @Test
    public void test00() {
    }

    /**
     * 初始化
     * @return
     */
    public AsyncHttpClient initNettyServer() {
        // 服务器的CPU核数映射的线程数
        int processThread = Runtime.getRuntime().availableProcessors();
        // Netty的Boss线程数
        int eventLoopGroupBossNum = 1;

        // 判断是否使用 epoll 模式
        boolean userEPoll = RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        EventLoopGroup eventLoopGroupWork;
        EventLoopGroup eventLoopGroupBoss;
        if (userEPoll) {
            eventLoopGroupBoss = new EpollEventLoopGroup(eventLoopGroupBossNum, new DefaultThreadFactory("NettyBossEPoll"));
            eventLoopGroupWork = new EpollEventLoopGroup(processThread, new DefaultThreadFactory("NettyWorkEPoll"));
        } else {
            eventLoopGroupBoss = new EpollEventLoopGroup(eventLoopGroupBossNum, new DefaultThreadFactory("NettyBossEPoll"));
            eventLoopGroupWork = new NioEventLoopGroup(processThread, new DefaultThreadFactory("NettyWorkNio"));
        }

        // 参考一下 sentinel NettyTransportServer 类的创建
     /*   serverBootstrap.group(eventLoopGroupBoss, eventLoopGroupWork)
                .channel(userEPoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class) // 创建 SocketChannel 对象
                .option(ChannelOption.SO_BACKLOG, 1024)         // sync + accept = backlog TODO 需要学习
                .option(ChannelOption.SO_REUSEADDR, true)       // tcp端口重绑定
                .option(ChannelOption.SO_KEEPALIVE, false)      // 如果在两小时内没有数据通信的时候，TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.TCP_NODELAY, true)   // 该参数的作用就是禁用Nagle算法，使用小数据传输时合并
                .childOption(ChannelOption.SO_SNDBUF, 65535)    // 设置发送数据缓冲区大小
                .childOption(ChannelOption.SO_RCVBUF, 65535)    // 设置接收数据缓冲区大小
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(
                                // HttpServerCodec 为 http 编解码处理器
                                new HttpServerCodec(),
                                // HttpObjectAggregator 是 Netty 提供的 HTTP 消息聚合器，通过它可以把 HttpMessage 和 HttpContent 聚合成一个 FullHttpRequest 或者 FullHttpResponse(取决于是处理请求还是响应）
                                new HttpObjectAggregator(64 * 1024 * 1024), // http body报文最大大小
                                // 异常重试
                                new HttpServerExpectContinueHandler(),
                                // 服务端连接信息
                                new NettyServerConnectManagerHandler(),
                                // Netty核心处理handler
                                new NettyHttpServerHandler(nettyProcessor)
                        );
                    }
                })
                .localAddress(new InetSocketAddress(rapidConfig.getPort()));*/
        return null;

    }
}
