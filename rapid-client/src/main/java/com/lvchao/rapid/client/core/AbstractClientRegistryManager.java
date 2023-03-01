package com.lvchao.rapid.client.core;

import com.lvchao.rapid.client.core.autoconfigure.RapidProperties;
import com.lvchao.rapid.common.config.ServiceDefinition;
import com.lvchao.rapid.common.config.ServiceInstance;
import com.lvchao.rapid.common.constants.BasicConst;
import com.lvchao.rapid.common.util.FastJsonConvertUtil;
import com.lvchao.rapid.common.util.ServiceLoader;
import com.lvchao.rapid.discovery.api.Registry;
import com.lvchao.rapid.discovery.api.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Properties;

/**
 * <p>
 * 抽象注册管理器
 * </p>
 *
 * @author lvchao
 * @since 2023/2/10 17:54
 */
@Slf4j
public abstract class AbstractClientRegistryManager {

    /**
     * 配置文件：路径 + 名称
     */
    public static final String PROPERTIES_PATH = "rapid.properties";

    /**
     * 注册地址
     */
    public static final String REGISTER_ADDRESS_KEY = "rapid.registryAddress";

    /**
     * 命名空间
     */
    public static final String NAMESPACE_KEY = "rapid.namespace";

    /**
     * 环境
     */
    public static final String ENV_KEY = "rapid.env";

    /**
     * 启动标记
     */
    protected volatile boolean whetherStart = false;

    public static Properties properties = new Properties();

    protected static String registryAddress;

    protected static String namespace;

    protected static String env;

    protected static String superPath;

    protected static String servicesPath;

    protected static String instancesPath;

    protected static String rulesPath;

    private RegistryService registryService;

    //	静态代码块读取rapid.properties配置文件
    static {
        log.debug("****************加载指定配置文件 start****************");
        InputStream is = null;
        is = AbstractClientRegistryManager.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH);
        try {
            if (is != null) {
                properties.load(is);
                registryAddress = properties.getProperty(REGISTER_ADDRESS_KEY);
                namespace = properties.getProperty(NAMESPACE_KEY);
                env = properties.getProperty(ENV_KEY);
            }
            // 如果没有则使用默认值
        } catch (Exception e) {
            log.error("#AbstractClientRegisteryManager# InputStream load is error", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ex) {
                    //	ignore
                    log.error("#AbstractClientRegisteryManager# InputStream close is error", ex);
                }
            }
        }
        log.debug("****************加载指定配置文件 end****************");
    }

    /**
     * <B>构造方法</B>AbstractClientRegisteryManager<BR>
     * application.properties/yml 优先级是最高的
     *
     * @param rapidProperties
     * @throws Exception
     */
    protected AbstractClientRegistryManager(RapidProperties rapidProperties){
        // 1.初始化加载配置信息
        if (rapidProperties.getRegistryAddress() != null) {
            registryAddress = rapidProperties.getRegistryAddress();
        }
        namespace = rapidProperties.getNamespace();
        // 判断还是为空则使用默认的的命名空间
        if (StringUtils.isBlank(namespace)) {
            namespace = RapidProperties.RAPID_PREFIX;
        }
        env = rapidProperties.getEnv();

        // 2.初始化加载注册中心对象
        ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
        for (RegistryService registryService : serviceLoader) {
            registryService.initialized(rapidProperties.getRegistryAddress());
            this.registryService = registryService;
        }

        // 3.注册构建顶级目录结构（例如：/rapid-dev ）
        generatorStructPath(Registry.PATH + namespace + BasicConst.BAR_SEPARATOR + env);
    }

    /**
     * 注册顶级结构目录路径，只需要构建一次即可
     *
     * @param path
     * @throws Exception
     */
    private void generatorStructPath(String path) {
        superPath = path;
        try {
            registryService.registerPathIfNotExists(superPath, "", true);
            registryService.registerPathIfNotExists(servicesPath = superPath + Registry.SERVICE_PREFIX, "", true);
            registryService.registerPathIfNotExists(instancesPath = superPath + Registry.INSTANCE_PREFIX, "", true);
            registryService.registerPathIfNotExists(rulesPath = superPath + Registry.RULE_PREFIX, "", true);
        } catch (Throwable t) {
            log.error("AbstractClientRegistryManager#generatorStructPath# error...");
            throw new RuntimeException("AbstractClientRegistryManager#generatorStructPath#方法异常");
        }
    }

    /**
     * 注册服务定义 对象
     * @param serviceDefinition
     * @throws Exception
     */
    protected void registerServiceDefinition(ServiceDefinition serviceDefinition) throws Exception {
        /**
         * 	/rapid-env
         * 		/services
         * 			/serviceA:1.0.0  ==> ServiceDefinition
         * 			/serviceA:2.0.0
         * 			/serviceB:1.0.0
         * 		/instances
         * 			/serviceA:1.0.0/192.168.11.100:port	 ==> ServiceInstance
         * 			/serviceA:1.0.0/192.168.11.101:port
         * 			/serviceB:1.0.0/192.168.11.102:port
         * 			/serviceA:2.0.0/192.168.11.103:port
         * 		/rules
         * 			/ruleId1	==>	Rule
         * 			/ruleId2
         * 		/gateway
         */
        String key = servicesPath + Registry.PATH + serviceDefinition.getUniqueId();

        if (!registryService.isExistKey(key)) {
            String value = FastJsonConvertUtil.convertObjectToJSON(serviceDefinition);
            registryService.registerPathIfNotExists(key, value, true);
        }
    }

    /**
     * 注册服务实例方法
     * @param serviceInstance
     * @throws Exception
     */
    protected void registerServiceInstance(ServiceInstance serviceInstance) throws Exception {
        String key = instancesPath
                + Registry.PATH
                + serviceInstance.getUniqueId()
                + Registry.PATH
                + serviceInstance.getServiceInstanceId();
        if (!registryService.isExistKey(key)) {
            String value = FastJsonConvertUtil.convertObjectToJSON(serviceInstance);
            registryService.registerPathIfNotExists(key, value, false);
        }
    }

    public static String getRegistryAddress() {
        return registryAddress;
    }

    public static String getNamespace() {
        return namespace;
    }

    public static String getEnv() {
        return env;
    }

    public static String getSuperPath() {
        return superPath;
    }

    public static String getServicesPath() {
        return servicesPath;
    }

    public static String getInstancesPath() {
        return instancesPath;
    }

    public static String getRulesPath() {
        return rulesPath;
    }
}
