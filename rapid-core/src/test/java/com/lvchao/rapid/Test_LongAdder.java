package com.lvchao.rapid;

import com.lvchao.rapid.core.rolling.RollingNumberEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.LongAdder;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/25 20:28
 */
@Slf4j
public class Test_LongAdder {

    public static void main(String[] args) {
        test02();
    }

    private static void test02(){
        LongAdder[] longAdders = new LongAdder[2];
        longAdders[0] = new LongAdder();
        longAdders[0].increment();
        longAdders[0].increment();
        System.out.println(longAdders[0].sum());
    }

    private static void test01(){
        int length = RollingNumberEvent.values().length;
        log.info("length = {}",length);
        LongAdder[] longAdders = new LongAdder[length];
        for (RollingNumberEvent type : RollingNumberEvent.values()) {
            log.info("type={}###type.ordinal={}###type.isCounter={}",type,type.ordinal(),type.isCounter());
            if (type.isCounter()) {
                longAdders[type.ordinal()] = new LongAdder();
            }
        }
        System.out.println("***************************");
        /*for (RollingNumberEvent type : RollingNumberEvent.values()) {
            LongAdder longAdder = longAdders[0];
            long sum = longAdders[type.ordinal()].sum();
            log.info("----{}-----",sum);
        }*/
        LongAdder longAdder = longAdders[0];
        longAdder.increment();
        longAdder.increment();
        longAdder.increment();
        longAdder.increment();
        longAdder.increment();
        System.out.println(longAdder.sum());
    }
}
