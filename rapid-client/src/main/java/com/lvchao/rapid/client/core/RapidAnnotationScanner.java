package com.lvchao.rapid.client.core;

import com.lvchao.rapid.client.RapidInvoker;
import com.lvchao.rapid.client.RapidProtocol;
import com.lvchao.rapid.client.RapidService;
import com.lvchao.rapid.client.support.dubbo.DubboConstants;
import com.lvchao.rapid.common.config.DubboServiceInvoker;
import com.lvchao.rapid.common.config.HttpServiceInvoker;
import com.lvchao.rapid.common.config.ServiceDefinition;
import com.lvchao.rapid.common.config.ServiceInvoker;
import com.lvchao.rapid.common.constants.BasicConst;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.ServiceBean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 注解扫描类, 用于扫描所有的用户定义的 @RapidService 和 @RapidInvoker
 * </p>
 *
 * @author lvchao
 * @since 2023/2/11 9:56
 */
public class RapidAnnotationScanner {

    private RapidAnnotationScanner(){
    }

    // TODO 为什么用这种单例模式
    private static class SingletonHolder{
        static final RapidAnnotationScanner INSTANCE = new RapidAnnotationScanner();
    }

    public static RapidAnnotationScanner getInstance(){
        return SingletonHolder.INSTANCE;
    }

    /**
     * 扫描传入的Bean对象，最终返回一个ServiceDefinition
     * @param bean
     * @param args 额外的参数选项：注册dubbo时需要使用ServiceBean
     * @return
     */
    public synchronized ServiceDefinition scanBuilder(Object bean, Object... args) {

        Class<?> clazz = bean.getClass();
        // 判断注解是否存在
        boolean isPresent = clazz.isAnnotationPresent(RapidService.class);
        if(isPresent) {
            // 获取注解信息
            RapidService rapidService = clazz.getAnnotation(RapidService.class);
            String serviceId = rapidService.serviceId();
            RapidProtocol protocol = rapidService.protocol();
            String patternPath = rapidService.patternPath();
            String version = rapidService.version();

            ServiceDefinition serviceDefinition = new ServiceDefinition();
            Map<String /* invokerPath */, ServiceInvoker> invokerMap = new HashMap<String, ServiceInvoker>();

            Method[] methods = clazz.getMethods();
            if(methods != null && methods.length > 0) {
                for(Method method : methods) {
                    RapidInvoker rapidInvoker = method.getAnnotation(RapidInvoker.class);
                    if(rapidInvoker == null) {
                        continue;
                    }
                    String path = rapidInvoker.path();

                    switch (protocol) {
                        case HTTP:
                            HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path, bean, method);
                            invokerMap.put(path, httpServiceInvoker);
                            break;
                        case DUBBO:
                            ServiceBean<?> serviceBean = (ServiceBean<?>)args[0];
                            DubboServiceInvoker dubboServiceInvoker = createDubboServiceInvoker(path, serviceBean, method);
                            //	dubbo version reset for serviceDefinition version
                            String dubboVersion = dubboServiceInvoker.getVersion();
                            if(!StringUtils.isBlank(dubboVersion)) {
                                version = dubboVersion;
                            }
                            invokerMap.put(path, dubboServiceInvoker);
                            break;
                        default:
                            break;
                    }
                }
            }
            //	设置属性
            serviceDefinition.setUniqueId(serviceId + BasicConst.COLON_SEPARATOR + version);
            serviceDefinition.setServiceId(serviceId);
            serviceDefinition.setVersion(version);
            serviceDefinition.setProtocol(protocol.getCode());
            serviceDefinition.setPatternPath(patternPath);
            serviceDefinition.setEnable(true);
            serviceDefinition.setInvokerMap(invokerMap);
            return serviceDefinition;
        }

        return null;
    }

    /**
     * 构建HttpServiceInvoker对象
     * @param path
     * @param bean
     * @param method
     * @return
     */
    private HttpServiceInvoker createHttpServiceInvoker(String path, Object bean, Method method) {
        HttpServiceInvoker httpServiceInvoker = new HttpServiceInvoker();
        httpServiceInvoker.setInvokerPath(path);
        return httpServiceInvoker;
    }

    /**
     * 构建DubboServiceInvoker对象
     * @param path
     * @param serviceBean
     * @param method
     * @return
     */
    private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
        DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
        dubboServiceInvoker.setInvokerPath(path);

        String methodName = method.getName();
        String registerAddress = serviceBean.getRegistry().getAddress();
        String interfaceClass = serviceBean.getInterface();

        dubboServiceInvoker.setRegisterAddress(registerAddress);
        dubboServiceInvoker.setMethodName(methodName);
        dubboServiceInvoker.setInterfaceClass(interfaceClass);

        String[] parameterTypes = new String[method.getParameterCount()];
        Class<?>[] classes = method.getParameterTypes();
        for(int i = 0; i < classes.length; i ++) {
            parameterTypes[i] = classes[i].getName();
        }
        dubboServiceInvoker.setParameterTypes(parameterTypes);

        Integer seriveTimeout = serviceBean.getTimeout();
        if(seriveTimeout == null || seriveTimeout.intValue() == 0) {
            ProviderConfig providerConfig = serviceBean.getProvider();
            if(providerConfig != null) {
                Integer providerTimeout = providerConfig.getTimeout();
                if(providerTimeout == null || providerTimeout.intValue() == 0) {
                    seriveTimeout = DubboConstants.DUBBO_TIMEOUT;
                } else {
                    seriveTimeout = providerTimeout;
                }
            }
        }
        dubboServiceInvoker.setTimeout(seriveTimeout);

        String dubboVersion = serviceBean.getVersion();
        dubboServiceInvoker.setVersion(dubboVersion);

        return dubboServiceInvoker;
    }
}
