package com.lvchao.rapid.core.netty.processor.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * DefaultCacheManager
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 10:39
 */
@Slf4j
public class DefaultCacheManager {

    /**
     * 私有构造函数
     */
    private DefaultCacheManager(){}

    /**
     * 双层缓存
     */
    @Getter
    private final ConcurrentHashMap<String, Cache<String,?>> cacheMap = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        private static final DefaultCacheManager INSTANCE = new DefaultCacheManager();
    }

    public static DefaultCacheManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 根据一个全局的缓存ID 创建一个Caffeine缓存对象
     * @param cacheId
     * @param <V>
     * @return
     */
    public <V> Cache<String, V> create(String cacheId) {
        cacheMap.computeIfAbsent(cacheId,v -> Caffeine.newBuilder().build());
        log.info("当前DefaultCacheManager中共计{}个缓存key",cacheMap.size());
        return (Cache<String, V>) cacheMap.get(cacheId);
    }

    /**
     * 根据cacheId 和对应的真正Caffeine缓存key 删除一个Caffeine缓存对象
     * @param cacheId
     * @param key
     * @param <V>
     */
    public <V> void remove(String cacheId, String key) {
        Cache<String, V> cache = (Cache<String, V>) cacheMap.get(cacheId);
        if(cache != null) {
            cache.invalidate(key);
        }
    }

    /**
     * 根据全局的缓存id 删除这个Caffeine缓存对象
     * @param cacheId
     * @param <V>
     */
    public <V> void remove(String cacheId) {
        Cache<String, V> cache = (Cache<String, V>) cacheMap.get(cacheId);
        if(cache != null) {
            cache.invalidateAll();
        }
    }

    /**
     * 清空所有的缓存
     */
    public void cleanAll() {
        cacheMap.values().forEach(cache -> cache.invalidateAll());
    }

    /**
     * createForDubboGenericService
     * @return
     */
    public static Cache<String, GenericService> createForDubboGenericService() {
        return Caffeine.newBuilder().build();
    }

}
