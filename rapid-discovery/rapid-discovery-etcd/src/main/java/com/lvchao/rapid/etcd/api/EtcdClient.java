package com.lvchao.rapid.etcd.api;

import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.lease.LeaseRevokeResponse;
import io.etcd.jetcd.lease.LeaseTimeToLiveResponse;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.lock.UnlockResponse;
import io.etcd.jetcd.support.CloseableClient;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * EtcdClient
 * </p>
 *
 * @author lvchao
 * @since 2023/2/14 11:05
 */
public interface EtcdClient {

    String CHARSET = "utf-8";

    /**
     * 添加 key
     * @param key
     * @param value
     * @throws Exception
     */
    void putKey(String key, String value) throws Exception;

    /**
     * 添加key并返回CompletableFuture
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    CompletableFuture<PutResponse> putKeyCallFuture(String key, String value) throws Exception;

    /**
     * 获取 key
     * @param key
     * @return
     * @throws Exception
     */
    KeyValue getKey(final String key) throws Exception;

    /**
     * 删除 key
     * @param key
     */
    void deleteKey(String key);

    /**
     * 根据前缀获取多个 key
     * @param prefix
     * @return
     */
    List<KeyValue> getKeyWithPrefix(String prefix);

    /**
     * 根据前缀删除
     * @param prefix
     */
    void deleteKeyWithPrefix(String prefix);

    /**
     * 添加 key 并设置过期时间
     * @param key
     * @param value
     * @param expireTime
     * @return
     */
    long putKeyWithExpireTime(String key, String value, long expireTime);

    /**
     * 添加 key 以及租约Id
     * @param key
     * @param value
     * @param leaseId
     * @return
     * @throws Exception
     */
    long putKeyWithLeaseId(String key, String value, long leaseId) throws Exception;

    /**
     * 生成租约Id
     * @param expireTime
     * @return
     * @throws Exception
     */
    long generatorLeaseId(long expireTime) throws Exception;

    /**
     * 保持单一租约
     * @param leaseId
     * @param observer
     * @return
     */
    CloseableClient keepAliveSingleLease(long leaseId, StreamObserver<LeaseKeepAliveResponse> observer);

    /**
     *
     * @param leaseId
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    LeaseKeepAliveResponse keepAliveOnce(long leaseId) throws InterruptedException, ExecutionException;

    /**
     *
     * @param leaseId
     * @param timeout
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    LeaseKeepAliveResponse keepAliveOnce(long leaseId, long timeout) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     *
     * @param leaseId
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    LeaseTimeToLiveResponse timeToLiveLease(long leaseId) throws InterruptedException, ExecutionException;

    /**
     * 撤销租约
     * @param leaseId
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    LeaseRevokeResponse revokeLease(long leaseId) throws InterruptedException, ExecutionException;

    /**
     * 获取心跳租约Id
     * @return
     * @throws InterruptedException
     */
    long getHeartBeatLeaseId() throws InterruptedException;

    /**
     * 加锁
     * @param lockName
     * @return
     * @throws Exception
     */
    LockResponse lock(String lockName) throws Exception;

    /**
     * 加锁并设置过期时间
     * @param lockName
     * @param expireTime
     * @return
     * @throws Exception
     */
    LockResponse lock(String lockName, long expireTime) throws Exception;

    /**
     * 通过租约Id加锁
     * @param lockName
     * @param leaseId
     * @return
     * @throws Exception
     */
    LockResponse lockByLeaseId(String lockName, long leaseId) throws Exception;

    /**
     * 解锁
     * @param lockName
     * @return
     * @throws Exception
     */
    UnlockResponse unlock(String lockName) throws Exception;

    /**
     * 添加 观察监听
     * @param watcherKey
     * @param usePrefix
     * @param watcherListener
     */
    void addWatcherListener(final String watcherKey, final boolean usePrefix, WatcherListener watcherListener);

    /**
     * 移除 观察监听
     * @param watcherKey
     */
    void removeWatcherListener(final String watcherKey);

    /**
     * 添加心跳租约超时通知监听
     * @param heartBeatLeaseTimeoutListener
     */
    void addHeartBeatLeaseTimeoutNotifyListener(HeartBeatLeaseTimeoutListener heartBeatLeaseTimeoutListener);

    /**
     * 关闭 client
     */
    void close();

}
