package com.lvchao.rapid.client;

import java.lang.annotation.*;

/**
 * <p>
 * 必须要在服务的方法上进行强制的声明
 * </p>
 *
 * @author lvchao
 * @since 2023/2/10 17:49
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RapidInvoker {

    /**
     * 访问路径
     *
     * @return
     */
    String path();
    
}
