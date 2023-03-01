
package com.lvchao.rapid.common.exception;

import com.lvchao.rapid.common.enums.ResponseCode;

/**
 * <p>
 * 所有的响应异常基础定义
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public class RapidResponseException extends RapidBaseException {

    private static final long serialVersionUID = 1L;

    public RapidResponseException() {
        this(ResponseCode.INTERNAL_ERROR);
    }

    public RapidResponseException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public RapidResponseException(Throwable cause, ResponseCode code) {
        super(code.getMessage(), cause, code);
        this.code = code;
    }

}
