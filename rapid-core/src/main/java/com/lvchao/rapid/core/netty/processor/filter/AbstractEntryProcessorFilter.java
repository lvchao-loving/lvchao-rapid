package com.lvchao.rapid.core.netty.processor.filter;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.lvchao.rapid.common.config.Rule;
import com.lvchao.rapid.common.constants.BasicConst;
import com.lvchao.rapid.common.util.JSONUtil;
import com.lvchao.rapid.core.context.Context;
import com.lvchao.rapid.core.netty.processor.cache.DefaultCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 抽象的Filter 用于真正的Filter进行继承使用
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 10:30
 */
@Slf4j
public abstract class AbstractEntryProcessorFilter<FilterConfigClass> extends AbstractLinkedProcessorFilter<Context> {

    /**
     * 配置类上的注解信息
     */
    protected Filter filterAnnotation;

    /**
     * 初始化缓存
     */
    protected Cache<String, FilterConfigClass> cache;

    /**
     * 泛型 class 文件
     */
    protected final Class<FilterConfigClass> filterConfigClass;

    public AbstractEntryProcessorFilter(Class<FilterConfigClass> filterConfigClass, String cacheId) {
        // 说明：这里的 this 指代的是子类
        /**
         * TODO 说明一下理解
         * this --->> 返回的是什么？【子类】
         * super --->> 返回的是什么？【子类】
         */
        this.filterAnnotation = this.getClass().getAnnotation(Filter.class);
        this.filterConfigClass = filterConfigClass;
        // 将 FilterConfigClass 存储到本地缓存中
        this.cache = DefaultCacheManager.getInstance().create(cacheId);
    }

    @Override
    public boolean check(Context context) throws Throwable {
        // 根据注解上传入的filterId 判断当前Rule中是否存在
        return context.getRule().hashId(filterAnnotation.id());
    }

    @Override
    public void transformEntry(Context ctx, Object... args) throws Throwable {
        FilterConfigClass filterConfigClass = dynamicLoadCache(ctx, args);
        log.info("当前filer class：{}，参数值：{}", this.getClass().getName() ,JSON.toJSONString(filterConfigClass));
        super.transformEntry(ctx, filterConfigClass);
    }

    /**
     * 动态加载缓存：每一个过滤器的具体配置规则
     *
     * @param ctx
     * @param args
     * @return
     */
    private FilterConfigClass dynamicLoadCache(Context ctx, Object[] args) {
        //	通过上下文对象拿到规则，再通过规则获取到指定filterId的FilterConfig
        Rule.FilterConfig filterConfig = ctx.getRule().getFilterConfig(filterAnnotation.id());

        //	定义一个cacheKey
        String ruleId = ctx.getRule().getId();

        // 拼接缓存key：ruleId + $ + filterAnnotation.id()
        String cacheKey = ruleId + BasicConst.DOLLAR_SEPARATOR + filterAnnotation.id();

        FilterConfigClass fcc = cache.getIfPresent(cacheKey);
        if (fcc == null) {
            if (filterConfig != null && StringUtils.isNotEmpty(filterConfig.getConfig())) {
                String configStr = filterConfig.getConfig();
                try {
                    fcc = JSONUtil.parse(configStr, filterConfigClass);
                    cache.put(cacheKey, fcc);
                } catch (Exception e) {
                    log.error("#AbstractEntryProcessorFilter# dynamicLoadCache filterId: {}, config parse error: {}", filterAnnotation.id(), configStr, e);
                }
            }
        }
        return fcc;
    }
}
