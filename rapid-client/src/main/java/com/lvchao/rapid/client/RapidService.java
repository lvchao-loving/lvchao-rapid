package com.lvchao.rapid.client;

import java.lang.annotation.*;

/**
 * <p>
 * 服务定义注解类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/10 17:49
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RapidService {

    /**
     * 服务的唯一ID
     * @return
     */
    String serviceId();

    /**
     * 对应服务的版本号
     * @return
     */
    String version() default "1.0.0";

    /**
     * 协议类型
     * @return
     */
    RapidProtocol protocol();

    /**
     * ANT路径匹配表达式配置
     * @return
     */
    String patternPath();

}
