package com.lvchao.rapid.core.helper;

import com.lvchao.rapid.common.config.DynamicConfigManager;
import com.lvchao.rapid.common.config.Rule;
import com.lvchao.rapid.common.config.ServiceDefinition;
import com.lvchao.rapid.common.config.ServiceInvoker;
import com.lvchao.rapid.common.constants.BasicConst;
import com.lvchao.rapid.common.constants.RapidConst;
import com.lvchao.rapid.common.constants.RapidProtocol;
import com.lvchao.rapid.common.enums.ResponseCode;
import com.lvchao.rapid.common.exception.RapidNotFoundException;
import com.lvchao.rapid.common.exception.RapidPathNoMatchedException;
import com.lvchao.rapid.common.exception.RapidResponseException;
import com.lvchao.rapid.common.util.AntPathMatcher;
import com.lvchao.rapid.core.context.AttributeKey;
import com.lvchao.rapid.core.context.HttpRequestWrapper;
import com.lvchao.rapid.core.context.RapidContext;
import com.lvchao.rapid.core.context.RapidRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 解析请求信息，构建上下文对象
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 16:18
 */
public class RequestHelper {

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    /**
     * 解析FullHttpRequest 构建RapidContext核心构建方法
     * @return
     */
    public static RapidContext doContext(HttpRequestWrapper wrapper){
        // 1.构建请求对象 RapidRequest
        RapidRequest rapidRequest = doRequest(wrapper);

        // 2.根据请求对象里的uniqueId，获取资源服务信息(也就是服务定义信息)
        ServiceDefinition serviceDefinition  = getServiceDefinition(rapidRequest);

        // 3.快速路径匹配失败的策略
        if (!ANT_PATH_MATCHER.match(serviceDefinition.getPatternPath(),rapidRequest.getPath())){
            throw new RapidPathNoMatchedException();
        }
        // 4.根据请求对象获取服务定义对应的方法调用，然后获取对应的规则
        ServiceInvoker serviceInvoker = getServiceInvoker(rapidRequest, serviceDefinition);
        String ruleId = serviceInvoker.getRuleId();
        Rule rule = DynamicConfigManager.getInstance().getRule(ruleId);
        if (Objects.isNull(rule)){
            throw new RuntimeException("规则不能为空");
        }

        //	5.构建我们而定RapidContext对象
        RapidContext rapidContext = new RapidContext.Builder()
                .setProtocol(serviceDefinition.getProtocol())
                .setRapidRequest(rapidRequest)
                .setNettyCtx(wrapper.getCtx())
                .setKeepAlive(HttpUtil.isKeepAlive(wrapper.getFullHttpRequest()))
                .setRule(rule)
                .build();
        // 6.设置SR: SR(Server[Rapid-Core] Received):	服务器接收到网络请求
        rapidContext.setSRTime(rapidRequest.getBeginTime());

        // 7.设置一些必要的上下文参数用于后面使用
        putContext(rapidContext, serviceInvoker);

        return rapidContext;
    }

    /**
     * 设置必要的上下文方法
     * @param rapidContext
     * @param serviceInvoker
     */
    private static void putContext(RapidContext rapidContext, ServiceInvoker serviceInvoker) {
        switch (rapidContext.getProtocol()) {
            case RapidProtocol.HTTP:
                rapidContext.putAttribute(AttributeKey.HTTP_INVOKER, serviceInvoker);
                break;
            case RapidProtocol.DUBBO:
                rapidContext.putAttribute(AttributeKey.DUBBO_INVOKER, serviceInvoker);
                break;
            default:
                break;
        }
    }

    /**
     * 根据请求对象和服务定义对象获取对应的ServiceInvoke
     * @param rapidRequest
     * @param serviceDefinition
     * @return
     */
    private static ServiceInvoker getServiceInvoker(RapidRequest rapidRequest, ServiceDefinition serviceDefinition) {
        Map<String, ServiceInvoker> invokerMap = serviceDefinition.getInvokerMap();
        ServiceInvoker serviceInvoker = invokerMap.get(rapidRequest.getPath());
        if(serviceInvoker == null) {
            throw new RapidNotFoundException();
        }
        return serviceInvoker;
    }

    /**
     * 通过请求对象获取服务资源信息
     * @param rapidRequest
     * @return
     */
    private static ServiceDefinition getServiceDefinition(RapidRequest rapidRequest) {
        //	ServiceDefinition从哪里获取，就是在网关服务初始化的时候(加载的时候)？ 从缓存信息里获取
        ServiceDefinition serviceDefinition = DynamicConfigManager.getInstance().getServiceDefinition(rapidRequest.getUniqueId());
        //	做异常情况判断
        if(serviceDefinition == null) {
            throw new RapidNotFoundException();
        }
        return serviceDefinition;
    }

    /**
     * 构建RapidRequest请求对象
     * @param wrapper
     * @return
     */
    private static RapidRequest doRequest(HttpRequestWrapper wrapper) {
        ChannelHandlerContext ctx = wrapper.getCtx();
        FullHttpRequest fullHttpRequest = wrapper.getFullHttpRequest();

        HttpHeaders headers = fullHttpRequest.headers();

        // 服务唯一标识，必须存在
        String  uniqueId = headers.get(RapidConst.UNIQUE_ID);

        if (StringUtils.isBlank(uniqueId)){
            throw new RapidResponseException(ResponseCode.REQUEST_PARSE_ERROR_NO_UNIQUEID);
        }

        String host = headers.get(HttpHeaderNames.HOST);
        HttpMethod method = fullHttpRequest.method();
        String uri = fullHttpRequest.uri();
        String clientIp = getClientIp(ctx, fullHttpRequest);
        String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
        Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);

        RapidRequest rapidRequest = new RapidRequest(uniqueId,
                charset,
                clientIp,
                host,
                uri,
                method,
                contentType,
                headers,
                fullHttpRequest);

        return rapidRequest;
    }

    /**
     * 获取客户端 ip
     * @return
     */
    private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request){
        /**
         * X-Forwarded-For（XFF）是用来识别通过HTTP代理或负载均衡方式连接到Web服务器的客户端最原始的IP地址的HTTP请求头字段
         */
        String xForwardedValue = request.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);

        String clientIp = null;

        if (StringUtils.isNotBlank(clientIp)){
            List<String> values = Arrays.asList(xForwardedValue.split(", "));
            if(values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
                clientIp = values.get(0);
            }
        }

        if(clientIp == null) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
            clientIp = inetSocketAddress.getAddress().getHostAddress();
        }

        return clientIp;
    }
}
