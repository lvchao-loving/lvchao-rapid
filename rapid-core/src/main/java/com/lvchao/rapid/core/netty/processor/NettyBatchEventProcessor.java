package com.lvchao.rapid.core.netty.processor;

import com.lmax.disruptor.dsl.ProducerType;
import com.lvchao.rapid.common.concurrent.queue.flusher.ParallelFlusher;
import com.lvchao.rapid.common.enums.ResponseCode;
import com.lvchao.rapid.core.RapidConfig;
import com.lvchao.rapid.core.context.HttpRequestWrapper;
import com.lvchao.rapid.core.helper.ResponseHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * flusher缓冲队列的核心实现, 最终调用的方法还是要回归到NettyCoreProcessor
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 22:09
 */
@Slf4j
public class NettyBatchEventProcessor implements NettyProcessor {

    private static final String THREAD_NAME_PREFIX = "rapid-flusher-";

    private RapidConfig rapidConfig;

    private NettyCoreProcessor nettyCoreProcessor;

    private ParallelFlusher<HttpRequestWrapper> parallelFlusher;

    public NettyBatchEventProcessor(RapidConfig rapidConfig, NettyCoreProcessor nettyCoreProcessor) {
        this.rapidConfig = rapidConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
        // 创建 ParallelFlusher 的 builder 对象
        ParallelFlusher.Builder<HttpRequestWrapper> builder = new ParallelFlusher.Builder<HttpRequestWrapper>()
                .setBufferSize(rapidConfig.getBufferSize())
                .setThreads(rapidConfig.getProcessThread())
                .setProducerType(ProducerType.MULTI)
                .setNamePrefix(THREAD_NAME_PREFIX)
                .setWaitStrategy(rapidConfig.getATureWaitStrategy())
                // TODO 添加 BatchEventProcessorListener 的作用
                .setEventListener(new BatchEventProcessorListener());
        this.parallelFlusher = builder.build();
    }

    @Override
    public void start() {
        this.nettyCoreProcessor.start();
        this.parallelFlusher.start();
    }

    @Override
    public void shutdown() {
        this.nettyCoreProcessor.shutdown();
        this.parallelFlusher.shutdown();
    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) throws Exception {
        parallelFlusher.add(httpRequestWrapper);
    }

    /**
     * 监听事件的处理核心逻辑
     */
    private class BatchEventProcessorListener implements ParallelFlusher.EventListener<HttpRequestWrapper> {

        @Override
        public void onEvent(HttpRequestWrapper event) throws Exception {
            nettyCoreProcessor.process(event);
        }

        @Override
        public void onException(Throwable t, long sequence, HttpRequestWrapper event) {
            FullHttpRequest request = event.getFullHttpRequest();
            ChannelHandlerContext ctx = event.getCtx();
            log.error("#BatchEventProcessorListener# onException 请求处理失败, request: {}. errorMessage: {}", request, t.getMessage(), t);
            // 构建响应对象
            try {
                FullHttpResponse response = ResponseHelper.getHttpResponseJSONServerError(ResponseCode.INTERNAL_ERROR);

                // 判断是否保持连接
                if (HttpUtil.isKeepAlive(request)) {
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    ctx.writeAndFlush(response);
                }
            } catch (Exception e) {
                //	ignore
                log.error("#BatchEventProcessorListener# onException 请求回写失败, request: {}. errorMessage: {}", request, e.getMessage(), e);
            }
        }
    }
}
