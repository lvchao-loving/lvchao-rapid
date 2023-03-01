package com.lvchao.rapid.common.exception;

import com.lvchao.rapid.common.enums.ResponseCode;
import lombok.Getter;

/**
 * <p>
 * 连接异常定义类
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public class RapidConnectException extends RapidBaseException {

	private static final long serialVersionUID = 1L;

	@Getter
	private final String uniqueId;
	
	@Getter
	private final String requestUrl;
	
	public RapidConnectException(String uniqueId, String requestUrl) {
		this.uniqueId = uniqueId;
		this.requestUrl = requestUrl;
	}
	
	public RapidConnectException(Throwable cause, String uniqueId, String requestUrl, ResponseCode code) {
		super(code.getMessage(), cause, code);
		this.uniqueId = uniqueId;
		this.requestUrl = requestUrl;
	}

}
