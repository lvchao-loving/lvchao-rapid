package com.lvchao.rapid.core.netty.handler;

import com.lvchao.rapid.common.util.RemotingHelper;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * <p>
 * 连接管理器
 *
 * 说明：
 *      1. ChannelHandler 实现，表示ChannelInboundHandler和ChannelOutboundHandler的组合。如果ChannelHandler实现需要拦截操作和状态更新，这是一个很好的起点。
 *      2. supper.method 的思路是实现对handler的向下传递不中断
 *
 * TODO netty 的 disconnect 和 close 方法的需求
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 14:07
 */
@Slf4j
public class NettyServerConnectManagerHandler extends ChannelDuplexHandler {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelRegistered {}", remoteAddr);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelUnregistered {}", remoteAddr);
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelActive {}", remoteAddr);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelInactive {}", remoteAddr);
        super.channelInactive(ctx);
    }


    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: disconnect {}", remoteAddr);
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: close {}", remoteAddr);
        super.close(ctx, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: connect {}", remoteAddr);
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    /**
     * Gets called if an user event was triggered.
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            /**
             * public enum IdleState {
             *   // No data was received for a while.
             *      READER_IDLE,
             *   //  No data was sent for a while.
             *      WRITER_IDLE,
             *   //  No data was either received or sent for a while.
             *      ALL_IDLE
             * }
             * 说明当前事件处于空闲状态
             */
            if (event.state().equals(IdleState.ALL_IDLE)){
                final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                log.warn("NETTY SERVER PIPLINE: userEventTriggered: IDLE {}", remoteAddr);
                ctx.channel().close();
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    /**
     * A Channel received an Throwable in one of its inbound operations.
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.warn("NETTY SERVER PIPLINE: remoteAddr： {}, exceptionCaught {}", remoteAddr, cause);
        // 关闭 channel
        ctx.channel().close();
    }
}
