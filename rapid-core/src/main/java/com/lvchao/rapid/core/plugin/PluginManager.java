package com.lvchao.rapid.core.plugin;

import com.lvchao.rapid.common.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 插件管理器
 * </p>
 *
 * @author lvchao
 * @since 2023/2/21 16:32
 */
@Slf4j
public class PluginManager {

    private final MultiplePluginImpl multiplePlugin;

    private static final PluginManager INSTANCE = new PluginManager();

    public static Plugin getPlugin() {
        return INSTANCE.multiplePlugin;
    }

    private PluginManager(){
        // SPI方式扫描出所有插件实现
        ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class);
        Map<String, Plugin> pluginHashMap = new HashMap<>();
        for (Plugin plugin:plugins){
            if (!plugin.check()){
                continue;
            }
            String pluginName = plugin.getClass().getName();
            pluginHashMap.put(pluginName, plugin);
            log.info("#PluginFactory# The Scanner Plugin is: {}", plugin.getClass().getName());
        }
        this.multiplePlugin = new MultiplePluginImpl(pluginHashMap);
        Runtime.getRuntime().addShutdownHook(new Thread(multiplePlugin::destroy, "Shutdown-Plugin"));
    }
}
