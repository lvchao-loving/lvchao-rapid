package com.lvchao.rapid.core.netty.processor.filter.post;

import com.lvchao.rapid.common.config.Rule;
import com.lvchao.rapid.common.constants.ProcessorFilterConstants;
import com.lvchao.rapid.common.metric.Metric;
import com.lvchao.rapid.common.metric.MetricType;
import com.lvchao.rapid.common.util.Pair;
import com.lvchao.rapid.common.util.TimeUtil;
import com.lvchao.rapid.core.RapidConfigLoader;
import com.lvchao.rapid.core.context.Context;
import com.lvchao.rapid.core.netty.processor.cache.ConstantCache;
import com.lvchao.rapid.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.lvchao.rapid.core.netty.processor.filter.Filter;
import com.lvchao.rapid.core.netty.processor.filter.FilterConfiguration;
import com.lvchao.rapid.core.netty.processor.filter.ProcessorFilterType;
import com.lvchao.rapid.core.plugin.Plugin;
import com.lvchao.rapid.core.plugin.PluginManager;
import com.lvchao.rapid.core.plugin.metric.kafka.MetricKafkaClientPlugin;
import com.lvchao.rapid.core.rolling.RollingNumber;
import com.lvchao.rapid.core.rolling.RollingNumberEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * <p>
 * 后置过滤器：统计分析
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 20:55
 */
@Filter(
        id = ProcessorFilterConstants.STATISTICS_POST_FILTER_ID,
        name = ProcessorFilterConstants.STATISTICS_POST_FILTER_NAME,
        value = ProcessorFilterType.POST,
        order = ProcessorFilterConstants.STATISTICS_POST_FILTER_ORDER
)
public class StatisticsPostFilter extends AbstractEntryProcessorFilter<StatisticsPostFilter.Config>{

    public static final Integer windowSize = 60 * 1000;

    public static final Integer bucketSize = 60;

    private RollingNumber rollingNumber;

    private Thread consumerThread;

    public StatisticsPostFilter() {
        super(StatisticsPostFilter.Config.class, ConstantCache.STATISTICS_POST_FILTER_CACHE_ID);
        MetricConsumer metricConsumer = new MetricConsumer();
        this.rollingNumber = new RollingNumber(windowSize,
                bucketSize,
                "Rapid-Gateway",
                metricConsumer.getMetricQueue());
        consumerThread = new Thread(metricConsumer);
        // 线程启动标识开始
        metricConsumer.start();
    }

    @Override
    public void entry(Context ctx, Object... args) throws Throwable {
        try {
            StatisticsPostFilter.Config config = (StatisticsPostFilter.Config)args[0];
            if(config.isRollingNumber()) {
                consumerThread.start();
                rollingNumber(ctx, args);
            }
        } finally {
            //	如果走的是最后一个postfilter
            ctx.terminated();
            super.fireNext(ctx, args);
        }
    }

    private void rollingNumber(Context ctx, Object... args) {

        Throwable throwable = ctx.getThrowable();
        if(throwable == null) {
            rollingNumber.increment(RollingNumberEvent.SUCCESS);
        }
        else {
            rollingNumber.increment(RollingNumberEvent.FAILURE);
        }

        //	请求开始的时间
        long SRTime = ctx.getSRTime();
        //	路由的开始时间(route ---> service)
        long RSTime = ctx.getRSTime();
        //	路由的接收请求时间（service --> route）
        long RRTime = ctx.getRRTime();
        //	请求结束（写出请求的时间）
        long SSTime = ctx.getSSTime();

        //	整个生命周期的耗时
        long requestTimeout = SSTime - SRTime;
        long defaultRequestTimeout = RapidConfigLoader.getRapidConfig().getRequestTimeout();
        if(requestTimeout > defaultRequestTimeout) {
            rollingNumber.increment(RollingNumberEvent.REQUEST_TIMEOUT);
        }

        long routeTimeout = RRTime - RSTime;
        long defaultRouteTimeout = RapidConfigLoader.getRapidConfig().getRouteTimeout();
        if(routeTimeout > defaultRouteTimeout) {
            rollingNumber.increment(RollingNumberEvent.ROUTE_TIMEOUT);
        }
    }

    @Getter
    @Setter
    public static class Config extends FilterConfiguration {
        private boolean rollingNumber = true;
    }


    public class MetricConsumer implements Runnable {

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
            while(isRunning) {
                try {
                    Pair<String, Long> pair = metricQueue.take();
                    String key = pair.getObject1();
                    Long value = pair.getObject2();

                    // report 上报
                    Plugin plugin = PluginManager.getPlugin().getPlugin(MetricKafkaClientPlugin.class.getName());
                    if(plugin != null) {
                        MetricKafkaClientPlugin metricKafkaClientPlugin = (MetricKafkaClientPlugin)plugin;

                        HashMap<String, String> tags = new HashMap<>();
                        tags.put(MetricType.KEY, MetricType.STATISTICS);

                        String topic = RapidConfigLoader.getRapidConfig().getMetricTopic();

                        Metric metric = Metric.create(key,
                                value,
                                TimeUtil.currentTimeMillis(),
                                tags,
                                topic,
                                false);
                        metricKafkaClientPlugin.send(metric);
                    }

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