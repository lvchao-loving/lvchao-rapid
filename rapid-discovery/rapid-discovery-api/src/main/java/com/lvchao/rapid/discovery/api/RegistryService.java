package com.lvchao.rapid.discovery.api;

/**
 * <p>
 * 注册服务接口
 * </p>
 *
 * @author lvchao
 * @since 2023/2/11 12:47
 */
public interface RegistryService extends Registry{
    /**
     * 添加一堆的监听事件
     * @param superPath
     * @param notify
     */
    void addWatcherListeners(String superPath, Notify notify);

    /**
     * 初始化注册服务
     * @param registryAddress
     */
    void initialized(String registryAddress);
}
