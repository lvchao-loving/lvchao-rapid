package com.lvchao.rapid.chain;

/**
 * <p>
 * ProcessorEntity 抽象链
 * </p>
 *
 * @author lvchao
 * @since 2023/2/27 20:05
 */
public abstract class ProcessorEntityChain<T> extends AbstractLinkedProcessorEntity<T> {

    /**
     * 添加头节点
     * @param entity
     */
    public abstract void addFirst(AbstractLinkedProcessorEntity<T> entity);

    /**
     * 添加尾节点
     * @param entity
     */
    public abstract void addLast(AbstractLinkedProcessorEntity<T> entity);
}
