package com.lvchao.rapid.core.netty.processor.filter;

import lombok.Data;

/**
 * <p>
 * 所有的过滤器配置实现类的Base类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/9 11:21
 */
@Data
public class FilterConfiguration {

    /**
     * 	是否打印日志
     */
    private boolean loggable = false;

}
