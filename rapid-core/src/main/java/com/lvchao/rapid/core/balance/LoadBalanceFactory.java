package com.lvchao.rapid.core.balance;

import com.lvchao.rapid.common.enums.LoadBalanceStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 负载均衡工厂
 * </p>
 *
 * @author lvchao
 * @since 2023/2/27 22:23
 */
public class LoadBalanceFactory {

    private final Map<LoadBalanceStrategy, LoadBalance> loadBalanceMap = new HashMap<>();
    private static final LoadBalanceFactory INSTANCE = new LoadBalanceFactory();

    private LoadBalanceFactory() {
        loadBalanceMap.put(LoadBalanceStrategy.RANDOM, new RandomLoadBalance());
        loadBalanceMap.put(LoadBalanceStrategy.ROUND_ROBIN, new RoundRobinLoadBalance());
    }

    public static LoadBalance getLoadBalance(LoadBalanceStrategy loadBalance) {
        return INSTANCE.loadBalanceMap.get(loadBalance);
    }
}
