package com.lvchao.rapid.core.rolling;

import lombok.Getter;

/**
 * <p>
 * 环形数组事件类型类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/8 8:46
 */
public enum RollingNumberEvent {

    /**
     * 成功
     */
    SUCCESS(1, 1),
    /**
     * 失败
     */
    FAILURE(1, 2),
    /**
     * 请求慢调用, BLOCK
     */
    REQUEST_TIMEOUT(1, 3),
    /**
     * 路由转发慢调用, BLOCK
     */
    ROUTE_TIMEOUT(1, 4);

    private final int type;

    @Getter
    private final int name;

    RollingNumberEvent(int type, int name) {
        this.type = type;
        this.name = name;
    }

    public boolean isCounter() {
        return type == 1;
    }

    public boolean isMaxUpdater() {
        return type == 2;
    }
}
