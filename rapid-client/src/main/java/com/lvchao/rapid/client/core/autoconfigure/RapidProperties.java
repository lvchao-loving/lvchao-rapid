package com.lvchao.rapid.client.core.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * 配置类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/11 11:55
 */
@Data
@ConfigurationProperties(prefix = RapidProperties.RAPID_PREFIX)
public class RapidProperties {

    public static final String RAPID_PREFIX = "rapid";

    /**
     * etcd注册中心地址，单机默认值
     */
    private String registryAddress = "http://127.0.0.1:3279";

    /**
     * etcd注册命名空间，默认值
     */
    private String namespace = RAPID_PREFIX;

    /**
     * 环境属性，默认值
     */
    private String env = "dev";
}
