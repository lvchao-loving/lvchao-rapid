package com.lvchao.rapid.disruptor.quicklystart;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadFactory;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/1 16:59
 */
@Slf4j
public class DSMain {
    public static void main(String[] args) {
        System.out.println("test");
        int ringBufferSize = 1024 * 1024;
        OrderEventFactory orderEventFactory = new OrderEventFactory();
        Disruptor<OrderEvent> disruptor = new Disruptor<>(orderEventFactory,
                ringBufferSize,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("ds-thread");
                        return thread;
                    }
                },
                ProducerType.SINGLE, new BlockingWaitStrategy());

        disruptor.handleEventsWith(new OrderEventHandler());

        disruptor.start();

        RingBuffer<OrderEvent> ringBuffer = disruptor.getRingBuffer();

        OrderEventProducer producer = new OrderEventProducer(ringBuffer);

        String name = "张三";

        for(long i = 0 ; i < 100; i++) {
            producer.putData(name + "---" + i);
        }

        disruptor.shutdown();
    }
}
