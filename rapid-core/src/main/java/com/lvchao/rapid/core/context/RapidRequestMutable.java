package com.lvchao.rapid.core.context;

import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;

/**
 * <p>
 * 请求可修改的参数操作接口
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 22:30
 */
public interface RapidRequestMutable {

    /**
     * 设置请求host
     *
     * @param host
     */
    void setModifyHost(String host);

    /**
     * 获取修改的host
     *
     * @return
     */
    String getModifyHost();

    /**
     * 设置请求路径
     *
     * @param path
     */
    void setModifyPath(String path);

    /**
     * 获取修改的地址
     *
     * @return
     */
    String getModifyPath();

    /**
     * 添加请求头信息
     *
     * @param name
     * @param value
     */
    void addHeader(CharSequence name, String value);

    /**
     * 设置请求头信息
     *
     * @param name
     * @param value
     */
    void setHeader(CharSequence name, String value);

    /**
     * 添加请求的查询参数
     *
     * @param name
     * @param value
     */
    void addQueryParam(String name, String value);


    /**
     * 添加或替换cookie
     *
     * @param cookie
     */
    void addOrReplaceCookie(Cookie cookie);

    /**
     * 添加form表单参数
     *
     * @param name
     * @param value
     */
    void addFormParam(String name, String value);


    /**
     * 设置请求超时时间
     *
     * @param requestTimeout
     */
    void setRequestTimeout(int requestTimeout);

    /**
     * 构建转发请求的请求对象
     *
     * @return
     */
    Request build();

    /**
     * 获取最终的路由路径
     *
     * @return
     */
    String getFinalUrl();

}
