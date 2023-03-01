package com.lvchao.rapid.core.netty.processor;

import com.lvchao.rapid.core.context.HttpRequestWrapper;

/**
 * <p>
 * 处理Netty核心逻辑的执行器接口定义
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 11:49
 */
public interface NettyProcessor {

    /**
     * 核心执行方法
     *
     * @param httpRequestWrapper
     * @throws Exception
     */
    void process(HttpRequestWrapper httpRequestWrapper) throws Exception;

    /**
     * 执行器启动方法
     */
    default void start(){}

    /**
     * 执行资源 释放/关闭方法
     */
    default void shutdown(){}
}
