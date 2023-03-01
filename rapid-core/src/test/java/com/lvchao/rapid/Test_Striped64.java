package com.lvchao.rapid;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/24 16:46
 */
public class Test_Striped64 {

    public static void main(String[] args) {
        test01();
        System.err.println("------------");
        test02();
    }

    public static void test01(){
        AtomicLong atomicLong = new AtomicLong(0);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        CountDownLatch countDownLatch = new CountDownLatch(10);
        long l = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    int i = 10000000;
                    while (i > 0) {
                        atomicLong.incrementAndGet();
                        i--;
                    }
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            long l2 = System.currentTimeMillis();
            System.out.println((l2 - l) + "ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(atomicLong.get());
    }

    /**
     * 使用 LongAdder
     */
    public static void test02() {
        LongAdder longAdder = new LongAdder();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        CountDownLatch countDownLatch = new CountDownLatch(10);
        long l = System.currentTimeMillis();
        for (int i=0;i<10;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    int i=10000000;
                    while(i>0){
                        longAdder.add(1);
                        i--;
                    }
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            long l2 = System.currentTimeMillis();
            System.out.println((l2-l)+"ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(longAdder.sum());
    }

}
