package com.lvchao.rapid.core.netty.processor.filter;

import lombok.Getter;

/**
 * <p>
 * 过滤器的类型定义
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 9:00
 */
@Getter
public enum ProcessorFilterType {

    PRE("PRE", "前置过滤器"),

    ROUTE("ROUTE", "中置过滤器"),

    ERROR("ERROR", "异常过滤器"),

    POST("POST", "后置过滤器");

    private final String code;

    private final String message;

    ProcessorFilterType(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
