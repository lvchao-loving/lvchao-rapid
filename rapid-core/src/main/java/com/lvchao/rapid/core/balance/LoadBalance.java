package com.lvchao.rapid.core.balance;

import com.lvchao.rapid.common.config.ServiceInstance;
import com.lvchao.rapid.core.context.RapidContext;

/**
 * <p>
 * 负载均衡最上层的接口定义
 * </p>
 *
 * @author lvchao
 * @since 2023/2/23 17:15
 */
public interface LoadBalance {

    /**
     * 默认权重
     */
    int DEFAULT_WEIGHT = 100;

    /**
     * 默认预热时间
     */
    int DEFAULT_WARMUP = 5 * 60 * 1000;

    /**
     * 从所有实例列表中选择一个实例
     * @param context
     * @return
     */
    ServiceInstance select(RapidContext context);
}
