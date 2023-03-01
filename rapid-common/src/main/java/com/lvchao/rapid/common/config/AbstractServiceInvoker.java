package com.lvchao.rapid.common.config;

/**
 * <p>
 * 抽象的服务调用接口实现类
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public class AbstractServiceInvoker implements ServiceInvoker {
	
	protected String invokerPath;

	/**
	 * 这个参数需要手动设置参数绑定
	 */
	protected String ruleId;

	/**
	 * 默认的超时时间
	 */
	protected int timeout = 5000;

	@Override
	public String getInvokerPath() {
		return invokerPath;
	}

	@Override
	public void setInvokerPath(String invokerPath) {
		this.invokerPath = invokerPath;
	}

	@Override
	public String getRuleId() {
		return ruleId;
	}

	@Override
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;		
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
