package com.lvchao.rapid.chain;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 默认实现的Entity链
 * </p>
 *
 * @author lvchao
 * @since 2023/2/27 20:11
 */
@Slf4j
@Getter
@Setter
public class DefaultProcessorEntityChain<T> extends ProcessorEntityChain<T> {

    /**
     * 创建头节点
     */
    private AbstractLinkedProcessorEntity<T> first = new AbstractLinkedProcessorEntity<T>() {

        @Override
        public boolean check(T t) throws Throwable {
            return true;
        }

        @Override
        public void entry(T t, Object... args) throws Throwable {
            fireNext(t);
        }
    };

    /**
     * 创建尾节点
     */
    private AbstractLinkedProcessorEntity<T> end = first;

    @Override
    public boolean check(T t) throws Throwable {
        return false;
    }

    @Override
    public void entry(T t, Object... args) throws Throwable {
        first.entry(t, args);
    }

    @Override
    public void addFirst(AbstractLinkedProcessorEntity<T> entity) {
        entity.setNext(first.getNext());
        first.setNext(entity);
        if (first == end){
            end = entity;
        }
    }

    @Override
    public void addLast(AbstractLinkedProcessorEntity<T> entity) {
        end.setNext(entity);
        end = entity;
    }
}
