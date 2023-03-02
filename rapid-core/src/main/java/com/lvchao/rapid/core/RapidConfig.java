package com.lvchao.rapid.core;

import com.lmax.disruptor.*;
import com.lvchao.rapid.common.constants.BasicConst;
import com.lvchao.rapid.common.constants.RapidBufferHelper;
import com.lvchao.rapid.common.util.NetUtils;
import lombok.Data;

/**
 * <p>
 * 网关的通用配置信息类
 * </p>
 *
 * @author lvchao
 * @since 2023/1/15 8:20
 */
@Data
public class RapidConfig {

    /**
     * 网关的默认端口
     */
    private int port = 8888;

    /**
     * 网关服务唯一ID： rapidId  192.168.11.111:8888
     */
    private String rapidId = NetUtils.getLocalIp() + BasicConst.COLON_SEPARATOR + port;

    /**
     * 网关的注册中心地址
     * 说明：etcd默认的地址需要添加 http，默认端口为 2379
     */
    private String registryAddress = "http://127.0.0.1:2379,http://127.0.0.2:2379,http://127.0.0.3:2379";

    /**
     * 网关的命名空间
     */
    private String namespace = "rapid";

    /**
     * 启动环境：dev test prod
     */
    private String env = "dev";

    /**
     * 网关服务器的CPU核数映射的线程数
     */
    private int processThread = Runtime.getRuntime().availableProcessors();

    /**
     * Netty的Boss线程数
     */
    private int eventLoopGroupBossNum = 1;

    /**
     * Netty的Work线程数
     */
    private int eventLoopGroupWorkNum = processThread;

    /**
     * 是否开启 EPOLL
     */
    private boolean useEPoll = true;

    /**
     * 是否开启 Netty 内存分配机制
     */
    private boolean nettyAllocator = true;

    /**
     * http body报文最大大小
     */
    private int maxContentLength = 64 * 1024 * 1024;

    /**
     * dubbo开启连接数数量
     */
    private int dubboConnections = processThread;

    /**
     * 设置响应模式, 默认是单异步模式：CompletableFuture回调处理结果： whenComplete  or  whenCompleteAsync
     * TODO 学习一下 CompletableFuture
     */
    private boolean whenComplete = true;

    /**
     * 网关队列：缓冲模式
     * RapidBufferHelper.FLUSHER;
     */
    private String bufferType = RapidBufferHelper.FLUSHER;

    /**
     * 网关队列：内存队列大小  1024 * 16;
     */
    private int bufferSize = 1024 * 16;

    /**
     * 网关队列：阻塞/等待策略
     * TODO 阻塞策略很多种，可以了解，例如：blocking、spin、yield 等等
     */
    private String waitStrategy = "blocking";


    //	Http Async 参数选项：

    /**
     * 连接超时时间 30 * 1000
     */
    private int httpConnectTimeout = 30 * 1000;

    /**
     * 请求超时时间 30 * 1000
     */
    private int httpRequestTimeout = 30 * 1000;

    /**
     * 客户端请求重试次数 2
     */
    private int httpMaxRequestRetry = 2;

    /**
     * 客户端请求最大连接数 10000
     */
    private int httpMaxConnections = 10000;

    /**
     * 客户端每个地址支持的最大连接数 8000
     */
    private int httpConnectionsPerHost = 8000;

    /**
     * 客户端空闲连接超时时间, 默认60秒
     */
    private int httpPooledConnectionIdleTimeout = 60 * 1000;

    /**
     * 默认请求超时时间 3s
     */
    private long requestTimeout = 3000;

    /**
     * 默认路由转发的慢调用时间 2s
     */
    private long routeTimeout = 2000;

    /**
     * kafka地址
     */
    private String kafkaAddress = "";//"192.168.11.51:9092";

    /**
     * 网关服务指标消息主题
     */
    private String metricTopic = "rapid-metric-topic";

    public WaitStrategy getATureWaitStrategy() {
        switch (waitStrategy) {
            case "blocking":
                return new BlockingWaitStrategy();
            case "busySpin":
                return new BusySpinWaitStrategy();
            case "yielding":
                return new YieldingWaitStrategy();
            case "sleeping":
                return new SleepingWaitStrategy();
            default:
                return new BlockingWaitStrategy();
        }
    }
}
