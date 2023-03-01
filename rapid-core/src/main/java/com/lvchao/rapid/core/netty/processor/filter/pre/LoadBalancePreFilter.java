package com.lvchao.rapid.core.netty.processor.filter.pre;

import com.lvchao.rapid.common.config.DynamicConfigManager;
import com.lvchao.rapid.common.config.ServiceInstance;
import com.lvchao.rapid.common.constants.ProcessorFilterConstants;
import com.lvchao.rapid.common.constants.RapidProtocol;
import com.lvchao.rapid.common.enums.LoadBalanceStrategy;
import com.lvchao.rapid.common.enums.ResponseCode;
import com.lvchao.rapid.common.exception.RapidResponseException;
import com.lvchao.rapid.core.balance.LoadBalance;
import com.lvchao.rapid.core.balance.LoadBalanceFactory;
import com.lvchao.rapid.core.context.AttributeKey;
import com.lvchao.rapid.core.context.Context;
import com.lvchao.rapid.core.context.RapidContext;
import com.lvchao.rapid.core.context.RapidRequest;
import com.lvchao.rapid.core.netty.processor.cache.ConstantCache;
import com.lvchao.rapid.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.lvchao.rapid.core.netty.processor.filter.Filter;
import com.lvchao.rapid.core.netty.processor.filter.FilterConfiguration;
import com.lvchao.rapid.core.netty.processor.filter.ProcessorFilterType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * <p>
 * 负载均衡前置过滤器
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 20:59
 */
@Filter(
        id = ProcessorFilterConstants.LOADBALANCE_PRE_FILTER_ID,
        name = ProcessorFilterConstants.LOADBALANCE_PRE_FILTER_NAME,
        value = ProcessorFilterType.PRE,
        order = ProcessorFilterConstants.LOADBALANCE_PRE_FILTER_ORDER
)
public class LoadBalancePreFilter extends AbstractEntryProcessorFilter<LoadBalancePreFilter.Config> {

    public LoadBalancePreFilter() {
        super(LoadBalancePreFilter.Config.class,ConstantCache.LOAD_BALANCE_PRE_FILTER_CACHE_ID);
    }

    @Override
    public void entry(Context ctx, Object... args) throws Throwable {
        try {
            RapidContext rapidContext = (RapidContext) ctx;
            LoadBalancePreFilter.Config config = (LoadBalancePreFilter.Config)args[0];
            LoadBalanceStrategy loadBalanceStrategy = config.getBalanceStrategy();
            String protocol = rapidContext.getProtocol();
            switch (protocol) {
                case RapidProtocol.HTTP:
                    doHttpLoadBalance(rapidContext, loadBalanceStrategy);
                    break;
                case RapidProtocol.DUBBO:
                    doDubboLoadBalance(rapidContext, loadBalanceStrategy);
                    break;
                default:
                    break;
            }
        } finally {
            super.fireNext(ctx, args);;
        }
    }

    private void doHttpLoadBalance(RapidContext rapidContext, LoadBalanceStrategy loadBalanceStrategy) {
        RapidRequest rapidRequest = rapidContext.getRequest();
        String uniqueId = rapidRequest.getUniqueId();
        Set<ServiceInstance> serviceInstances = DynamicConfigManager.getInstance()
                .getServiceInstanceByUniqueId(uniqueId);

        rapidContext.putAttribute(AttributeKey.MATCH_INSTANCES, serviceInstances);

        //	通过负载均衡枚举值获取负载均衡实例对象
        LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(loadBalanceStrategy);
        //	调用负载均衡实现，选择一个实例进行返回
        ServiceInstance serviceInstance = loadBalance.select(rapidContext);

        if(serviceInstance == null) {
            //	如果服务实例没有找到：终止请求继续执行，显示抛出异常
            rapidContext.terminated();
            throw new RapidResponseException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }

        //	这一步非常关键：设置可修改的服务host，为当前选择的实例对象的address
        rapidContext.getRequestMutale().setModifyHost(serviceInstance.getAddress());
    }

    private void doDubboLoadBalance(RapidContext rapidContext, LoadBalanceStrategy loadBalanceStrategy) {
        //	将负载均衡策略设置到上下文中即可，由 dubbo LoadBalance去进行使用：SPI USED
        rapidContext.putAttribute(AttributeKey.DUBBO_LOADBALANCE_STRATEGY, loadBalanceStrategy);
    }

    /**
     * <B>主类名称：</B>Config<BR>
     * <B>概要说明：</B>负载均衡前置过滤器配置<BR>
     * @author JiFeng
     * @since 2021年12月20日 下午4:21:54
     */
    @Getter
    @Setter
    public static class Config extends FilterConfiguration {

        private LoadBalanceStrategy balanceStrategy = LoadBalanceStrategy.ROUND_ROBIN;

    }
}
