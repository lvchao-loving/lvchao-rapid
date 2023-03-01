package com.lvchao.rapid.core.discovery;

import com.alibaba.fastjson.JSONObject;
import com.lvchao.rapid.common.config.*;
import com.lvchao.rapid.common.constants.BasicConst;
import com.lvchao.rapid.common.constants.RapidProtocol;
import com.lvchao.rapid.common.util.FastJsonConvertUtil;
import com.lvchao.rapid.common.util.Pair;
import com.lvchao.rapid.common.util.ServiceLoader;
import com.lvchao.rapid.core.RapidConfig;
import com.lvchao.rapid.discovery.api.Notify;
import com.lvchao.rapid.discovery.api.Registry;
import com.lvchao.rapid.discovery.api.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * <p>
 * 网关服务的注册中心管理类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/21 15:21
 */
@Slf4j
public class RegistryManager {

    /**
     * 私有构造函数
     */
    private RegistryManager() {
    }

    /**
     * 单例模式
     */
    private static class SingletonHolder {
        private static final RegistryManager INSTANCE = new RegistryManager();
    }

    public static RegistryManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 配置信息
     */
    private RapidConfig rapidConfig;

    /**
     * 注册中心接口
     */
    private RegistryService registryService;

    /**
     * superPath 例如：
     */
    private String superPath;

    /**
     * servicesPath 例如：
     */
    private String servicesPath;

    /**
     * instancesPath 例如：
     */
    private String instancesPath;

    /**
     * rulesPath 例如：
     */
    private String rulesPath;

    /**
     * gatewaysPath 例如：
     */
    private String gatewaysPath;

    /**
     * 线程同步变量
     */
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public void initialized(RapidConfig rapidConfig) throws Exception {
        this.rapidConfig = rapidConfig;
        //	1.路径的设置
        // superPath = /rapid-dev
        superPath = Registry.PATH + rapidConfig.getNamespace() + BasicConst.BAR_SEPARATOR + rapidConfig.getEnv();
        // servicesPath = /rapid-dev/services
        servicesPath = superPath + Registry.SERVICE_PREFIX;
        // instancesPath = /rapid-dev/instances
        instancesPath = superPath + Registry.INSTANCE_PREFIX;
        // rulesPath = /rapid-dev/rules
        rulesPath = superPath + Registry.RULE_PREFIX;
        // gatewaysPath = /rapid-dev/gateway
        gatewaysPath = superPath + Registry.GATEWAY_PREFIX;

        //	2.初始化加载注册中心对象
        ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
        for (RegistryService registryService : serviceLoader) {
            registryService.initialized(rapidConfig.getRegistryAddress());
            this.registryService = registryService;
        }

        //	3.注册监听
        this.registryService.addWatcherListeners(superPath, new ServiceListener());

        //	4.订阅服务（主要就是加载 etcd 中的数据到 jvm 中）
        subscribeService();

        //	5.注册自身服务
        RegistryServer registryServer = new RegistryServer(registryService);
        registryServer.registerSelf();

    }

