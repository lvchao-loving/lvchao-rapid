package com.lvchao.rapid.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 * <p>
 * HttpRequestWrapper
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 11:53
 */
@Data
public class HttpRequestWrapper {
    private FullHttpRequest fullHttpRequest;
    private ChannelHandlerContext ctx;
}
