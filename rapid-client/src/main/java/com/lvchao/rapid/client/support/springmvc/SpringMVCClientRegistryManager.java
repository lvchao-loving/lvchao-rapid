package com.lvchao.rapid.client.support.springmvc;

import com.lvchao.rapid.client.core.AbstractClientRegistryManager;
import com.lvchao.rapid.client.core.RapidAnnotationScanner;
import com.lvchao.rapid.client.core.autoconfigure.RapidProperties;
import com.lvchao.rapid.common.config.ServiceDefinition;
import com.lvchao.rapid.common.config.ServiceInstance;
import com.lvchao.rapid.common.constants.BasicConst;
import com.lvchao.rapid.common.constants.RapidConst;
import com.lvchao.rapid.common.util.NetUtils;
import com.lvchao.rapid.common.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Http请求的客户端注册管理器
 * </p>
 *
 * @author lvchao
 * @since 2023/2/11 12:08
 */
@Slf4j
public class SpringMVCClientRegistryManager extends AbstractClientRegistryManager implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private ServerProperties serverProperties;

    private static final Set<Object> uniqueBeanSet = new HashSet<>();

    public SpringMVCClientRegistryManager(RapidProperties rapidProperties) {
        super(rapidProperties);
    }

    @PostConstruct
    private void init() {
        if(!ObjectUtils.allNotNull(serverProperties, serverProperties.getPort())) {
            return;
        }
        //	判断如果当前验证属性都不为空 就进行初始化
        whetherStart = true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    // TODO 确定 onApplicationEvent 的作用
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if(!whetherStart) {
            return;
        }

        if(event instanceof WebServerInitializedEvent ||
                event instanceof ServletWebServerInitializedEvent) {
            try {
                registrySpringMVC();
            } catch (Exception e) {
                log.error("#SpringMVCClientRegisteryManager# registerySpringMVC error", e);
            }
        } else if(event instanceof ApplicationStartedEvent){
            //	START:::
            System.err.println("******************************************");
            System.err.println("**        Rapid SpringMVC Started       **");
            System.err.println("******************************************");
        }
    }

    /**
     * 解析SpringMvc的事件，进行注册
     * @throws Exception
     */
    private void registrySpringMVC() throws Exception {
        Map<String, RequestMappingHandlerMapping> allRequestMappings = BeanFactoryUtils
                .beansOfTypeIncludingAncestors(applicationContext,
                        RequestMappingHandlerMapping.class,
                        true,
                        false);

        for(RequestMappingHandlerMapping handlerMapping : allRequestMappings.values()) {
            Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();
            for(Map.Entry<RequestMappingInfo, HandlerMethod> me : map.entrySet()) {
                HandlerMethod handlerMethod = me.getValue();
                Class<?> clazz = handlerMethod.getBeanType();
                Object bean = applicationContext.getBean(clazz);
                //	如果当前Bean对象已经加载则不需要做任何事
                if(uniqueBeanSet.add(bean)) {
                    ServiceDefinition serviceDefinition = RapidAnnotationScanner.getInstance().scanBuilder(bean);
                    if(serviceDefinition != null) {
                        //	设置环境
                        serviceDefinition.setEnvType(getEnv());
                        //	注册服务定义
                        registerServiceDefinition(serviceDefinition);

                        //	注册服务实例：
                        ServiceInstance serviceInstance = new ServiceInstance();
                        String localIp = NetUtils.getLocalIp();
                        int port = serverProperties.getPort();
                        String serviceInstanceId = localIp + BasicConst.COLON_SEPARATOR + port;
                        String address = serviceInstanceId;
                        String uniqueId = serviceDefinition.getUniqueId();
                        String version = serviceDefinition.getVersion();

                        serviceInstance.setServiceInstanceId(serviceInstanceId);
                        serviceInstance.setUniqueId(uniqueId);
                        serviceInstance.setAddress(address);
                        serviceInstance.setWeight(RapidConst.DEFAULT_WEIGHT);
                        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
                        serviceInstance.setVersion(version);

                        registerServiceInstance(serviceInstance);
                    }
                }
            }
        }
    }

}
