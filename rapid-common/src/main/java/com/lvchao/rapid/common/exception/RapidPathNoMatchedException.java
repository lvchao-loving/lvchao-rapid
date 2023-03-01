package com.lvchao.rapid.common.exception;

import com.lvchao.rapid.common.enums.ResponseCode;

/**
 * <p>
 * 请求路径不匹配的异常定义类
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public class RapidPathNoMatchedException extends RapidBaseException {

	private static final long serialVersionUID = 1L;

	public RapidPathNoMatchedException() {
		this(ResponseCode.PATH_NO_MATCHED);
	}
	
	public RapidPathNoMatchedException(ResponseCode code) {
		super(code.getMessage(), code);
	}
	
	public RapidPathNoMatchedException(Throwable cause, ResponseCode code) {
		super(code.getMessage(), cause, code);
	}
}