    /**
     * 订阅服务的方法：拉取Etcd注册中心的所有需要使用的元数据信息，解析并放置到缓存中
     * <p>
     * 数据结构：
     * /rapid-dev
     * /services
     * /hello:1.0.0
     * /say:1.0.0
     * /instances
     * /hello:1.0.0/192.168.11.100:1234
     * /hello:1.0.0/192.168.11.101:4321
     */
    private synchronized void subscribeService() {
        log.info("#RegistryManager#subscribeService  ------------ 	服务订阅开始 	---------------");

        try {
            // 1.加载服务定义和服务实例的集合：获取  servicesPath = /rapid-dev/services 下面所有的列表
            List<Pair<String, String>> definitionList = this.registryService.getListByPrefixKey(servicesPath);

            for (Pair<String, String> definition : definitionList) {
                String definitionPath = definition.getObject1();
                String definitionJson = definition.getObject2();

                // 把当前获取的跟目录进行排除
                if (definitionPath.equals(servicesPath)) {
                    continue;
                }

                //	1.1.加载服务定义集合：
                String uniqueId = definitionPath.substring(servicesPath.length() + 1);
                ServiceDefinition serviceDefinition = parseServiceDefinition(definitionJson);
                DynamicConfigManager.getInstance().putServiceDefinition(uniqueId, serviceDefinition);
                log.info("#RegistryManager#subscribeService 1.1 加载服务定义信息 uniqueId : {}, serviceDefinition : {}",
                        uniqueId,
                        FastJsonConvertUtil.convertObjectToJSON(serviceDefinition));

                //	1.2 加载服务实例集合：
                //	首先拼接当前服务定义的服务实例前缀路径
                String serviceInstancePrefix = instancesPath + Registry.PATH + uniqueId;
                List<Pair<String, String>> instanceList = this.registryService.getListByPrefixKey(serviceInstancePrefix);
                Set<ServiceInstance> serviceInstanceSet = new HashSet<>();
                for (Pair<String, String> instance : instanceList) {
                    String instanceJson = instance.getObject2();
                    ServiceInstance serviceInstance = FastJsonConvertUtil.convertJSONToObject(instanceJson, ServiceInstance.class);
                    serviceInstanceSet.add(serviceInstance);
                }
                DynamicConfigManager.getInstance().addServiceInstance(uniqueId, serviceInstanceSet);
                log.info("#RegistryManager#subscribeService 1.2 加载服务实例 uniqueId : {}, serviceDefinition : {}",
                        uniqueId,
                        FastJsonConvertUtil.convertObjectToJSON(serviceInstanceSet));

            }

            //	2. 加载规则集合：
            List<Pair<String, String>> ruleList = this.registryService.getListByPrefixKey(rulesPath);
            for (Pair<String, String> r : ruleList) {
                String rulePath = r.getObject1();
                String ruleJson = r.getObject2();
                if (rulePath.endsWith(rulesPath)) {
                    continue;
                }
                Rule rule = FastJsonConvertUtil.convertJSONToObject(ruleJson, Rule.class);
                DynamicConfigManager.getInstance().putRule(rule.getId(), rule);
                log.info("#RegistryManager#subscribeService 2 加载规则信息 ruleId : {}, rule : {}",
                        rule.getId(),
                        FastJsonConvertUtil.convertObjectToJSON(rule));
            }

        } catch (Exception e) {
            log.error("#RegistryManager#subscribeService 服务订阅失败 ", e);
        } finally {
            // 标记加载完成了
            countDownLatch.countDown();
            log.info("#RegistryManager#subscribeService  ------------ 	服务订阅结束 	---------------");
        }
    }

    /**
     * 把从注册中心拉取过来的json字符串 转换成指定的ServiceDefinition
     *
     * @param definitionJson
     * @return
     */
    private ServiceDefinition parseServiceDefinition(String definitionJson) {
        // 这个不确定ServiceDefinition#invokerMap中ServiceInvoker实现类的类型
        Map<String, Object> jsonMap = FastJsonConvertUtil.convertJSONToObject(definitionJson, java.util.Map.class);
        ServiceDefinition serviceDefinition = new ServiceDefinition();

        //	填充 serviceDefinition
        serviceDefinition.setUniqueId((String) jsonMap.get("uniqueId"));
        serviceDefinition.setServiceId((String) jsonMap.get("serviceId"));
        serviceDefinition.setProtocol((String) jsonMap.get("protocol"));
        serviceDefinition.setPatternPath((String) jsonMap.get("patternPath"));
        serviceDefinition.setVersion((String) jsonMap.get("version"));
        serviceDefinition.setEnable((boolean) jsonMap.get("enable"));
        serviceDefinition.setEnvType((String) jsonMap.get("envType"));

        Map<String, ServiceInvoker> invokerMap = new HashMap<String, ServiceInvoker>();
        serviceDefinition.setInvokerMap(invokerMap);
        JSONObject jsonInvokerMap = (JSONObject) jsonMap.get("invokerMap");

        switch (serviceDefinition.getProtocol()) {
            case RapidProtocol.HTTP:
                Map<String, Object> httpInvokerMap = FastJsonConvertUtil.convertJSONToObject(jsonInvokerMap, Map.class);
                for (Map.Entry<String, Object> me : httpInvokerMap.entrySet()) {
                    String path = me.getKey();
                    JSONObject jsonInvoker = (JSONObject) me.getValue();
                    HttpServiceInvoker httpServiceInvoker = FastJsonConvertUtil.convertJSONToObject(jsonInvoker, HttpServiceInvoker.class);
                    invokerMap.put(path, httpServiceInvoker);
                }
                break;
            case RapidProtocol.DUBBO:
                Map<String, Object> dubboInvokerMap = FastJsonConvertUtil.convertJSONToObject(jsonInvokerMap, Map.class);
                for (Map.Entry<String, Object> me : dubboInvokerMap.entrySet()) {
                    String path = me.getKey();
                    JSONObject jsonInvoker = (JSONObject) me.getValue();
                    DubboServiceInvoker dubboServiceInvoker = FastJsonConvertUtil.convertJSONToObject(jsonInvoker, DubboServiceInvoker.class);
                    invokerMap.put(path, dubboServiceInvoker);
                }
                break;
            default:
                break;
        }
        return serviceDefinition;
    }

