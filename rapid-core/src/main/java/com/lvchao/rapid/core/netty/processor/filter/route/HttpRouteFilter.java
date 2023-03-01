package com.lvchao.rapid.core.netty.processor.filter.route;

import com.lvchao.rapid.common.constants.ProcessorFilterConstants;
import com.lvchao.rapid.common.enums.ResponseCode;
import com.lvchao.rapid.common.exception.RapidConnectException;
import com.lvchao.rapid.common.exception.RapidResponseException;
import com.lvchao.rapid.common.util.TimeUtil;
import com.lvchao.rapid.core.RapidConfigLoader;
import com.lvchao.rapid.core.context.Context;
import com.lvchao.rapid.core.context.RapidContext;
import com.lvchao.rapid.core.context.RapidResponse;
import com.lvchao.rapid.core.helper.AsyncHttpHelper;
import com.lvchao.rapid.core.netty.processor.cache.ConstantCache;
import com.lvchao.rapid.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.lvchao.rapid.core.netty.processor.filter.Filter;
import com.lvchao.rapid.core.netty.processor.filter.FilterConfiguration;
import com.lvchao.rapid.core.netty.processor.filter.ProcessorFilterType;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * 请求路由的中置过滤器
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 21:00
 */
@Filter(
        id = ProcessorFilterConstants.HTTP_ROUTE_FILTER_ID,
        name = ProcessorFilterConstants.HTTP_ROUTE_FILTER_NAME,
        value = ProcessorFilterType.ROUTE,
        order = ProcessorFilterConstants.HTTP_ROUTE_FILTER_ORDER
)
@Slf4j
public class HttpRouteFilter extends AbstractEntryProcessorFilter<FilterConfiguration> {

    public HttpRouteFilter() {
        super(FilterConfiguration.class, ConstantCache.HTTP_ROUTE_FILTER_CACHE_ID);
    }

    /**
     * SR(Server[Rapid-Core] Received):	服务器接收到网络请求
     * SS(Server[Rapid-Core] Send):		服务器写回请求
     * RS(Route Send):						客户端发送请求
     * RR(Route Received): 				客户端收到请求
     */
    @Override
    public void entry(Context context, Object... args) throws Throwable {

        RapidContext rapidContext = (RapidContext) context;

        // 构建请求的 Request
        Request request = rapidContext.getRequestMutale().build();

        // 设置路由开始时间
        rapidContext.setRSTime(TimeUtil.currentTimeMillis());

        CompletableFuture<Response> completableFuture = AsyncHttpHelper.getInstance().executeRequest(request);

        // 执行【单异步模式】
        if (RapidConfigLoader.getRapidConfig().isWhenComplete()) {
            completableFuture.whenComplete((response, throwable) -> {
                complete(request, response, throwable, rapidContext, args);
            });
        }
        // 执行【双异步模式】
        else {
            completableFuture.whenCompleteAsync((response, throwable) -> {
                complete(request, response, throwable, rapidContext, args);
            });
        }
    }


    /**
     * 真正执行请求响应回来的操作方法
     *
     * @param request
     * @param response
     * @param throwable
     * @param rapidContext
     * @param args
     */
    private void complete(Request request, Response response, Throwable throwable, RapidContext rapidContext, Object... args) {
        try {
            // 1.设置路由结束时间
            rapidContext.setRRTime(TimeUtil.currentTimeMillis());
            // 2.释放请求资源
            rapidContext.releaseRequest();
            // 3.判断是否有异常产生
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                //	超时异常
                if (throwable instanceof TimeoutException) {
                    log.warn("#HttpRouteFilter# complete返回响应执行， 请求路径：{}，耗时超过 {}  ms.",
                            url,
                            (request.getRequestTimeout() == 0 ?
                                    RapidConfigLoader.getRapidConfig().getHttpRequestTimeout() :
                                    request.getRequestTimeout())
                    );
                    //	网关里设置异常都是使用自定义异常
                    rapidContext.setThrowable(new RapidResponseException(ResponseCode.REQUEST_TIMEOUT));
                }
                //	其他异常情况
                else {
                    rapidContext.setThrowable(new RapidConnectException(throwable,
                            rapidContext.getUniqueId(),
                            url,
                            ResponseCode.HTTP_RESPONSE_ERROR));
                }
            }
            //	正常返回响应结果：
            else {
                //	设置响应信息
                rapidContext.setResponse(RapidResponse.buildRapidResponse(response));
            }

        } catch (Throwable t) {
            //	最终兜底异常处理
            rapidContext.setThrowable(new RapidResponseException(ResponseCode.INTERNAL_ERROR));
            log.error("#HttpRouteFilter# complete catch到未知异常", t);
        } finally {
            try {
                // 1.设置写回标记
                rapidContext.writtened();

                // 2.让异步线程内部自己进行触发下一个节点执行
                super.fireNext(rapidContext, args);
            } catch (Throwable t) {
                // 兜底处理，把异常信息放入上下文
                rapidContext.setThrowable(new RapidResponseException(ResponseCode.INTERNAL_ERROR));
                log.error("#HttpRouteFilter# fireNext出现异常", t);
            }
        }
    }
}
