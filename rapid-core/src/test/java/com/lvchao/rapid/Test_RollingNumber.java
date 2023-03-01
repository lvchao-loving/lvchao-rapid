package com.lvchao.rapid;

import com.lvchao.rapid.common.metric.Metric;
import com.lvchao.rapid.common.metric.MetricType;
import com.lvchao.rapid.common.util.Pair;
import com.lvchao.rapid.common.util.TimeUtil;
import com.lvchao.rapid.core.RapidConfigLoader;
import com.lvchao.rapid.core.plugin.Plugin;
import com.lvchao.rapid.core.plugin.PluginManager;
import com.lvchao.rapid.core.plugin.metric.kafka.MetricKafkaClientPlugin;
import com.lvchao.rapid.core.rolling.RollingNumber;
import com.lvchao.rapid.core.rolling.RollingNumberEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/26 9:25
 */
@Slf4j
public class Test_RollingNumber {

    @Test
    public void test03(){
        Long a = Long.MAX_VALUE;
        System.out.println(a);
    }

    @Test
    public void test02() throws InterruptedException {
        RollingNumber.BucketCircularArray buckets = new RollingNumber.BucketCircularArray(6);
        while (true){
           // TimeUnit.SECONDS.sleep(10);
            buckets.addLast(new RollingNumber.Bucket(System.currentTimeMillis()));
        }
    }

    @Test
    public void test01() throws InterruptedException {
        // 滑动窗口的时间为 6秒，并且每个窗口的时间为 1秒
        Integer windowSize = 1 * 1000;
        Integer bucketSize = 4;

        MetricConsumer metricConsumer = new MetricConsumer();
        Thread consumerThread = new Thread(metricConsumer);
        RollingNumber rollingNumber = new RollingNumber(windowSize,bucketSize,"test-uniqueKey",metricConsumer.getMetricQueue());
        metricConsumer.start();
        consumerThread.start();

        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while (true){
                    TimeUnit.SECONDS.sleep(1);
                    System.err.println(rollingNumber.getCumulativeSum(RollingNumberEvent.SUCCESS));
                }

            }
        }).start();

        /*   while (true){*/
            /*int i = new Random().nextInt(50);
            TimeUnit.MILLISECONDS.sleep(i);*/
            for (int i = 0; i < 10; i++) {
                new Thread(()->{
                    while (true){
                        rollingNumber.increment(RollingNumberEvent.SUCCESS);
                    }
                }).start();
            }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
      /*  }
*/


       /* // 每隔一秒钟像 阻塞队列中添加 一个数据
        AtomicLong a = new AtomicLong(1);
        while (true){
            TimeUnit.SECONDS.sleep(1);
            metricConsumer.getMetricQueue().add(new Pair<String, Long>("吕超",a .getAndIncrement()));
        }*/
    }

    public static class MetricConsumer implements Runnable {

        private ArrayBlockingQueue<Pair<String, Long>> metricQueue = new ArrayBlockingQueue<>(65535);

        private volatile boolean isRunning = false;

        public void start() {
            isRunning = true;
        }

        public void shutdown() {
            isRunning = false;
        }

        @Override
        public void run() {
            System.err.println("当前isRunning状态：" + isRunning);
            while(isRunning) {
                try {
                    Pair<String, Long> pair = metricQueue.take();
                    String key = pair.getObject1();
                    Long value = pair.getObject2();

                 //   System.err.println(key + "    " + value);
                } catch (InterruptedException e) {
                    //	ignore
                }
            }
        }

        public ArrayBlockingQueue<Pair<String, Long>> getMetricQueue() {
            return metricQueue;
        }

        public void setMetricQueue(ArrayBlockingQueue<Pair<String, Long>> metricQueue) {
            this.metricQueue = metricQueue;
        }

        public boolean isRunning() {
            return isRunning;
        }

    }
}
