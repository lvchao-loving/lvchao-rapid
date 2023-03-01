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
                .setEventLoopGroup(eventLoopGroupWork)
                .setConnectTimeout(rapidConfig.getHttpConnectTimeout())
                .setRequestTimeout(rapidConfig.getHttpRequestTimeout())
                .setMaxRequestRetry(rapidConfig.getHttpMaxRequestRetry())
                .setAllocator(PooledByteBufAllocator.DEFAULT)
                .setCompressionEnforced(true)
                .setMaxConnections(rapidConfig.getHttpMaxConnections())
                .setMaxConnectionsPerHost(rapidConfig.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(rapidConfig.getHttpPooledConnectionIdleTimeout());
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
