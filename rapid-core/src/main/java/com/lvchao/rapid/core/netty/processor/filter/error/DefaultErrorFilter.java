package com.lvchao.rapid.core.netty.processor.filter.error;

import com.lvchao.rapid.common.config.Rule;
import com.lvchao.rapid.common.constants.ProcessorFilterConstants;
import com.lvchao.rapid.common.enums.ResponseCode;
import com.lvchao.rapid.common.exception.RapidBaseException;
import com.lvchao.rapid.core.context.Context;
import com.lvchao.rapid.core.context.RapidResponse;
import com.lvchao.rapid.core.netty.processor.cache.ConstantCache;
import com.lvchao.rapid.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.lvchao.rapid.core.netty.processor.filter.Filter;
import com.lvchao.rapid.core.netty.processor.filter.ProcessorFilterType;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 默认异常处理过滤器
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 20:52
 */
@Filter(
        id = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ID,
        name = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_NAME,
        value = ProcessorFilterType.ERROR,
        order = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ORDER
)
@Slf4j
public class DefaultErrorFilter extends AbstractEntryProcessorFilter<Rule.FilterConfig> {

    public DefaultErrorFilter() {
        // 执行父类的构造方法
        super(Rule.FilterConfig.class, ConstantCache.DEFAULT_ERROR_FILTER_CACHE_ID);
    }

    @Override
    public void entry(Context ctx, Object... args) throws Throwable {
        try {
            // 从上下文中获取
            Throwable throwable = ctx.getThrowable();
            // 设置默认错误值
            ResponseCode responseCode = ResponseCode.INTERNAL_ERROR;
            // 如果异常属于自定义异常，则使用自定义异常信息的 ResponseCode
            if (throwable instanceof RapidBaseException) {
                RapidBaseException rapidBaseException = (RapidBaseException) throwable;
                responseCode = rapidBaseException.getCode();
            }
            RapidResponse rapidResponse = RapidResponse.buildRapidResponse(responseCode);
            ctx.setResponse(rapidResponse);
        } finally {
            log.info("============> do error filter <===============");
            //	设置写回标记
            ctx.writtened();
            //	触发后面的过滤器执行
            super.fireNext(ctx, args);
        }
    }
}
