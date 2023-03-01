package com.lvchao.rapid.core;

import com.lvchao.rapid.common.constants.BasicConst;
import com.lvchao.rapid.common.exception.RapidBaseException;
import com.lvchao.rapid.common.util.NetUtils;
import com.lvchao.rapid.common.util.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static com.lvchao.rapid.common.enums.ResponseCode.CONFIG_ERROR;

/**
 * <p>
 * 网关配置信息加载类
 * <p>
 * 网关配置加载规则：优先级顺序如下：高的优先级会覆盖掉低的优先级
 * 运行参数(最高) ->  jvm参数  -> 环境变量  -> 配置文件  -> 内部RapidConfig对象的默认属性值(最低);
 * </p>
 *
 * @author lvchao
 * @since 2023/1/30 22:08
 */
@Slf4j
public class RapidConfigLoader {
    /**
     * 环境变量 配置前缀
     */
    private final static String CONFIG_ENV_PREFIEX = "RAPID_";

    /**
     * jvm 配置文件前缀
     */
    private final static String CONFIG_JVM_PREFIEX = "rapid.";

    /**
     * 配置文件名称
     */
    private final static String CONFIG_FILE = "rapid.properties";

    private final static RapidConfigLoader INSTANCE = new RapidConfigLoader();

    private RapidConfig rapidConfig = new RapidConfig();

    private RapidConfigLoader() {
    }

    public static RapidConfigLoader getInstance() {
        return INSTANCE;
    }

    public static RapidConfig getRapidConfig() {
        return INSTANCE.rapidConfig;
    }

    /**
     * 加载配置 运行参数(最高) ->  jvm参数  -> 环境变量  -> 配置文件  -> 内部RapidConfig对象的默认属性值(最低)
     *
     * @param args
     * @return
     */
    public RapidConfig load(String args[]) {

        loadRapidConfig(args);

        checkRapidConfig();

        return rapidConfig;
    }

    /**
     * 按顺序配置参数
     *
     * @param args
     */
    private void loadRapidConfig(String args[]) {
        //	1.配置文件
        {
            InputStream is = RapidConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (is != null) {
                Properties properties = new Properties();
                try {
                    properties.load(is);
                    PropertiesUtils.properties2Object(properties, rapidConfig);
                } catch (IOException e) {
                    //	warn
                    log.warn("#RapidConfigLoader# load config file: {} is error", CONFIG_FILE, e);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            //	ignore
                        }
                    }
                }
            }
        }

        //	2.环境变量
        {
            Map<String, String> env = System.getenv();
            Properties properties = new Properties();
            properties.putAll(env);
            PropertiesUtils.properties2Object(properties, rapidConfig, CONFIG_ENV_PREFIEX);
        }

        //	3.jvm参数
        {
            Properties properties = System.getProperties();
            PropertiesUtils.properties2Object(properties, rapidConfig, CONFIG_JVM_PREFIEX);
        }

        //	4.运行参数: --xxx=xxx --enable=true  --port=1234
        {
            if (args != null && args.length > 0) {
                Properties properties = new Properties();
                for (String arg : args) {
                    if (arg.startsWith("--") && arg.contains("=")) {
                        properties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
                    }
                }
                PropertiesUtils.properties2Object(properties, rapidConfig);
            }
        }
        // TODO 重新设置 rapidId
        rapidConfig.setRapidId(NetUtils.getLocalIp() + BasicConst.COLON_SEPARATOR + rapidConfig.getPort());
    }

    /**
     * 校验初始化的 RapidConfig 文件
     */
    private void checkRapidConfig() {

        if (rapidConfig.getPort() < 0 || rapidConfig.getPort() > 65535) {
            throw new RapidBaseException(CONFIG_ERROR.getMessage(), CONFIG_ERROR);
        }

    }
}
