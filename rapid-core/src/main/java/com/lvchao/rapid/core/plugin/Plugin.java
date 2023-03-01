package com.lvchao.rapid.core.plugin;

/**
 * <p>
 * 插件的生命周期管理
 * </p>
 *
 * @author lvchao
 * @since 2023/2/21 15:54
 */
public interface Plugin {

    /**
     * 插件校验
     * @return
     */
    default boolean check() {
        return true;
    }

    /**
     * 初始化
     */
    void init();

    /**
     * 销毁
     */
    void destroy();

    /**
     * 根据 插件名称 获取插件
     * @param pluginName
     * @return
     */
    Plugin getPlugin(String pluginName);
}
