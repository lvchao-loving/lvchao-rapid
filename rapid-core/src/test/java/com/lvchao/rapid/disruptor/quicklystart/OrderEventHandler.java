package com.lvchao.rapid.disruptor.quicklystart;

import com.lmax.disruptor.EventHandler;
import org.apache.commons.lang3.RandomUtils;

/**
 * <p>
 * 事件
 * </p>
 *
 * @author lvchao
 * @since 2023/2/1 16:14
 */
public class OrderEventHandler implements EventHandler<OrderEvent> {
    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws Exception {
        Thread.sleep(RandomUtils.nextInt(1000, 2000));
        System.err.println("消费者消费：" + event.getName());
    }
}
