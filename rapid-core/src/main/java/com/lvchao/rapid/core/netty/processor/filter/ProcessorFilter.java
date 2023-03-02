package com.lvchao.rapid.core.netty.processor.filter;

/**
 * <p>
 * 执行过滤器的接口操作
 * </p>
 * com.lvchao.rapid.core.netty.processor.filter.ProcessorFilter
 * com.lvchao.rapid.core.netty.processor.filter
 * @author lvchao
 * @since 2023/2/7 9:02
 */
public interface ProcessorFilter<T> {

    /**
     * 过滤器是否执行的校验方法
     * @param t
     * @return
     * @throws Throwable
     */
    boolean check(T t) throws Throwable;

    /**
     * 真正执行过滤器的方法
     * @param t
     * @param args
     * @throws Throwable
     */
    void entry(T t, Object... args) throws Throwable;

    /**
     * 触发下一个过滤器执行
     * @param t
     * @param args
     * @throws Throwable
     */
    void fireNext(T t, Object... args) throws Throwable;

    /**
     * 对象传输的方法
     * TODO： 为什么会有这个方法，目前的考虑是 在进入 entry()方法的时候需要将解析上下文中的参数并且设置到 args 的位置，所以参数转化；其次在阅读
     *        sentinel 源码的时候也用到了请求链的方式，并且也存在 transformEntry()
     * @param t
     * @param args
     * @throws Throwable
     */
    void transformEntry(T t, Object... args) throws Throwable;


    /**
     * 生命周期方法：过滤器初始化的方法，如果子类有需求则进行覆盖
     * @throws Exception
     */
    default void init() throws Exception {

    }

    /**
     * 生命周期方法：过滤器销毁的方法，如果子类有需求则进行覆盖
     * @throws Exception
     */
    default void destroy() throws Exception {

    }

    /**
     * 生命周期方法：过滤器刷新的方法，如果子类有需求则进行覆盖
     * @throws Exception
     */
    default void refresh() throws Exception {

    }
}
