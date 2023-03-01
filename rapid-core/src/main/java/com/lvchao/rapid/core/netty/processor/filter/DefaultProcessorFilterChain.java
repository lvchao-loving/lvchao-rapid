package com.lvchao.rapid.core.netty.processor.filter;

import com.lvchao.rapid.core.context.Context;

/**
 * <p>
 * 最终的链表实现类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 10:25
 */
public class DefaultProcessorFilterChain extends ProcessorFilterChain<Context>{

    private final String id;

    public DefaultProcessorFilterChain(String id){
        this.id = id;
    }

    /**
     * 	虚拟头结点：dummyHead
     */
    private AbstractLinkedProcessorFilter<Context> first = new AbstractLinkedProcessorFilter<Context>() {

        @Override
        public void entry(Context ctx, Object... args) throws Throwable {
            // 头节点不做任何事情，直接执行下一个节点
            super.fireNext(ctx, args);
        }

        @Override
        public boolean check(Context ctx) throws Throwable {
            return true;
        }

    };

    /**
     * 	尾节点
     */
    private AbstractLinkedProcessorFilter<Context> end = first;

    /**
     * 头节点添加
     * @param filter
     */
    @Override
    public void addFirst(AbstractLinkedProcessorFilter<Context> filter) {
        filter.setNext(first.getNext());
        first.setNext(filter);
        if(end == first) {
            end = filter;
        }
    }

    /**
     * 尾节点添加
     * @param filter
     */
    @Override
    public void addLast(AbstractLinkedProcessorFilter<Context> filter) {
        end.setNext(filter);
        end = filter;
    }

    @Override
    public void setNext(AbstractLinkedProcessorFilter<Context> filter) {
        addLast(filter);
    }

    @Override
    public AbstractLinkedProcessorFilter<Context> getNext() {
        return first.getNext();
    }


    @Override
    public boolean check(Context ctx) throws Throwable {
        return true;
    }

    @Override
    public void entry(Context ctx, Object... args) throws Throwable {
        first.transformEntry(ctx, args);
    }

    public String getId() {
        return id;
    }

}
