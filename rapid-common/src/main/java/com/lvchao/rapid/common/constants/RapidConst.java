package com.lvchao.rapid.common.constants;

/**
 * <p>
 * 网关常量类：与业务相关
 * </p>
 *
 * @author lvchao
 * @since 2023/1/30 23:00
 */
/**
 * <p>
 *
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public interface RapidConst {

	String RAPID = "rapid";

	String UNIQUE_ID = "uniqueId";

	/**
	 * 默认版本
	 */
	String DEFAULT_VERSION = "1.0.0";

	/**
	 * 协议 key
	 */
	String PROTOCOL_KEY = "protocol";
	
	/**
	 * 	默认的实例权重为100
	 */
	int DEFAULT_WEIGHT = 100;
	
	/**
	 * 	请求超时时间默认为20s
	 */
	int DEFAULT_REQUEST_TIMEOUT = 20000;
	
}
