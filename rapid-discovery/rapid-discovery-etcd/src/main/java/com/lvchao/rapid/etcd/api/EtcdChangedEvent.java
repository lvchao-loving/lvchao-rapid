package com.lvchao.rapid.etcd.api;

import io.etcd.jetcd.KeyValue;
import lombok.Getter;

/**
 * <p>
 * EtcdChangedEvent
 * </p>
 *
 * @author lvchao
 * @since 2023/2/14 11:07
 */
@Getter
public class EtcdChangedEvent {

    public static enum Type {
        PUT,
        DELETE,
        UNRECOGNIZED;
    }

    private KeyValue prevKeyValue;

    private KeyValue curtkeyValue;

    private Type type;

    public EtcdChangedEvent(KeyValue prevKeyValue, KeyValue curtkeyValue, Type type) {
        this.prevKeyValue = prevKeyValue;
        this.curtkeyValue = curtkeyValue;
        this.type = type;
    }
}
