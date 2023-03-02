package com.lvchao.rapid.core.context;

import com.lvchao.rapid.common.config.Rule;
import com.lvchao.rapid.common.util.AssertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * <p> 
 * 网关请求上下文核心对象
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 17:11
 */
public class RapidContext extends BasicContext{

    private final RapidRequest rapidRequest;

    private RapidResponse rapidResponse;

    private final Rule rule;

    private RapidContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive,
                         RapidRequest rapidRequest, Rule rule) {
        super(protocol, nettyCtx, keepAlive);
        this.rapidRequest = rapidRequest;
        this.rule = rule;
    }

    /**
     * 建造者类
     */
    public static class Builder {

        private String protocol;

        private ChannelHandlerContext nettyCtx;

        private RapidRequest rapidRequest;

        private Rule rule;

        private boolean keepAlive;

        public Builder() {
        }

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setNettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder setRapidRequest(RapidRequest rapidRequest) {
            this.rapidRequest = rapidRequest;
            return this;
        }

        public Builder setRule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public Builder setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public RapidContext build() {
            AssertUtil.notNull(protocol, "protocol不能为空");
            AssertUtil.notNull(nettyCtx, "nettyCtx不能为空");
            AssertUtil.notNull(rapidRequest, "rapidRequest不能为空");
            AssertUtil.notNull(rule, "rule不能为空");
            return new RapidContext(protocol, nettyCtx, keepAlive, rapidRequest, rule);
        }
    }

    /**
     * 获取必要的上下文参数，如果没有则抛出IllegalArgumentException
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getRequiredAttribute(AttributeKey<T> key) {
        T value = getAttribute(key);
        AssertUtil.notNull(value, "required attribute '" + key + "' is missing !");
        return value;
    }

    /**
     * 获取指定key的上下文参数，如果没有则返回第二个参数的默认值
     * @param key
     * @param defaultValue
     * @param <T>
     * @return
     */
    public <T> T getAttributeOrDefault(AttributeKey<T> key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    /**
     * 根据过滤器id获取对应的过滤器配置信息
     * @param filterId
     * @return
     */
    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    /**
     * 获取上下文中唯一的UniqueId
     * @return
     */
    public String getUniqueId() {
        return rapidRequest.getUniqueId();
    }

    /**
     * 重写覆盖父类：basicContext的该方法，主要用于真正的释放操作
     */
    @Override
    public void releaseRequest() {
        if(requestReleased.compareAndSet(false, true)) {
            ReferenceCountUtil.release(rapidRequest.getFullHttpRequest());
        }
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public RapidRequest getRequest() {
        return rapidRequest;
    }

    /**
     * 调用该方法就是获取原始请求内容，不去做任何修改动作
     * @return
     */
    public RapidRequest getOriginRequest() {
        return rapidRequest;
    }

    /**
     * 调用该方法区分于原始的请求对象操作，主要就是做属性修改的
     * @return
     */
    public RapidRequest getRequestMutale() {
        return rapidRequest;
    }

    @Override
    public RapidResponse getResponse() {
        return rapidResponse;
    }

    @Override
    public void setResponse(Object response) {
        this.rapidResponse = (RapidResponse)response;
    }
}
