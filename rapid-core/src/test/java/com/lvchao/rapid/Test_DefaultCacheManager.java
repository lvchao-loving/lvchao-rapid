package com.lvchao.rapid;

import com.github.benmanes.caffeine.cache.Cache;
import com.lvchao.rapid.core.netty.processor.cache.DefaultCacheManager;
import org.junit.Test;

/**
 * <p>
 * 测试 DefaultCacheManager
 * </p>
 *
 * @author lvchao
 * @since 2023/2/27 19:38
 */
public class Test_DefaultCacheManager {
    @Test
    public void test01(){
        DefaultCacheManager instance = DefaultCacheManager.getInstance();
        Cache<String, String> a = instance.create("a");
        System.out.println(instance.getCacheMap().size());
        Cache<String, String> b = instance.create("b");
        System.out.println(instance.getCacheMap().size());
        a.put("a","a");
        System.out.println(a.getIfPresent("b"));
    }
}
