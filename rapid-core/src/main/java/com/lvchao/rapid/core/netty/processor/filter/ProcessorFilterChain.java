package com.lvchao.rapid.core.netty.processor.filter;

/**
 * <p>
 * 链表的抽象接口：添加一些简单的操作方法
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 10:26
 */
public abstract class ProcessorFilterChain<T> extends AbstractLinkedProcessorFilter<T>{

    /**
     * 在链表的头部添加元素
     * @param filter
     */
    public abstract void addFirst(AbstractLinkedProcessorFilter<T> filter);

    /**
     * 在链表尾部添加
     * @param filter
     */
    public abstract void addLast(AbstractLinkedProcessorFilter<T> filter);
}
