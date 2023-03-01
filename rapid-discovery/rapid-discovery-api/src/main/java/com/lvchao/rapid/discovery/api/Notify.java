package com.lvchao.rapid.discovery.api;

/**
 * <p>
 * 监听服务接口
 * </p>
 *
 * @author lvchao
 * @since 2023/2/11 12:26
 */
public interface Notify {

    /**
     * 添加或者更新的方法
     *
     * @param key
     * @param value
     * @throws Exception
     */
    void put(String key, String value) throws Exception;

    /**
     * <B>方法名称：</B>delete<BR>
     * <B>概要说明：</B>删除方法<BR>
     *
     * @param key
     * @throws Exception
     * @author JiFeng
     * @since 2021年12月19日 下午1:28:59
     */
    void delete(String key) throws Exception;
}
