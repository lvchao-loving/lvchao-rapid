package com.lvchao.rapid.core.netty.processor;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lvchao.rapid.common.concurrent.queue.mpmc.MpmcBlockingQueue;
import com.lvchao.rapid.common.enums.ResponseCode;
import com.lvchao.rapid.core.RapidConfig;
import com.lvchao.rapid.core.context.HttpRequestWrapper;
import com.lvchao.rapid.core.helper.ResponseHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * mpmc的核心实现处理器, 最终我们还是要使用NettyCoreProcessor
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:09
 */
@Slf4j
public class NettyMpmcProcessor implements NettyProcessor {

    private RapidConfig rapidConfig;

    private NettyCoreProcessor nettyCoreProcessor;


    private MpmcBlockingQueue<HttpRequestWrapper> mpmcBlockingQueue;

    private boolean usedExecutorPool;

    private ExecutorService executorService;

    /**
     * 是否正在运行，volatile 可以理解为 操作系统的一个命令
     */
    private volatile boolean isRunning = false;

    /**
     * 单线程模式
     */
    private Thread consumerProcessorThread;

    /**
     * 有参构造方法
     *
     * @param rapidConfig
     * @param nettyCoreProcessor
     * @param usedExecutorPool
     */
    public NettyMpmcProcessor(RapidConfig rapidConfig, NettyCoreProcessor nettyCoreProcessor, boolean usedExecutorPool) {
        // 通用的两个参数设置
        this.rapidConfig = rapidConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;

        // mpmc队列参数
        this.mpmcBlockingQueue = new MpmcBlockingQueue<>(rapidConfig.getBufferSize());
        this.usedExecutorPool = usedExecutorPool;
    }

    @Override
    public void start() {
        this.isRunning = true;
        this.nettyCoreProcessor.start();
        if (usedExecutorPool) {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(rapidConfig.getProcessThread(), rapidConfig.getProcessThread(), 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(1000 * 1000), new ThreadFactoryBuilder().setNameFormat("NettyMpmcProcessor-Thread-%d").build(),
                    new ThreadPoolExecutor.CallerRunsPolicy());
            this.executorService = TtlExecutors.getTtlExecutorService(threadPoolExecutor);
            for (int i = 0; i < rapidConfig.getProcessThread(); i++) {
                this.executorService.submit(new ConsumerProcessor());
            }
        } else {
            this.consumerProcessorThread = new Thread(new ConsumerProcessor());
            this.consumerProcessorThread.start();
        }
    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) throws Exception {
        mpmcBlockingQueue.put(httpRequestWrapper);
    }

    @Override
    public void shutdown() {
        this.isRunning = false;
        this.nettyCoreProcessor.shutdown();
        if (usedExecutorPool) {
            this.executorService.shutdown();
        }
    }

    /**
     * 消费者核心实现类
     */
    private class ConsumerProcessor implements Runnable {

        @Override
        public void run() {
            // 当启动 或 队列不为空时，执行消费消息
            while (isRunning || !mpmcBlockingQueue.isEmpty()) {
                System.err.println("执行到了这里....");
                HttpRequestWrapper event = null;
                try {
                    event = mpmcBlockingQueue.take();
                    nettyCoreProcessor.process(event);
                } catch (Throwable t) {
                    if (event != null) {
                        HttpRequest request = event.getFullHttpRequest();
                        ChannelHandlerContext ctx = event.getCtx();
                        try {
                            log.error("#ConsumerProcessor# onException 请求处理失败, request: {}. errorMessage: {}", request, t.getMessage(), t);
                            //	首先构建响应对象
                            FullHttpResponse fullHttpResponse = ResponseHelper.getHttpResponseJSONServerError(ResponseCode.INTERNAL_ERROR);
                            //	判断是否保持连接
                            if (!HttpUtil.isKeepAlive(request)) {
                                ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                            } else {
                                //	如果保持连接, 则需要设置一下响应头：key: CONNECTION,  value: KEEP_ALIVE
                                fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                                ctx.writeAndFlush(fullHttpResponse);
                            }
                        } catch (Exception e) {
                            //	ignore
                            log.error("#ConsumerProcessor# onException 请求回写失败, request: {}. errorMessage: {}", request, e.getMessage(), e);
                        }
                    } else {
                        log.error("#ConsumerProcessor# onException event is Empty errorMessage: {}", t.getMessage(), t);
                    }
                }
            }
        }

    }
}
