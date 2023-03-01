package com.lvchao.rapid.core.netty.processor.filter;

import com.lvchao.rapid.core.context.Context;

import java.util.List;

/**
 * <p>
 * 过滤器工厂接口
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 20:33
 */
public interface ProcessorFilterFactory {
    
    /**
     * 根据过滤器类型，添加一组过滤器，用于构建过滤器链
     *
     * @param filterType
     * @param filters
     * @throws Exception
     */
    void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters) throws Exception;


    /**
     * 正常情况下执行过滤器链条
     *
     * @param ctx
     * @throws Exception
     */
    void doFilterChain(Context ctx) throws Exception;


    /**
     * 错误、异常情况下执行该过滤器链条
     *
     * @param ctx
     * @throws Exception
     */
    void doErrorFilterChain(Context ctx) throws Exception;

    /**
     * 获取指定类类型的过滤器
     *
     * @param t
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T getFilter(Class<T> t) throws Exception;

    /**
     * 获取指定ID的过滤器
     *
     * @param filterId
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T getFilter(String filterId) throws Exception;
}
