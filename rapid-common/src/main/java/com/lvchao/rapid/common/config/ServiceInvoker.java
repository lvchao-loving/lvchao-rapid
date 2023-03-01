package com.lvchao.rapid.common.config;

/**
 * <p>
 * 服务调用的接口模型描述
 *
 * 实现类需要三个字段：
 *  String invokerPath;
 *  String ruleId;
 *  Integer timeout;
 *
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public interface ServiceInvoker {

    /**
     * 获取 真正的服务调用的全路径
     *
     * @return
     */
    String getInvokerPath();

    /**
     * 设置 真正的服务调用的全路径
     *
     * @param invokerPath
     */
    void setInvokerPath(String invokerPath);

    /**
     * 获取 指定服务调用绑定的唯一规则
     *
     * @return
     */
    String getRuleId();

    /**
     * 设置 指定服务调用绑定的唯一规则
     *
     * @param ruleId
     */
    void setRuleId(String ruleId);

    /**
     * 获取该服务调用(方法)的超时时间
     *
     * @return
     */
    int getTimeout();

    /**
     * 设置服务的超时时间
     *
     * @param timeout
     */
    void setTimeout(int timeout);

}
