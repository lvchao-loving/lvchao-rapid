package com.lvchao.rapid.core.helper;

import org.asynchttpclient.*;

import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 异步的http辅助类：NettyHttpClient
 * </p>
 *
 * @author lvchao
 * @since 2023/2/1 9:32
 */
public class AsyncHttpHelper {

    private AsyncHttpHelper() {
    }

    private static final class SingletonHolder {
        private static final AsyncHttpHelper INSTANCE = new AsyncHttpHelper();
    }

    public static AsyncHttpHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private AsyncHttpClient asyncHttpClient;

    public void initialized(AsyncHttpClient asyncHttpClient) {
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
