package com.lvchao.rapid.client.core.autoconfigure;

import com.lvchao.rapid.client.support.dubbo.Dubbo27ClientRegisterManager;
import com.lvchao.rapid.client.support.springmvc.SpringMVCClientRegistryManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;

/**
 * <p>
 * SpringBoot自动装配加载类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/11 12:00
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RapidProperties.class)
// 只有当 RapidProperties 的配置文件中 存在 "registerAddress" 和 "namespace" 属性时，当前配置类才起作用
@ConditionalOnProperty(prefix = RapidProperties.RAPID_PREFIX, name = {"registryAddress", "namespace"})
public class RapidClientAutoConfiguration {

    public RapidClientAutoConfiguration() {
        log.info("执行 RapidClientAutoConfiguration 方法的无参构造方法！");
    }

    @Autowired
    private RapidProperties rapidProperties;

    @Bean
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean(SpringMVCClientRegistryManager.class)
    public SpringMVCClientRegistryManager springMVCClientRegistryManager() throws Throwable {
        return new SpringMVCClientRegistryManager(rapidProperties);
    }

    @Bean
    @ConditionalOnClass({ServiceBean.class})
    @ConditionalOnMissingBean(Dubbo27ClientRegisterManager.class)
    public Dubbo27ClientRegisterManager dubbo27ClientRegisterManager() {
        return new Dubbo27ClientRegisterManager(rapidProperties);
    }

}
