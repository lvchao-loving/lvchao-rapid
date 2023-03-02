package com.lvchao.rapid;

import com.lvchao.rapid.common.util.RemotingUtil;
import com.lvchao.rapid.core.RapidConfigLoader;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.asynchttpclient.*;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 测试 AsyncHttpClient
 * </p>
 *
 * @author lvchao
 * @since 2023/3/1 20:59
 */
public class Test_AsyncHttpClient {

    @Test
    public void test00() throws ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = initAsyncHttpClient();
        AsyncHttpClientHelper asyncHttpClientHelper = new AsyncHttpClientHelper(asyncHttpClient);

        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setMethod("GET");
        requestBuilder.setUrl("http://localhost:8083/testGet");
        Request request = requestBuilder.build();

        CompletableFuture<Response> completableFuture = asyncHttpClientHelper.executeRequest(request);
        System.out.println(completableFuture.get());
    }

    /**
     * 单异步请求
     */
    @Test
    public void test01() throws ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = initAsyncHttpClient();
        AsyncHttpClientHelper asyncHttpClientHelper = new AsyncHttpClientHelper(asyncHttpClient);

        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setMethod("GET");
        requestBuilder.setUrl("http://localhost:8083/testGet");
        Request request = requestBuilder.build();

        CompletableFuture<Response> completableFuture = asyncHttpClientHelper.executeRequest(request);
        // 执行【单异步模式】
        completableFuture.whenComplete((response, throwable) -> {
           System.err.println(response);
           System.err.println(throwable);
        });
        TimeUnit.SECONDS.sleep(4);
    }

    /**
     * 双异步测试
     */
    @Test
    public void test02() throws InterruptedException {
        AsyncHttpClient asyncHttpClient = initAsyncHttpClient();
        AsyncHttpClientHelper asyncHttpClientHelper = new AsyncHttpClientHelper(asyncHttpClient);

        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setMethod("GET");
        requestBuilder.setUrl("http://localhost:8083/testGet");
        Request request = requestBuilder.build();

        CompletableFuture<Response> completableFuture = asyncHttpClientHelper.executeRequest(request);
        // 执行【双异步模式】
        completableFuture.whenCompleteAsync((response, throwable) -> {
            System.err.println(response);
            System.err.println(throwable);
        });
        TimeUnit.SECONDS.sleep(4);
    }



    /**
     * 初始化
     * @return
     */
    public AsyncHttpClient initAsyncHttpClient(){
        // 服务器的CPU核数映射的线程数
        int processThread = Runtime.getRuntime().availableProcessors();

        // 判断是否使用 epoll 模式
        boolean userEPoll = RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();

        // 创建使用的线程池
        EventLoopGroup eventLoopGroupWork;
        if (userEPoll) {
            eventLoopGroupWork = new EpollEventLoopGroup(processThread, new DefaultThreadFactory("WorkEPoll"));
        } else {
            eventLoopGroupWork = new NioEventLoopGroup(processThread, new DefaultThreadFactory("WorkNio"));
        }

        DefaultAsyncHttpClientConfig.Builder clientBuilder = new DefaultAsyncHttpClientConfig.Builder()
                .setFollowRedirect(false)
                .setEventLoopGroup(eventLoopGroupWork) // work 线程数这里启动的是一个线程
                .setConnectTimeout(30 * 1000) // 连接超时时间 30 * 1000
                .setRequestTimeout(30 * 1000) // 请求超时时间 30 * 1000
                .setMaxRequestRetry(2) // 客户端请求重试次数 2
                .setAllocator(PooledByteBufAllocator.DEFAULT) // 内存分配 池化的直接内存
                .setCompressionEnforced(true) // 强制压缩 true
                .setMaxConnections(10000) // 客户端请求最大连接数 10000
                .setMaxConnectionsPerHost(8000) // 客户端每个地址支持的最大连接数 8000 TODO 需要学习一下这个参数
                .setPooledConnectionIdleTimeout(60 * 1000); // 客户端空闲连接超时时间, 默认60秒

        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient(clientBuilder.build());

        return asyncHttpClient;
    }

    /**
     * 异步请求工具类主要是封装了请求方式
     */
    public static class AsyncHttpClientHelper{

        private AsyncHttpClient asyncHttpClient;

        public AsyncHttpClientHelper(AsyncHttpClient asyncHttpClient){
            this.asyncHttpClient = asyncHttpClient;
        }

        public CompletableFuture<Response> executeRequest(Request request) {
            ListenableFuture<Response> future = asyncHttpClient.executeRequest(request);
            return future.toCompletableFuture();
        }

        public <T> CompletableFuture<T> executeRequest(Request request, AsyncHandler<T> handler) {
            ListenableFuture<T> future = asyncHttpClient.executeRequest(request, handler);
            return future.toCompletableFuture();
        }
    }
}
