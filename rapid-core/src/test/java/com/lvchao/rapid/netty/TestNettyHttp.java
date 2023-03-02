package com.lvchao.rapid.netty;

import io.netty.bootstrap.ServerBootstrap;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/3/2 21:16
 */
public class TestNettyHttp {

    @Test
    public void test01() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        TestNettyHttpServer nettyHttpServer = new TestNettyHttpServer();
        nettyHttpServer.start();

        ServerBootstrap serverBootstrap = nettyHttpServer.getServerBootstrap();
        countDownLatch.await();
    }
}
