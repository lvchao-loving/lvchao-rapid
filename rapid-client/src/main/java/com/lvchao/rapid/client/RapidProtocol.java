package com.lvchao.rapid.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * 表示注册服务的协议枚举类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/10 17:47
 */
@Getter
@AllArgsConstructor
public enum RapidProtocol {

    HTTP("http", "http协议"),
    DUBBO("dubbo", "http协议");

    private String code;
    private String desc;

}
