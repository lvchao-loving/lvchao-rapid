package com.lvchao.rapid.common.constants;

/**
 * <p>
 * 协议定义类
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public interface RapidProtocol {
	
	String HTTP = "http";
	
	String DUBBO = "dubbo";
	
	static boolean isHttp(String protocol) {
		return HTTP.equals(protocol);
	}
	
	static boolean isDubbo(String protocol) {
		return DUBBO.equals(protocol);
	}
	
}
