package com.lvchao.rapid.core.netty.handler;

import com.lvchao.rapid.core.context.HttpRequestWrapper;
import com.lvchao.rapid.core.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Netty核心处理handler
 * </p>
 *
 * 1. ChannelInboundHandlerAdapter 和 SimpleChannelInboundHandler 的区别：ChannelInboundHandlerAdapter接口不会自定释放buffer对象，
 * SimpleChannelInboundHandler会自动释放buffer对象。
 * 2. 这里使用 ChannelInboundHandlerAdapter 接口的原因是，当前Handler并没有处理当前详细，只是暂存所以不能释放buffer对象。
 *
 * @author lvchao
 * @since 2023/1/31 14:13
 */
@Slf4j
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    private NettyProcessor nettyProcessor;

    public NettyHttpServerHandler(NettyProcessor nettyProcessor){
        this.nettyProcessor = nettyProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest){

            FullHttpRequest request = (FullHttpRequest) msg;
            HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper();
            httpRequestWrapper.setCtx(ctx);
            httpRequestWrapper.setFullHttpRequest(request);

            nettyProcessor.process(httpRequestWrapper);
        }else {
            //	never go this way, ignore
            log.error("#NettyHttpServerHandler.channelRead# message type is not httpRequest: {}", msg);
            boolean release = ReferenceCountUtil.release(msg);
            if(!release) {
                log.error("#NettyHttpServerHandler.channelRead# release fail 资源释放失败");
            }
        }
    }
}
