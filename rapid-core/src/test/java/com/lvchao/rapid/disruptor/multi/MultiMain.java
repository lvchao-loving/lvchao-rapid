package com.lvchao.rapid.disruptor.multi;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/2 15:10
 */
public class MultiMain {
    public static void main(String[] args) {
        // 创建 RingBuffer
        RingBuffer<Order> ringBuffer = RingBuffer.create(ProducerType.MULTI, () -> new Order(), 1024 * 1024, new YieldingWaitStrategy());
        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

        // 构建消费者集合
        List<Consumer> consumerList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            consumerList.add(new Consumer("consumer" + i));
        }

        // 构建消费者工作池
      //  new WorkerPool<Order>(ringBuffer,sequenceBarrier,new EventExceptionHandler(),);


    }

    /**
     * 事件异常处理器
     */
    static class EventExceptionHandler implements ExceptionHandler<Order> {

        public void handleEventException(Throwable ex, long sequence, Order event) {
        }

        public void handleOnStartException(Throwable ex) {
        }

        public void handleOnShutdownException(Throwable ex) {
        }
    }
}
