package com.lvchao.rapid.core.netty;

import com.lvchao.rapid.core.LifeCycle;
import com.lvchao.rapid.core.RapidConfig;
import com.lvchao.rapid.core.helper.AsyncHttpHelper;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

/**
 * <p>
 * HTTP客户端启动类，主要用于下游服务的请求转发
 * </p>
 *
 * @author lvchao
 * @since 2023/1/30 23:00
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {

    private RapidConfig rapidConfig;

    private AsyncHttpClient asyncHttpClient;

    private EventLoopGroup eventLoopGroupWork;

    private DefaultAsyncHttpClientConfig.Builder clientBuilder;

    public NettyHttpClient(RapidConfig rapidConfig,EventLoopGroup eventLoopGroupWork){
        this.rapidConfig = rapidConfig;
        this.eventLoopGroupWork = eventLoopGroupWork;
        // 在构造函数调用初始化方法
        init();
    }

    @Override
    public void init() {
        clientBuilder = new DefaultAsyncHttpClientConfig.Builder()
                .setFollowRedirect(false)
                .setEventLoopGroup(eventLoopGroupWork) // work 线程数这里启动的是一个线程
                .setConnectTimeout(rapidConfig.getHttpConnectTimeout()) // 连接超时时间 30 * 1000
                .setRequestTimeout(rapidConfig.getHttpRequestTimeout()) // 请求超时时间 30 * 1000
                .setMaxRequestRetry(rapidConfig.getHttpMaxRequestRetry()) // 客户端请求重试次数 2
                .setAllocator(PooledByteBufAllocator.DEFAULT) // 内存分配 池化的直接内存
                .setCompressionEnforced(true) // 强制压缩 true
                .setMaxConnections(rapidConfig.getHttpMaxConnections()) // 客户端请求最大连接数 10000
                .setMaxConnectionsPerHost(rapidConfig.getHttpConnectionsPerHost()) // 客户端每个地址支持的最大连接数 8000 TODO 需要学习一下这个参数
                .setPooledConnectionIdleTimeout(rapidConfig.getHttpPooledConnectionIdleTimeout()); // 客户端空闲连接超时时间, 默认60秒
    }

    @Override
    public void start() {
        this.asyncHttpClient = new DefaultAsyncHttpClient(clientBuilder.build());
        AsyncHttpHelper.getInstance().initialized(asyncHttpClient);
    }

    @Override
    public void shutdown() {
        if(asyncHttpClient != null) {
            try {
                this.asyncHttpClient.close();
            } catch (Throwable t) {
                // ignore
                log.error("#NettyHttpClient.shutdown# shutdown error", t);
            }
        }
    }
}
