package com.lvchao.rapid.common.exception;


import com.lvchao.rapid.common.enums.ResponseCode;

/**
 * <p>
 * 服务信息未找到异常定义：比如服务定义、实例等信息未找到均会抛出此异常
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public class RapidNotFoundException extends RapidBaseException {

	private static final long serialVersionUID = -5534700534739261761L;

	public RapidNotFoundException(){
		this(ResponseCode.SERVICE_DEFINITION_NOT_FOUND);
	}

	public RapidNotFoundException(ResponseCode code) {
		super(code.getMessage(), code);
	}
	
	public RapidNotFoundException(Throwable cause, ResponseCode code) {
		super(code.getMessage(), cause, code);
	}
	
}
