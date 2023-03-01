package com.lvchao.rapid.console.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Rapid控制台属性配置文件
 * </p>
 *
 * @author lvchao
 * @since 2023/2/28 18:04
 */
@Data
@ConfigurationProperties(prefix = RapidConsoleProperties.RAPID_CONSOLE_PREFIX)
public class RapidConsoleProperties {

    public static final String RAPID_CONSOLE_PREFIX = "rapid.console";

    private String registryAddress;

    private String namespace;

    private String kafkaAddress;

    private String groupId;

    private String topicNamePrefix;

    private int ConsumerNum = 1;
}
