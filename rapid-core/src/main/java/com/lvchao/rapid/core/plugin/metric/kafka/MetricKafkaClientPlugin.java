package com.lvchao.rapid.core.plugin.metric.kafka;

import com.lvchao.rapid.common.metric.TimeSeries;
import com.lvchao.rapid.core.RapidConfig;
import com.lvchao.rapid.core.RapidConfigLoader;
import com.lvchao.rapid.core.plugin.Plugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * MetricKafkaClientPlugin
 * </p>
 *
 * @author lvchao
 * @since 2023/2/22 17:46
 */
@Slf4j
public class MetricKafkaClientPlugin implements Plugin {

    private MetricKafkaClientCollector metricKafkaClientCollector;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * kafka地址
     */
    private String kafkaAddress;

    public MetricKafkaClientPlugin(){}

    @Override
    public boolean check() {
        kafkaAddress = RapidConfigLoader.getRapidConfig().getKafkaAddress();
        if (StringUtils.isNotBlank(kafkaAddress)){
            return true;
        }
        return false;
    }

    @Override
    public void init() {
        if (check()){
            // 初始化 kafka
            metricKafkaClientCollector = new MetricKafkaClientCollector(kafkaAddress);
            metricKafkaClientCollector.start();
            initialized.compareAndSet(false,true);
        }
    }

    @Override
    public void destroy() {
        if (checkInit()){
            metricKafkaClientCollector.shutdown();
            initialized.compareAndSet(false,true);
        }
    }

    @Override
    public Plugin getPlugin(String pluginName) {
        if (checkInit() && (MetricKafkaClientPlugin.class.getName().equals(pluginName))){
            return this;
        }
        throw new RuntimeException("#MetricKafkaClientPlugin# pluginName: " + pluginName + " is no matched");
    }

    private boolean checkInit(){
        return initialized.get() && Objects.nonNull(metricKafkaClientCollector);
    }

    /**
     * 单条发送
     * @param metric
     * @param <T>
     */
    public <T extends TimeSeries> void send(T metric){
        try{
            if (checkInit()){
                metricKafkaClientCollector.sendAsync(metric.getDestination(), metric,((metadata, exception) -> {
                    if (exception != null){
                        log.error("#MetricKafkaClientSender# callback exception, metric: {}, {}", metric.toString(), exception.getMessage());
                    }
                }));
            }
        }catch (Exception e){
            log.error("#MetricKafkaClientSender# send exception, metric: {}", metric.toString(), e);
        }
    }

    /**
     * 批量发送
     * @param metricList
     * @param <T>
     */
    public <T extends TimeSeries> void sendBatch(List<T> metricList){
        for (T metric: metricList) {
            send(metric);
        }
    }
}
