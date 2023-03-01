package com.lvchao.rapid.etcd.api;

/**
 * <p>
 * EtcdClientNotInitException
 * </p>
 *
 * @author lvchao
 * @since 2023/2/14 11:09
 */
public class EtcdClientNotInitException extends RuntimeException{
    public EtcdClientNotInitException() {
        super();
    }

    public EtcdClientNotInitException(String message) {
        super(message);
    }
}
