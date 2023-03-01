package com.lvchao.rapid.chain;

/**
 * <p>
 * 链式调用顶层接口，抛出 Throwable 异常
 * </p>
 *
 * @author lvchao
 * @since 2023/2/27 15:16
 */
public interface ProcessorEntity<T> {
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
}
