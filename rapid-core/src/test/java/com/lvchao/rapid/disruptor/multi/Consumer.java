package com.lvchao.rapid.disruptor.multi;

import com.lmax.disruptor.WorkHandler;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/2 13:37
 */
public class Consumer implements WorkHandler<Order> {

    private String consumerId;

    private AtomicInteger atomicInteger = new AtomicInteger(0);


    private Random random = new Random();

    public Consumer(String consumerId){
        this.consumerId = consumerId;
    }

    @Override
    public void onEvent(Order event) throws Exception {
        Thread.sleep(1 * random.nextInt(5));
        System.err.println("当前消费者: " + this.consumerId + ", 消费信息ID: " + event.getId());
        atomicInteger.incrementAndGet();
    }

}
