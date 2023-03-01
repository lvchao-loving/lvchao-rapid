package com.lvchao.rapid.core;

/**
 * <p>
 * 生命周期管理接口
 * </p>
 *
 * @author lvchao
 * @since 2023/1/30 22:57
 */
public interface LifeCycle {

    /**
     * 生命周期组件的初始化动作
     */
    void init();

    /**
     * 生命周期组件的启动方法
     */
    void start();

    /**
     * 生命周期组件的关闭方法
     */
    void shutdown();

}
