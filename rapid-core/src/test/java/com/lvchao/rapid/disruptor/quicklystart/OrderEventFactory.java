package com.lvchao.rapid.disruptor.quicklystart;

import com.lmax.disruptor.EventFactory;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/1 16:57
 */
public class OrderEventFactory implements EventFactory<OrderEvent> {

    @Override
    public OrderEvent newInstance() {
        return new OrderEvent();
    }
}
