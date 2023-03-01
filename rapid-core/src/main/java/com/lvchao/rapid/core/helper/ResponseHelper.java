package com.lvchao.rapid.core.helper;

import com.lvchao.rapid.common.constants.BasicConst;
import com.lvchao.rapid.common.enums.ResponseCode;
import com.lvchao.rapid.common.util.TimeUtil;
import com.lvchao.rapid.core.context.Context;
import com.lvchao.rapid.core.context.RapidResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * 响应的辅助类
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 16:30
 */
public class ResponseHelper {

    /**
     * 获取响应对象：JSON格式，并且是HttpResponseStatus.INTERNAL_SERVER_ERROR类型的反参
     * @param responseCode
     * @return
     */
    public static FullHttpResponse getHttpResponseJSONServerError(ResponseCode responseCode){
        return getHttpResponse(responseCode,HttpHeaderValues.APPLICATION_JSON,HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 获取响应对象：JSON格式，并且是HttpResponseStatus.INTERNAL_SERVER_ERROR类型的反参
     * @param responseCode
     * @return
     */
    public static FullHttpResponse getHttpResponse(ResponseCode responseCode, AsciiString applicationType, HttpResponseStatus httpResponseStatus){
        // 首先通过ResponseCode，获取自定义的RapidResponse对象。
        RapidResponse rapidResponse = RapidResponse.buildRapidResponse(responseCode);

        // 封装成 netty 处理的 FullHttpResponse 对象
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus,
                Unpooled.wrappedBuffer(rapidResponse.getContent().getBytes()));
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, applicationType + ";charset=utf-8");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());

        return httpResponse;
    }

    /**
     * 通过上下文对象和RapidResponse对象 构建FullHttpResponse
     * @return
     */
    private static FullHttpResponse getHttpResponse(Context context, RapidResponse rapidResponse){
        // 构建 content 属性
        ByteBuf content;
        if (Objects.nonNull(rapidResponse.getFutureResponse())){
            content = Unpooled.wrappedBuffer(rapidResponse.getFutureResponse().getResponseBodyAsByteBuffer());
        }else if (rapidResponse.getContent() != null){
            content = Unpooled.wrappedBuffer(rapidResponse.getContent().getBytes());
        }else {
            content = Unpooled.wrappedBuffer(BasicConst.BLANK_SEPARATOR_1.getBytes());
        }

        // 创建一个返回对象
        DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(rapidResponse.getFutureResponse().getStatusCode()), content);


        // 添加请求头
        if (Objects.isNull(rapidResponse.getFutureResponse())){
            defaultFullHttpResponse.headers().add(rapidResponse.getResponseHeaders());
            defaultFullHttpResponse.headers().add(rapidResponse.getExtraResponseHeaders());
            defaultFullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH,defaultFullHttpResponse.content().readableBytes());
        }else {
            defaultFullHttpResponse.headers().add(rapidResponse.getFutureResponse().getHeaders());
            defaultFullHttpResponse.headers().add(rapidResponse.getExtraResponseHeaders());
        }

        return defaultFullHttpResponse;
    }

    /**
     * 写回响应信息
     * @param rapidContext
     */
    public static void writeResponse(Context rapidContext){
        // 设置服务写回时间
        rapidContext.setSSTime(TimeUtil.currentTimeMillis());
        // 释放资源
        rapidContext.releaseRequest();

        if (rapidContext.isWrittened()){
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(rapidContext, (RapidResponse) rapidContext.getResponse());
            if (rapidContext.isKeepAlive()){
                // 长连接
                httpResponse.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
                rapidContext.getNettyCtx().writeAndFlush(httpResponse);
            }else {
                // 短链接
                // 写出数据并关闭连接
                rapidContext.getNettyCtx().writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
            }
            // 将上下文标记为 complete
            rapidContext.completed();
        }else if (rapidContext.isCompleted()){
            // 执行上下文的回调函数
            rapidContext.invokeCompletedCallback();
        }
    }
}
