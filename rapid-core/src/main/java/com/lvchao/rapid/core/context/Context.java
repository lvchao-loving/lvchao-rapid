package com.lvchao.rapid.core.context;

import com.lvchao.rapid.common.config.Rule;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

/**
 * <p>
 * 网关上下文接口定义
 * </p>
 *
 * @author lvchao
 * @since 2023/2/5 20:31
 */
public interface Context {

    /**
     * 一个请求正在执行过程中
     */
    int RUNNING = -1;

    /**
     * 写回响应标记, 标记当前Context/请求需要写回
     */
    int WRITTEN = 0;

    /**
     * 当写回成功后, 设置该标记：ctx.writeAndFlush(response);
     */
    int COMPLETED = 1;

    /**
     * 表示整个网关请求完毕, 彻底结束
     */
    int TERMINATED = 2;

    /**
     * 设置上下文状态为正常运行状态
     */
    void runned();

    /**
     * 设置上下文状态为标记写回
     */
    void writtened();

    /**
     * 设置上下文状态为写回结束
     */
    void completed();

    /**
     * 设置上下文状态为最终结束
     */
    void terminated();

    /**
     * 判断当前状态是否为 RUNNING
     * @return
     */
    boolean isRunning();

    /**
     * 判断当前状态是否为 WRITTEN
     * @return
     */
    boolean isWrittened();

    /**
     * 判断当前状态是否为 COMPLETED
     * @return
     */
    boolean isCompleted();

    /**
     * 判断当前状态是否为 TERMINATED
     * @return
     */
    boolean isTerminated();

    /**
     * 获取请求转换协议
     * @return
     */
    String getProtocol();

    /**
     * 获取规则
     * @return
     */
    Rule getRule();

    /**
     * 获取请求对象
     * @return
     */
    Object getRequest();

    /**
     * 获取响应对象
     * @return
     */
    Object getResponse();

    /**
     * 设置响应对象
     * @param response
     */
    void setResponse(Object response);

    /**
     * 设置异常信息
     * @param throwable
     */
    void setThrowable(Throwable throwable);

    /**
     * 获取异常
     * @return
     */
    Throwable getThrowable();

    /**
     * 获取上下文参数
     * @param key
     * @param <T>
     * @return
     */
    <T> T getAttribute(AttributeKey<T> key);

    /**
     * 保存上下文属性信息
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    <T> T putAttribute(AttributeKey<T> key, T value);

    /**
     * 获取Netty的上下文对象
     * @return
     */
    ChannelHandlerContext getNettyCtx();

    /**
     * 是否保持连接
     * @return
     */
    boolean isKeepAlive();

    /**
     * 释放请求资源的方法
     */
    void releaseRequest();

    /**
     * 写回接收回调函数设置
     * @param consumer
     */
    void completedCallback(Consumer<Context> consumer);

    /**
     * 回调函数执行
     */
    void invokeCompletedCallback();


    /**
     * 	SR(Server[Rapid-Core] Received):	网关服务器接收到网络请求
     * 	SS(Server[Rapid-Core] Send):		网关服务器写回请求
     * 	RS(Route Send):						网关客户端发送请求
     * 	RR(Route Received): 				网关客户端收到请求
     */

    long getSRTime();

    void setSRTime(long sRTime);

    long getSSTime();

    void setSSTime(long sSTime);

    long getRSTime();

    void setRSTime(long rSTime);

    long getRRTime();

    void setRRTime(long rRTime);
}
