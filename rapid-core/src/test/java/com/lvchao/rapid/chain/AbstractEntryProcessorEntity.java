package com.lvchao.rapid.chain;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/27 15:47
 */
public abstract class AbstractEntryProcessorEntity<T> extends AbstractLinkedProcessorEntity<T>{

    @Override
    public void entry(T t, Object... args) throws Throwable {

    }

}
