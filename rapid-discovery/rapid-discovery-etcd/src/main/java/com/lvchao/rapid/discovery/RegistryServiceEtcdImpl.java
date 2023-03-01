package com.lvchao.rapid.discovery;

import com.lvchao.rapid.common.util.Pair;
import com.lvchao.rapid.discovery.api.Notify;
import com.lvchao.rapid.discovery.api.Registry;
import com.lvchao.rapid.discovery.api.RegistryService;
import com.lvchao.rapid.etcd.api.EtcdChangedEvent;
import com.lvchao.rapid.etcd.api.EtcdClient;
import com.lvchao.rapid.etcd.api.HeartBeatLeaseTimeoutListener;
import com.lvchao.rapid.etcd.api.WatcherListener;
import com.lvchao.rapid.etcd.core.EtcdClientImpl;
import io.etcd.jetcd.KeyValue;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 注册中心实现类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/14 21:52
 */
@Slf4j
public class RegistryServiceEtcdImpl implements RegistryService {

    private EtcdClient etcdClient;

    private Map<String, String> cachedMap = new HashMap<String, String>();

    @Override
    public void initialized(String registryAddress) {
        //	初始化etcd客户端对象
        etcdClient = new EtcdClientImpl(registryAddress,
                true,
                "",
                null,
                null,
                null);

        //	添加异常的过期处理监听
        etcdClient.addHeartBeatLeaseTimeoutNotifyListener(new HeartBeatLeaseTimeoutListener() {
            @Override
            public void timeoutNotify() {
                cachedMap.forEach((key, value) -> {
                    try {
                        registerEphemeralNode(key, value);
                    } catch (Exception e) {
                        //	ignore
                        log.error("#RegistryServiceEtcdImpl.initialized# HeartBeatLeaseTimeoutListener: timeoutNotify is error", e);
                    }
                });
            }
        });
    }

    /**
     * 根据一个路径做多种实现，服务的子节点变更 添加监听
     *
     * @param superPath
     * @param notify
     */
    @Override
    public void addWatcherListeners(String superPath, Notify notify) {
        /**
         * 说明这里的巧妙设计：所有的变化都会通知到传入的 Notify 实现类中。
         */
        etcdClient.addWatcherListener(superPath + Registry.SERVICE_PREFIX, true, new InnerWatcherListener(notify));
        etcdClient.addWatcherListener(superPath + Registry.INSTANCE_PREFIX, true, new InnerWatcherListener(notify));
        etcdClient.addWatcherListener(superPath + Registry.RULE_PREFIX, true, new InnerWatcherListener(notify));
        //	网关服务本身发变更
        etcdClient.addWatcherListener(superPath + Registry.GATEWAY_PREFIX, true, new InnerWatcherListener(notify));
    }

    static class InnerWatcherListener implements WatcherListener {

        private final Notify notify;

        public InnerWatcherListener(Notify notify) {
            this.notify = notify;
        }

        @Override
        public void watcherKeyChanged(EtcdClient etcdClient, EtcdChangedEvent event) throws Exception {
            EtcdChangedEvent.Type type = event.getType();
            KeyValue curtKeyValue = event.getCurtkeyValue();
            switch (type) {
                case PUT:
                    notify.put(curtKeyValue.getKey().toString(Charset.defaultCharset()),
                            curtKeyValue.getValue().toString(Charset.defaultCharset()));
                    break;
                case DELETE:
                    notify.delete(curtKeyValue.getKey().toString(Charset.defaultCharset()));
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public long registerEphemeralNode(String key, String value) throws Exception {
        long leaseId = this.etcdClient.getHeartBeatLeaseId();
        cachedMap.put(key, value);
        return this.etcdClient.putKeyWithLeaseId(key, value, leaseId);
    }

    @Override
    public void registerPathIfNotExists(String path, String value, boolean isPersistent) throws Exception {
        if (!isExistKey(path)) {
            if (isPersistent) {
                registerPersistentNode(path, value);
            } else {
                registerEphemeralNode(path, value);
            }
        }
    }

    @Override
    public void registerPersistentNode(String key, String value) throws Exception {
        this.etcdClient.putKey(key, value);
    }

    @Override
    public List<Pair<String, String>> getListByPrefixKey(String prefix) throws Exception {
        List<KeyValue> list = this.etcdClient.getKeyWithPrefix(prefix);
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        for (KeyValue kv : list) {
            result.add(new Pair<String, String>(kv.getKey().toString(Charset.defaultCharset()),
                    kv.getValue().toString(Charset.defaultCharset())));
        }
        return result;
    }

    @Override
    public Pair<String, String> getByKey(String key) throws Exception {
        KeyValue kv = etcdClient.getKey(key);
        return new Pair<String, String>(kv.getKey().toString(Charset.defaultCharset()),
                kv.getValue().toString(Charset.defaultCharset()));
    }

    @Override
    public boolean isExistKey(String key) throws Exception {
        KeyValue kv = etcdClient.getKey(key);
        if (kv == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void deleteByKey(String key) {
        etcdClient.deleteKey(key);
    }

    @Override
    public void close() {
        /**
         * 	1.对api使用非常的熟悉：能够解决相关的bug和对应的业务问题
         * 	2.对源码的了解和深度使用：精通
         *  3.最终可以封装非常好用的正确的api 给业务侧同学去使用（全公司）
         */
        if (etcdClient != null) {
            etcdClient.close();
        }
    }

}
