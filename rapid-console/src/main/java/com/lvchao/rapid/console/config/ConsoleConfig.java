package com.lvchao.rapid.console.config;

import com.lvchao.rapid.discovery.api.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ServiceLoader;

/**
 * <p>
 * Rapid控制台配置文件
 * </p>
 *
 * @author lvchao
 * @since 2023/2/28 18:11
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RapidConsoleProperties.class)
@ConditionalOnProperty(prefix = RapidConsoleProperties.RAPID_CONSOLE_PREFIX, name = {"registryAddress", "namespace"})
public class ConsoleConfig {

    @Autowired
    private RapidConsoleProperties rapidProperties;

    @Bean
    public RegistryService registryService() {
        ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
        for(RegistryService registryService : serviceLoader) {
            registryService.initialized(rapidProperties.getRegistryAddress());
            return registryService;
        }
        return null;
    }

   /* @Bean
    public ConsumerContainer consumerContainer(RapidConsoleProperties rapidProperties) {
        String kafkaAddress = rapidProperties.getKafkaAddress();
        String groupId = rapidProperties.getGroupId();
        String topicNamePrefix = rapidProperties.getTopicNamePrefix();
        if(StringUtils.isBlank(kafkaAddress)) {
            log.warn("#MainConfig.consumerContainer# kafkaAddress is null, kafkaAddress = {}", kafkaAddress);
            return null;
        }
        if(StringUtils.isBlank(groupId)) {
            log.warn("#MainConfig.consumerContainer# groupId is null, groupId = {}", groupId);
            return null;
        }
        if(StringUtils.isBlank(topicNamePrefix)) {
            log.warn("#MainConfig.consumerContainer# topicNamePrefix is null, topicNamePrefix = {}", topicNamePrefix);
            return null;
        }
        return consumerContainer(rapidProperties);
    }*/
}
