package com.lvchao.rapid.common.hashed;

/**
 * <p>
 *
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public interface TimerTask {

    void run(Timeout timeout) throws Exception;

}