package com.lvchao.rapid.common.exception;

import com.lvchao.rapid.common.enums.ResponseCode;

/**
 * <B>主类名称：</B>RapidBaseException<BR>
 * <B>概要说明：</B>网关最基础的异常定义类<BR>
 * @author JiFeng
 * @since 2021年12月5日 下午4:39:59
 */
/**
 * <p>
 *
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public class RapidBaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public RapidBaseException() {
    }

    protected ResponseCode code;

    public RapidBaseException(String message, ResponseCode code) {
        super(message);
        this.code = code;
    }

    public RapidBaseException(String message, Throwable cause, ResponseCode code) {
        super(message, cause);
        this.code = code;
    }

    public RapidBaseException(ResponseCode code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public RapidBaseException(String message, Throwable cause, 
    		boolean enableSuppression, boolean writableStackTrace, ResponseCode code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
    
    public ResponseCode getCode() {
        return code;
    }

}
