package com.lvchao.rapid.core.plugin;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * 多个插件合并实现，并且安全执行插件逻辑
 * </p>
 *
 * @author lvchao
 * @since 2023/2/21 15:57
 */
@Slf4j
public class MultiplePluginImpl implements Plugin {

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private Map<String/** pluginName **/, Plugin> pluginMap;

    public MultiplePluginImpl(Map<String, Plugin> pluginMap) {
        this.pluginMap = pluginMap;
    }

    @Override
    public void init() {
        if (initialized.compareAndSet(false,true)){
            Thread thread = new Thread(() -> {
                pluginMap.values().forEach(item ->{
                    item.init();
                });
                log.info("初始化插件线程完成");
            });
            thread.setName("MultiplePluginImpl#init#初始化线程");
            thread.start();
        }
    }

    @Override
    public void destroy() {
        pluginMap.forEach((pluginName, plugin) -> {
            try {
                plugin.destroy();
            } catch (Throwable t) {
                log.error("MultiplePluginImpl, 插件关闭失败：{}", plugin.getClass().getName(), t);
            }
        });
    }

    @Override
    public Plugin getPlugin(String pluginName) {
        return pluginMap.get(pluginName);
    }
}
