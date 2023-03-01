package com.lvchao.rapid.core.netty.processor.filter;

import com.lvchao.rapid.common.util.ServiceLoader;
import com.lvchao.rapid.core.context.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * <p>
 * 默认过滤器工厂实现类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 20:49
 */
@Slf4j
public class DefaultProcessorFilterFactory extends AbstractProcessorFilterFactory {

    private static class SingletonHolder {
        private static final DefaultProcessorFilterFactory INSTANCE = new DefaultProcessorFilterFactory();
    }

    public static DefaultProcessorFilterFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 构造方法：加载所有的ProcessorFilter子类的实现
     */
    private DefaultProcessorFilterFactory(){

        // SPI方式加载filter的集合：
        Map<String , List<ProcessorFilter<Context>>> filterMap = new LinkedHashMap<String, List<ProcessorFilter<Context>>>();

        // 通过ServiceLoader加载
        ServiceLoader<ProcessorFilter> serviceLoader = ServiceLoader.load(ProcessorFilter.class);

        // SPI 在遍历的时候才会创建对象，实现的原理就是底层重写了迭代器的next方法
        for(ProcessorFilter<Context> filter : serviceLoader) {
            Filter annotation = filter.getClass().getAnnotation(Filter.class);
            if(annotation != null) {
                String filterType = annotation.value().getCode();
                List<ProcessorFilter<Context>> filterList = filterMap.get(filterType);
                if(filterList == null) {
                    filterList = new ArrayList<ProcessorFilter<Context>>();
                    filterMap.put(filterType, filterList);
                }
                filterList.add(filter);
            }
        }

        //	java基础：枚举类循环也是有顺序的
        for(ProcessorFilterType filterType : ProcessorFilterType.values()) {
            List<ProcessorFilter<Context>> filterList = filterMap.get(filterType.getCode());
            if(filterList == null || filterList.isEmpty()) {
                continue;
            }

            /**
             * 根据注解的 order 进行排序
             */
            Collections.sort(filterList, new Comparator<ProcessorFilter<Context>>() {
                @Override
                public int compare(ProcessorFilter<Context> o1, ProcessorFilter<Context> o2) {
                    return o1.getClass().getAnnotation(Filter.class).order() -
                            o2.getClass().getAnnotation(Filter.class).order();
                }
            });

            try {
                super.buildFilterChain(filterType, filterList);
            } catch (Exception e) {
                //	ignore
                log.error("#DefaultProcessorFilterFactory.buildFilterChain# 网关过滤器加载异常, 异常信息为：{}!",e.getMessage(), e);
            }
        }

    }

    /**
     * 正常过滤器链条执行：pre + route + post
     * @param ctx
     * @throws Exception
     */
    @Override
    public void doFilterChain(Context ctx) throws Exception {
        try {
            defaultProcessorFilterChain.entry(ctx);
        } catch (Throwable e) {
            log.error("#DefaultProcessorFilterFactory.doFilterChain# ERROR MESSAGE: {}" , e.getMessage(), e);

            //	设置异常
            ctx.setThrowable(e);

            //	执行doFilterChain显示抛出异常时，Context上下文的生命周期为：Context.TERMINATED
            if(ctx.isTerminated()) {
                ctx.runned();
            }
            //	执行异常处理的过滤器链条
            doErrorFilterChain(ctx);
        }
    }

    /**
     * 异常过滤器链条执行：error + post
     * @param ctx
     * @throws Exception
     */
    @Override
    public void doErrorFilterChain(Context ctx) throws Exception {
        try {
            errorProcessorFilterChain.entry(ctx);
        } catch (Throwable e) {
            log.error("#DefaultProcessorFilterFactory.doErrorFilterChain# ERROR MESSAGE: {}" , e.getMessage(), e);
        }
    }

}
