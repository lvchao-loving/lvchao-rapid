package com.lvchao.rapid.core;

import com.lvchao.rapid.common.constants.RapidBufferHelper;
import com.lvchao.rapid.core.netty.NettyHttpClient;
import com.lvchao.rapid.core.netty.NettyHttpServer;
import com.lvchao.rapid.core.netty.processor.NettyBatchEventProcessor;
import com.lvchao.rapid.core.netty.processor.NettyCoreProcessor;
import com.lvchao.rapid.core.netty.processor.NettyMpmcProcessor;
import com.lvchao.rapid.core.netty.processor.NettyProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 主流程的容器类
 * </p>
 *
 * @author lvchao
 * @since 2023/1/30 22:59
 */
@Slf4j
public class RapidContainer implements LifeCycle {

    /**
     * 核心配置类
     */
    private final RapidConfig rapidConfig;

    /**
     * 接收http请求的server
     */
    private NettyHttpServer nettyHttpServer;

    /**
     * http转发的核心类
     */
    private NettyHttpClient nettyHttpClient;

    /**
     * 核心处理器
     */
    private NettyProcessor nettyProcessor;

    public RapidContainer(RapidConfig rapidConfig) {
        this.rapidConfig = rapidConfig;
        init();
    }

    @Override
    public void init() {
        // 1. 构建核心处理器
        NettyCoreProcessor nettyCoreProcessor = new NettyCoreProcessor();
        // 2. 是否开启缓存
        String bufferType = rapidConfig.getBufferType();
        if(RapidBufferHelper.isFlusher(bufferType)){
            nettyProcessor = new NettyBatchEventProcessor(rapidConfig,nettyCoreProcessor);
        }else if (RapidBufferHelper.isMpmc(bufferType)){
            nettyProcessor = new NettyMpmcProcessor(rapidConfig,nettyCoreProcessor,true);
        }else {
            nettyProcessor = nettyCoreProcessor;
        }
        // 3. 创建 NettyhttpServer
        nettyHttpServer = new NettyHttpServer(rapidConfig, nettyProcessor);
        // 4. 创建NettyHttpClient
        nettyHttpClient = new NettyHttpClient(rapidConfig, nettyHttpServer.getEventLoopGroupWork());
    }

    @Override
    public void start() {
        nettyProcessor.start();
        nettyHttpServer.start();
        nettyHttpClient.start();
        log.info("RapidContainer started !");
    }

    @Override
    public void shutdown() {
        nettyProcessor.shutdown();
        nettyHttpServer.shutdown();
        nettyHttpClient.shutdown();
    }
}
