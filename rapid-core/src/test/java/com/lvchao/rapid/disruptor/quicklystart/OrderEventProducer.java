package com.lvchao.rapid.disruptor.quicklystart;

import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;

/**
 * <p>
 * 生产者对象
 * </p>
 *
 * @author lvchao
 * @since 2023/2/2 8:59
 */
public class OrderEventProducer {

    private RingBuffer<OrderEvent> ringBuffer;

    public OrderEventProducer(RingBuffer<OrderEvent> ringBuffer){
        this.ringBuffer = ringBuffer;
    }

    /**
     *  生产者添加数据
     * @param name
     */
    public void putData(String name){
        // 先获取下一个可用的序号
        long sequence = ringBuffer.next();

        try {
            //	通过可用的序号获取对应下标的数据OrderEvent
            OrderEvent event = ringBuffer.get(sequence);
            //	重新设置内容
            event.setName(name);
        } finally {
            //	publish
            ringBuffer.publish(sequence);
        }
    }
}
