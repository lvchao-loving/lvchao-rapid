package com.lvchao.rapid.common.util;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 线程池工具类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/5 20:02
 */
public class ThreadPoolUtil {
    public static final Executor executor = TtlExecutors.getTtlExecutor(new ThreadPoolExecutor(10, 500, 5, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(1000), new ThreadFactoryBuilder().setNameFormat("delay-deliver-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy()));
}
