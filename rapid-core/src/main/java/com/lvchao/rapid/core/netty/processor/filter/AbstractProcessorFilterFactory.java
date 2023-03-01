package com.lvchao.rapid.core.netty.processor.filter;

import com.lvchao.rapid.core.context.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 抽象的过滤器工厂
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 10:35
 */
@Slf4j
public abstract class AbstractProcessorFilterFactory implements ProcessorFilterFactory {

    /*
     * pre + route + post
     * 正常：过滤器链条
     */
    public DefaultProcessorFilterChain defaultProcessorFilterChain = new DefaultProcessorFilterChain("defaultProcessorFilterChain");

    /*
     * error + post
     * 错误：过滤器链条
     */
    public DefaultProcessorFilterChain errorProcessorFilterChain = new DefaultProcessorFilterChain("errorProcessorFilterChain");

    /*
     * 根据过滤器类型获取filter集合
     */
    public Map<String /* processorFilterType */, Map<String /* filterId */, ProcessorFilter<Context>>> processorFilterTypeMap = new LinkedHashMap<>();

    /*
     * 根据过滤器id获取对应的Filter
     */
    public Map<String /* filterId */, ProcessorFilter<Context>> processorFilterIdMap = new LinkedHashMap<>();

    /**
     * 构建过滤器链条
     *
     * @param filterType
     * @param filters
     * @throws Exception
     */
    @Override
    public void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters) throws Exception {
        switch (filterType) {
            case PRE:
            case ROUTE:
                addFilterForChain(defaultProcessorFilterChain, filters);
                break;
            case ERROR:
                addFilterForChain(errorProcessorFilterChain, filters);
                break;
            case POST:
                addFilterForChain(defaultProcessorFilterChain, filters);
                addFilterForChain(errorProcessorFilterChain, filters);
                break;
            default:
                throw new RuntimeException("ProcessorFilterType is not supported !");
        }

    }

    private void addFilterForChain(DefaultProcessorFilterChain processorFilterChain,
                                   List<ProcessorFilter<Context>> filters) throws Exception {
        for (ProcessorFilter<Context> processorFilter : filters) {
            // 执行初始化方法
            processorFilter.init();
            doBuilder(processorFilterChain, processorFilter);
        }
    }

    /**
     * 添加过滤器到指定的filterChain
     *
     * @param processorFilterChain
     * @param processorFilter
     */
    private void doBuilder(DefaultProcessorFilterChain processorFilterChain,
                           ProcessorFilter<Context> processorFilter) {

        log.info("filterChain: {}, the scanner filter is : {}", processorFilterChain.getId(), processorFilter.getClass().getName());


        Filter annotation = processorFilter.getClass().getAnnotation(Filter.class);

        if (annotation != null) {
            //	构建过滤器链条，添加 filter
            processorFilterChain.addLast((AbstractLinkedProcessorFilter<Context>) processorFilter);

            //	如果filterId 为空，则取 classname
            String filterId = annotation.id();
            if (filterId == null || filterId.length() < 1) {
                filterId = processorFilter.getClass().getName();
            }
            // annotation.value() 为当前过滤器类型
            String code = annotation.value().getCode();
            Map<String, ProcessorFilter<Context>> filterMap = processorFilterTypeMap.get(code);
            if (filterMap == null) {
                filterMap = new LinkedHashMap<String, ProcessorFilter<Context>>();
            }
            filterMap.put(filterId, processorFilter);
            processorFilterTypeMap.put(code, filterMap);
            processorFilterIdMap.put(filterId, processorFilter);
        }
    }

    @Override
    public <T> T getFilter(Class<T> t) throws Exception {
        Filter annotation = t.getAnnotation(Filter.class);
        if (annotation != null) {
            String filterId = annotation.id();
            // 防御性编程：防御性编程（Defensive programming）是防御式设计的一种具体体现，它是为了保证，对程序的不可预见的使用，不会造成程序功能上的损坏。
            if (filterId == null || filterId.length() < 1) {
                filterId = t.getName();
            }
            return this.getFilter(filterId);
        }
        return null;
    }

    @Override
    public <T> T getFilter(String filterId) throws Exception {
        ProcessorFilter<Context> filter = null;
        if (!processorFilterIdMap.isEmpty()) {
            filter = processorFilterIdMap.get(filterId);
        }
        return (T) filter;
    }

}
