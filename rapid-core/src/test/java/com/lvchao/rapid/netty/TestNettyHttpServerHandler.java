package com.lvchao.rapid.netty;

import com.alibaba.fastjson.JSON;
import com.lvchao.rapid.common.constants.BasicConst;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 *  * 1. ChannelInboundHandlerAdapter 和 SimpleChannelInboundHandler 的区别：ChannelInboundHandlerAdapter接口不会自定释放buffer对象，
 *  * SimpleChannelInboundHandler会自动释放buffer对象。
 *  * 2. 这里使用 ChannelInboundHandlerAdapter 接口的原因是，当前Handler并没有处理当前详细，只是暂存所以不能释放buffer对象。
 * </p>
 *
 * @author lvchao
 * @since 2023/3/2 20:59
 */
@Slf4j
public class TestNettyHttpServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest){
            FullHttpRequest request = (FullHttpRequest) msg;
            String contentType = HttpUtil.getMimeType(request) == null ? null : HttpUtil.getMimeType(request).toString();
            Charset charset = HttpUtil.getCharset(request, StandardCharsets.UTF_8);
            log.info("接收请求参数：{}", JSON.toJSONString(request));
            // 目前参数乱码
            ByteBuf content = Unpooled.wrappedBuffer("lvchao is good person.".getBytes());
            // 创建一个返回对象
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(200), content);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());

            // 标记是否是长连接
            boolean isKeepLive = HttpUtil.isKeepAlive(request);
            if (isKeepLive){
                response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(response);
            }else {
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        }else {
            log.error("当前请求不是http请求忽略：{}", msg);
            boolean release = ReferenceCountUtil.release(msg);
            if(!release) {
                log.error("释放资源失败");
            }
        }
    }
}
