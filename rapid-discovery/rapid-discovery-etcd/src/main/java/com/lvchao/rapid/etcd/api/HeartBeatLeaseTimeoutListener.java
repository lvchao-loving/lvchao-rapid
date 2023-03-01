package com.lvchao.rapid.etcd.api;

/**
 * <p>
 * HeartBeatLeaseTimeoutListener
 * </p>
 *
 * @author lvchao
 * @since 2023/2/14 11:06
 */
@FunctionalInterface
public interface HeartBeatLeaseTimeoutListener {
    /**
     * 超时通知
     */
    void timeoutNotify();
}
