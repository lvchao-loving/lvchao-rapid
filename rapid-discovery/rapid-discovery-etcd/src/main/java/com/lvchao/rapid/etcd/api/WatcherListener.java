package com.lvchao.rapid.etcd.api;

/**
 * <p>
 * WatcherListener
 * </p>
 *
 * @author lvchao
 * @since 2023/2/14 11:06
 */
public interface WatcherListener {
    /**
     * 监听key的变化
     *
     * @param etcdClient
     * @param event
     * @throws Exception
     */
    void watcherKeyChanged(EtcdClient etcdClient, EtcdChangedEvent event) throws Exception;
}
