package com.lvchao.rapid.common.exception;

import com.lvchao.rapid.common.enums.ResponseCode;

/**
 * <p>
 * 启动异常
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 12:27
 */
public class RapidStartUpException extends RapidBaseException{

    public RapidStartUpException() {
        this(ResponseCode.INTERNAL_ERROR);
    }

    public RapidStartUpException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public RapidStartUpException(Throwable cause, ResponseCode code) {
        super(code.getMessage(), cause, code);
        this.code = code;
    }
}