    private class ServiceListener implements Notify {

        @Override
        public void put(String key, String value) throws Exception {
            log.info("ServiceListener#put#  key={},value={}",key,value);
            // 等待将 etcd 中的数据添加到jvm中
            countDownLatch.await();

            // 如果监听到的是【根目录】则直接放弃
            if (servicesPath.equals(key) || instancesPath.equals(key) || rulesPath.equals(key)) {
                return;
            }

            // 如果是服务定义发生变更
            if (key.contains(servicesPath)) {
                String uniqueId = key.substring(servicesPath.length() + 1);
                //	ServiceDefinition
                ServiceDefinition serviceDefinition = parseServiceDefinition(value);
                DynamicConfigManager.getInstance().putServiceDefinition(uniqueId, serviceDefinition);
                return;
            }

            // 如果是服务实例发生变更
            if (key.contains(instancesPath)) {
                //	ServiceInstance
                //			hello:1.0.0/192.168.11.100:1234
                String temp = key.substring(instancesPath.length() + 1);
                String[] tempArray = temp.split(Registry.PATH);
                if (tempArray.length == 2) {
                    String uniqueId = tempArray[0];
                    ServiceInstance serviceInstance = FastJsonConvertUtil.convertJSONToObject(value, ServiceInstance.class);
                    DynamicConfigManager.getInstance().updateServiceInstance(uniqueId, serviceInstance);
                }
                return;
            }

            //	如果是规则发生变更
            if (key.contains(rulesPath)) {
                //	Rule
                String ruleId = key.substring(rulesPath.length() + 1);
                Rule rule = FastJsonConvertUtil.convertJSONToObject(value, Rule.class);
                DynamicConfigManager.getInstance().putRule(ruleId, rule);
                return;
            }
            log.warn("走到了未监控的数据路径 key={}，value={}", key, value);
        }

        @Override
        public void delete(String key) throws Exception {
            log.info("ServiceListener#delete#  key={}",key);
            countDownLatch.await();

            if (servicesPath.equals(key) ||
                    instancesPath.equals(key) ||
                    rulesPath.equals(key)) {
                return;
            }

            //	如果是服务定义发生变更：
            if (key.contains(servicesPath)) {
                String uniqueId = key.substring(servicesPath.length() + 1);
                DynamicConfigManager.getInstance().removeServiceDefinition(uniqueId);
                DynamicConfigManager.getInstance().removeServiceInstancesByUniqueId(uniqueId);
                return;
            }
            //	如果是服务实例发生变更：
            if (key.contains(instancesPath)) {
                //	hello:1.0.0/192.168.11.100:1234
                String temp = key.substring(instancesPath.length() + 1);
                String[] tempArray = temp.split(Registry.PATH);
                if (tempArray.length == 2) {
                    String uniqueId = tempArray[0];
                    String serviceInstanceId = tempArray[1];
                    DynamicConfigManager.getInstance().removeServiceInstance(uniqueId, serviceInstanceId);
                }
                return;
            }
            //	如果是规则发生变更：
            if (key.contains(rulesPath)) {
                String ruleId = key.substring(rulesPath.length() + 1);
                DynamicConfigManager.getInstance().removeRule(ruleId);
                return;
            }
        }
    }

    /**
     * 网关自身注册服务
     */
    class RegistryServer {

        private RegistryService registryService;

        /**
         * 例如：/rapid-dev/gateway/10.5.13.49:8888
         */
        private String selfPath;

        public RegistryServer(RegistryService registryService) throws Exception {
            this.registryService = registryService;
            this.registryService.registerPathIfNotExists(superPath, "", true);
            this.registryService.registerPathIfNotExists(gatewaysPath, "", true);
            this.selfPath = gatewaysPath + Registry.PATH + rapidConfig.getRapidId();
        }

        public void registerSelf() throws Exception {
            String rapidConfigJson = FastJsonConvertUtil.convertObjectToJSON(rapidConfig);
            this.registryService.registerPathIfNotExists(selfPath, rapidConfigJson, false);
        }
    }

    public String getSuperPath() {
        return superPath;
    }

    public String getServicesPath() {
        return servicesPath;
    }

    public String getInstancesPath() {
        return instancesPath;
    }

    public String getRulesPath() {
        return rulesPath;
    }

    public String getGatewaysPath() {
        return gatewaysPath;
    }
}
